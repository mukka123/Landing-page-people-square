-- V1__core_schema.sql
-- Core schema for the Political Operations and Constituency Analytics Platform.
-- Zones (per HLD section 6): this migration owns the "curated" zone tables that the
-- application reads/writes transactionally. Landing/staging tables for import are in V2.

create extension if not exists postgis;
create extension if not exists pgcrypto; -- gen_random_uuid()
create extension if not exists citext;   -- case-insensitive email comparisons

-- ============================================================================
-- IDENTITY & TENANCY
-- ============================================================================

create table tenant (
    id              uuid primary key default gen_random_uuid(),
    name            text not null,
    status          text not null default 'ACTIVE' check (status in ('ACTIVE','SUSPENDED','ARCHIVED')),
    data_region     text not null default 'IN',
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);

create table app_user (
    id              uuid primary key default gen_random_uuid(),
    tenant_id       uuid not null references tenant(id),
    email           citext not null,
    display_name    text not null,
    status          text not null default 'ACTIVE' check (status in ('ACTIVE','SUSPENDED','DEACTIVATED')),
    consent_recorded_at timestamptz,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now(),
    unique (tenant_id, email)
);

-- Roles are fixed by the platform (Table 4 of the HLD); kept as a lookup table so
-- authorization checks can join rather than branch on string literals.
create table role (
    code            text primary key,
    description     text not null
);

insert into role (code, description) values
    ('PLATFORM_ADMIN', 'Tenant configuration, identity, integrations, retention, audit'),
    ('CONSTITUENCY_ADMIN', 'Users, geography, elections, campaigns, master data'),
    ('STRATEGY_ANALYST', 'Election trends, cluster analysis, reports, scenario models'),
    ('WAR_ROOM_MANAGER', 'Daily plan, task assignment, escalations, events, dashboards'),
    ('AREA_COORDINATOR', 'Manage booth teams, issues, visits, evidence'),
    ('BOOTH_COORDINATOR', 'Execute field tasks, record counts, issues, meeting notes'),
    ('VOLUNTEER', 'View and complete assigned tasks and attendance'),
    ('DATA_STEWARD', 'Import, validate, reconcile, publish datasets'),
    ('CONTENT_REVIEWER', 'Approve externally published text, images, translations'),
    ('AUDITOR', 'Read immutable audit evidence and compliance reports');

-- A user's role is always scoped to a geography (or NULL = whole tenant, PLATFORM_ADMIN only).
-- This is the join point that "role based access with geography attributes" (HLD section 3) hangs off.
create table user_role_assignment (
    id              uuid primary key default gen_random_uuid(),
    user_id         uuid not null references app_user(id) on delete cascade,
    role_code       text not null references role(code),
    geographic_unit_id uuid,  -- FK added below, once geographic_unit exists (avoids forward reference)
    effective_from  date not null default current_date,
    effective_to    date,
    created_at      timestamptz not null default now()
);

-- ============================================================================
-- GEOGRAPHY (versioned hierarchy + polling station crosswalk)
-- ============================================================================

create table geographic_unit (
    id              uuid primary key default gen_random_uuid(),
    tenant_id       uuid not null references tenant(id),
    type            text not null check (type in
                      ('STATE','DISTRICT','CONSTITUENCY','MANDAL','MUNICIPALITY','WARD','VILLAGE','COLONY','CLUSTER')),
    name            text not null,
    parent_id       uuid references geographic_unit(id),
    geometry        geometry(MultiPolygon, 4326),
    effective_from  date not null,
    effective_to    date,
    created_at      timestamptz not null default now()
);
create index idx_geographic_unit_parent on geographic_unit(parent_id);
create index idx_geographic_unit_geom on geographic_unit using gist(geometry);
create index idx_geographic_unit_tenant_type on geographic_unit(tenant_id, type);

alter table user_role_assignment
    add constraint fk_ura_geo foreign key (geographic_unit_id) references geographic_unit(id);

create table election (
    id              uuid primary key default gen_random_uuid(),
    tenant_id       uuid not null references tenant(id),
    type            text not null check (type in ('ASSEMBLY','PARLIAMENT','LOCAL_BODY')),
    year            int not null,
    election_date   date,
    constituency_id uuid not null references geographic_unit(id),
    status          text not null default 'DRAFT' check (status in ('DRAFT','STAGED','PUBLISHED','ARCHIVED')),
    created_at      timestamptz not null default now(),
    unique (tenant_id, constituency_id, year, type)
);

create table polling_station (
    id              uuid primary key default gen_random_uuid(),
    election_id     uuid not null references election(id),
    part_number     int not null,           -- unique within an election+constituency only, NOT globally
    name            text not null,
    address         text,
    location        geometry(Point, 4326),
    elector_count   int,
    created_at      timestamptz not null default now(),
    unique (election_id, part_number)
);
create index idx_polling_station_location on polling_station using gist(location);

