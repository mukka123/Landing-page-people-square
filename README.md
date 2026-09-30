# Political Operations and Constituency Analytics Platform

Implementation of the HLD (`Political_Operations_Constituency_Analytics_HLD.docx`),
starting with **Phase One: Analytics MVP**, per the doc's own delivery roadmap.

## What is actually built here

| Area | Status |
|---|---|
| Identity & tenancy (tenant, user, role, geography-scoped role assignment) | Implemented |
| Versioned geography hierarchy | Implemented |
| Polling station + crosswalk (booth renumbering across years) | Implemented, incl. proposal + approval flow |
| Election import registration, lineage, publish gate | Implemented |
| **Election data quality / reconciliation rules** | Implemented + unit tested (see `ReconciliationServiceTest`) |
| Analytics trends API (governed query layer) | Implemented |
| Angular web app: auth interceptor, dashboard (trend view), geography screen | Implemented (needs a real OIDC client wired in) |
| Security: OIDC resource server, server-side geography scoping, Problem Details errors | Implemented |
| Tasks, issues, events, volunteers, content, knowledge, AI orchestrator, audit | **Scaffolded only** (package stubs) — Phase Two/Three per roadmap |
| OCR/document extraction, natural-language analytics, all other AI capabilities | Not built — these call an independently-deployed AI orchestration service that does not exist yet |
| Kafka/event broker wiring | Not built — events are documented (Table 16 of the HLD) but not yet published/consumed |
| Frontend PWA/offline sync | Not built |

This is a real starting slice, not a mockup: the reconciliation logic, crosswalk approval
workflow, and tenant/geography authorization model are the riskiest and most load-bearing
parts of the whole platform, so they were built and tested first rather than last.

## Running it locally

```bash
docker compose up -d          # postgres+postgis, redis
cd backend && mvn spring-boot:run
cd frontend && npm install && npm start
```

You will need a real OIDC provider issuing tokens with `tenant_id`, `sub`, and role claims
matching `user_role_assignment` rows — there is no bundled identity provider. For local
dev without one, stub `TenantContextFilter` to build a fixed `TenantContext`, or point
`OIDC_ISSUER_URI` at a local Keycloak.

## Project layout

```
backend/   Spring Boot 3 / Java 21 modular monolith
  common/      base entity, tenant context, Problem Details error handling
  identity/    tenant, user, role, geography-scoped role assignment
  geography/   versioned hierarchy, polling stations, crosswalk + proposal service
  elections/   election, party, candidate, booth results, turnout, import, RECONCILIATION
  analytics/   governed trend/metric query layer
  operations/, issues/, events/, content/, knowledge/, ai/, audit/   -- stubs, see below
  config/      security (OIDC resource server), tenant context filter, request id filter
frontend/  Angular 20 standalone-component app
  core/        auth, HTTP interceptors, typed API client
  features/    dashboard (trend view), geography
```

## Next steps, in build order

See `ROADMAP.md` for the phase breakdown. The immediate next slice (finishing Phase One) is:

1. Wire a real OIDC provider (Keycloak recommended for dev) and remove the placeholder `AuthService`.
2. Election import worker: OCR/table extraction for Form 20 PDFs — this is the first AI-adjacent
   piece and needs the AI orchestrator service scaffolded (currently just `app.ai-orchestrator.base-url`
   pointing at nothing).
3. Crosswalk name/location matching — `CrosswalkProposalService.proposeMapping` currently takes
   pre-computed candidates; the actual name-similarity + geo-proximity matcher isn't written yet.
4. `analytics_snapshot` refresh job (currently the table exists but nothing populates it on
   `ResultsPublished`).
5. Export jobs (async, signed URLs) — referenced in config, not implemented.
