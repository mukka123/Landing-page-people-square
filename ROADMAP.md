# Delivery roadmap (from the HLD, section 12)

| Phase | Duration | Deliverables | Status |
|---|---|---|---|
| 0 — Foundation | 3-4 wks | Discovery, source inventory, privacy review, geography model, UX prototype, environments, backlog | Geography model done; rest not started |
| 1 — Analytics MVP | 8-10 wks | Identity, geography, election import (2014/2018/2023), crosswalk, dashboards, maps, exports, audit | **In progress — this repo** |
| 2 — Operations | 8-10 wks | Tasks, field PWA, offline sync, issues, volunteers, events, notifications, command center | Not started (package stubs only) |
| 3 — AI copilot | 6-8 wks | AI gateway, RAG, natural language analytics, OCR, issue routing, summaries, translation, evaluation | Not started |
| 4 — Scale & content | 6-8 wks | Multi-constituency config, content approvals, public theme monitoring, warehouse, advanced reports | Not started |
| 5 — Optimization | Ongoing | Forecasting, route optimization, native app if justified, model improvements, integrations | Not started |

## Epics (HLD Table 28) and where they map

- EPIC 01 Identity, organization, geographic access → `identity/`, `config/`
- EPIC 02 Versioned geography, map layers → `geography/` (map layers not built)
- EPIC 03 Election ingestion, validation, publication → `elections/`
- EPIC 04 Booth crosswalk, historical comparison → `geography/` crosswalk + `analytics/`
- EPIC 05 Dashboards, maps, saved views, exports → `frontend/features/dashboard` (maps/exports not built)
- EPIC 06-11 (tasks, field ops, issues, volunteers, events, content) → stub packages only
- EPIC 12 Knowledge/RAG, EPIC 13 AI gateway, EPIC 14 NL analytics → not started, depends on
  the independent AI orchestration service (HLD section 4), which is out of this repo's scope
- EPIC 15 Notifications/integration → not started
- EPIC 16 Audit, privacy, retention → `audit_event` table + `common/` exist; retention jobs not built
- EPIC 17 Observability, backup/DR, FinOps → actuator/OTel wired; no dashboards/runbooks yet