-- Maps a polling station in one election to a stable geographic cluster/area, so that
-- booth renumbering across 2014/2018/2023 doesn't break historical comparison.
create table polling_station_crosswalk (
    id                  uuid primary key default gen_random_uuid(),
    from_polling_station_id uuid not null references polling_station(id),
    to_geographic_unit_id   uuid not null references geographic_unit(id), -- must be type = CLUSTER
    match_method        text not null check (match_method in ('NAME_MATCH','LOCATION_MATCH','MANUAL','HYBRID')),
    confidence          numeric(4,3) not null check (confidence between 0 and 1),
    approval_status     text not null default 'PENDING' check (approval_status in ('PENDING','APPROVED','REJECTED')),
    approved_by         uuid references app_user(id),
    approved_at         timestamptz,
    created_at          timestamptz not null default now(),
    unique (from_polling_station_id)
);

-- ============================================================================
-- ELECTIONS (candidates, parties, results, turnout)
-- ============================================================================

create table party (
    id              uuid primary key default gen_random_uuid(),
    tenant_id       uuid not null references tenant(id),
    name            text not null,
    symbol          text,
    unique (tenant_id, name)
);

create table candidate (
    id              uuid primary key default gen_random_uuid(),
    election_id     uuid not null references election(id),
    party_id        uuid references party(id),
    name            text not null,
    source_ref      text
);

-- Source file lineage for every imported election result (HLD: "corrections preserve the
-- source file, earlier curated version, author, reason and approval").
create table election_source_file (
    id              uuid primary key default gen_random_uuid(),
    election_id     uuid not null references election(id),
    storage_key     text not null,
    checksum        text not null,
    uploaded_by     uuid not null references app_user(id),
    uploaded_at     timestamptz not null default now(),
    parser_version  text not null,
    status          text not null default 'RECEIVED'
                      check (status in ('RECEIVED','PARSING','PARSED','VALIDATION_FAILED','RECONCILED','PUBLISHED'))
);

create table booth_result (
    id                  uuid primary key default gen_random_uuid(),
    polling_station_id  uuid not null references polling_station(id),
    candidate_id        uuid not null references candidate(id),
    votes               int not null check (votes >= 0),
    postal_flag         boolean not null default false,
    source_file_id       uuid not null references election_source_file(id),
    created_at           timestamptz not null default now(),
    unique (polling_station_id, candidate_id, postal_flag)
);
create index idx_booth_result_station on booth_result(polling_station_id);
create index idx_booth_result_candidate on booth_result(candidate_id);

create table turnout (
    id                  uuid primary key default gen_random_uuid(),
    polling_station_id  uuid not null references polling_station(id) unique,
    male                int not null default 0,
    female              int not null default 0,
    other               int not null default 0,
    total_polled        int not null default 0,
    rejected            int not null default 0,
    nota                int not null default 0,
    tendered            int not null default 0,
    reconciled          boolean not null default false,
    reconciliation_note text
);

-- Derived metrics carry formula version + input snapshot per HLD data quality rules.
create table analytics_snapshot (
    id              uuid primary key default gen_random_uuid(),
    tenant_id       uuid not null references tenant(id),
    geographic_unit_id uuid not null references geographic_unit(id),
    election_id     uuid not null references election(id),
    metric          text not null,          -- e.g. TURNOUT_PCT, VOTE_SHARE, SWING, LEAD, CONCENTRATION
    party_id        uuid references party(id),
    value           numeric(12,4) not null,
    formula_version text not null,
    input_snapshot_ref text not null,
    refreshed_at    timestamptz not null default now()
);
create index idx_analytics_snapshot_lookup on analytics_snapshot(tenant_id, geographic_unit_id, election_id, metric);

-- ============================================================================
-- OPERATIONS (tasks) — Phase Two scope, table shell so FKs from other modules resolve
-- ============================================================================

create table task (
    id              uuid primary key default gen_random_uuid(),
    tenant_id       uuid not null references tenant(id),
    geographic_unit_id uuid references geographic_unit(id),
    title           text not null,
    owner_id        uuid references app_user(id),
    priority        text not null default 'NORMAL' check (priority in ('LOW','NORMAL','HIGH','CRITICAL')),
    due_at          timestamptz,
    status          text not null default 'OPEN' check (status in ('OPEN','IN_PROGRESS','BLOCKED','DONE','CANCELLED')),
    template_id     uuid,
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);

-- ============================================================================
-- AUDIT (append-only)
-- ============================================================================

create table audit_event (
    id              uuid primary key default gen_random_uuid(),
    tenant_id       uuid not null references tenant(id),
    actor_id        uuid references app_user(id),
    action          text not null,
    entity_type     text not null,
    entity_id       uuid,
    before_hash     text,
    after_hash      text,
    request_id      text not null,
    occurred_at     timestamptz not null default now()
);
create index idx_audit_event_tenant_time on audit_event(tenant_id, occurred_at desc);
-- Append-only enforcement: revoke UPDATE/DELETE at the application DB role level (see
-- deploy/db-roles.sql) rather than relying on triggers alone.

comment on table polling_station is 'Part number is unique within (election_id) only — never treat part_number as a stable cross-year identifier. Use polling_station_crosswalk for cross-election comparison.';
comment on table polling_station_crosswalk is 'to_geographic_unit_id must reference a GeographicUnit of type CLUSTER; enforced in application layer + a CHECK trigger (see V3).';
