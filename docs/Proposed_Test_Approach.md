# Proposed Performance Test Approach — ShopLite

## 1. Executive Summary (Business View)
ShopLite is an e-commerce application (React + Spring Boot + PostgreSQL) released every 3–4 months with 2-week sprints.
Today the team has no established performance testing process, no formal NFR/SLO, and constrained environments (shared TEST, restricted PREPROD).
The team observes cart/checkout slowing down for larger carts, while DevOps reports increasing CPU/RAM even under low traffic.

My approach is pragmatic and lightweight, starting with **baseline-driven SLOs**:
- establish **baseline + trends** (objective measurement),
- implement **fast sanity** checks for regression detection,
- run **targeted experiments** around cart size (1/10/50 items),
- evolve to **capacity/scale** testing when PREPROD access/process is available.

The initial goal is to protect delivery cadence by preventing regressions and shortening “root-cause time”, rather than maximizing throughput on TEST.

---

## 2. Context, Environments, Constraints
### 2.1 Product Scope (current)
Critical user journeys (GUI + REST):
- Browse catalog
- Add-to-cart
- Checkout / place order

> Note: The focus is backend/API performance. UI navigation is used only to define user journeys (not to measure UI performance). Guest checkout only: login and payment flows are out of scope.

### 2.2 Environments (practical implications)
- DEV: unstable (not suitable for perf)
- TEST: shared, resource-limited (2 vCPU / 2 GB, single app container) → **sanity/baseline/targeted low-impact tests**
- PREPROD: production-like, auto-scaling, DevOps-only, contains production data → **later capacity/scale via DevOps process**
- PROD: not launched yet (planned horizontal scaling)

### 2.3 Key constraints / risks
- TEST is shared → avoid disruptive loads; use agreed windows. **It will be the primary environment for the initial iteration**.
- PREPROD has production data → strict control of execution windows and data strategy via DevOps to prevent production data corruption.
- No NFR/SLO → start baseline-driven and refine with stakeholders later.

---

## 3. Testing Goals, Objectives and Scope Focus (v1)
### 3.1 Must-have outcomes (first iteration)
- Establish **Baseline** for key journeys on TEST.
- Implement **Sanity regression signal** (repeatable, low cost, regular).
- Investigate and **Quantify cart-size impact** (add-to-cart + checkout for 1/10/50 items).
- Capture minimal infra/DB metrics during runs to connect symptoms to likely causes.
- Define initial SLIs/SLO v0 and measurement method; refine after several baselines.

### 3.2 Nice-to-have (next iterations)
- Soak (low traffic, longer time) to validate CPU/RAM growth behavior.
- Capacity/scale tests in PREPROD (via DevOps) for target traffic + autoscaling behavior validation.
- Profiling (JVM) if results indicate leaks/inefficient queries.

---

## 4. Scope: What We Test First
### 4.1 Critical journeys (v1)
- **Browse catalog** (list/search/pagination) — representative read workload  
- **Add-to-cart** — repeated adds; focus on cart size growth  
- **Checkout / place order** — primary pain point; measure end-to-end time and errors  

### 4.2 Primary data volume dimension
For add-to-cart and checkout:
- small cart: **1 item**
- medium cart: **10 items**
- large cart: **50 items** (or the largest stable value feasible once endpoint/payload details are clarified)

This directly validates the stakeholder concern that performance degrades with larger product counts.

### 4.3 Out of scope (current iteration)
- **Authenticated user flows** (login/registration/account features)  
- **Payment processing** (explicitly excluded by the current project scope)  
- **Dedicated UI performance measurement** (may be added later as a separate workstream once backend baselines are established)

> Note: API endpoints and payload schemas are assumed; final request paths, payload structures, and correlation rules will be finalized once a Swagger definition or Postman collection becomes available.

## 5. What We Measure (SLIs) and Initial SLO v0
### 5.1 SLIs (per transaction - browse/add-to-cart/checkout)
- Latency: p50 / p95 / p99  
- Error rate: % failures (HTTP 4xx/5xx/timeouts) + functional assertion failures  
- Throughput: RPS (requests/sec) / TPM (transactions/min)
- Saturation signals (where available): queued requests, thread pool utilization, DB connection pool pressure

### 5.2 Minimal system/DB metrics
- App container: CPU, memory (RSS/heap if available)  
- PostgreSQL: connections, slow queries (if available)  
- Optional: GC pause/time (if exposed)  

### 5.3 Initial SLOs (baseline-driven; for early regression control)
For sanity runs on TEST:
- error rate < 1% (excluding known functional test-data issues)  
- checkout p95 not worse than baseline by > 10–15% (tunable after 2–3 baselines)  
- no sustained CPU/memory growth beyond baseline during short steady state  

> Note: These thresholds are intended as an early warning mechanism; they will be refined once stakeholders agree on expected user experience and once PREPROD becomes usable for more realistic validation.

---

## 6. Test Types and Cadence
### 6.1 **Performance sanity (regression signal)**
- Purpose: detect regressions early with minimal disruption.
- TEST, low load (1–5 VU), 5–10 minutes
- nightly and/or on demand; optionally PR-based if stable
- output: pass/fail trend + lightweight report

### 6.2 **Baseline / benchmark**
- Purpose: stable reference for trend comparison.
- TEST, controlled window, fixed parameters (TBD)
- once per sprint or before release candidate

### 6.3 **Targeted investigations (cart size 1/10/50 and checkout)**
- Purpose: reproduce and quantify suspected degradation with larger carts.
- TEST, low-to-moderate load (within constraints)
- output: comparison across cart sizes + correlated system/DB metrics

### 6.4 **Soak (later)**
- Purpose: validate CPU/RAM growth over time under low traffic (to address DevOps concern).
- ideally PREPROD or dedicated perf environment; otherwise controlled TEST window
- 1–2 hours initially; extend later up to 8 hours if stable

---

## 7. Tooling, Test Design, and Observability
### 7.1 Load generation
- Gatling (Java DSL) for API-level performance testing of backend flows.
- Optional later: a separate UI performance workstream (e.g., Sitespeed.io) once backend baselines are stable.

### 7.2 Test design principles (maintainability)
- Modular structure: common config, reusable request components, scenario controllers; consistent naming and transaction labels.
- Configuration via properties (baseUrl, timeouts, threads, ramp-up, duration); pipeline-friendly (`-J`, distributed `-G` if needed).
- Parameterization: product IDs, cart sizes, and guest data via CSV/variables.
- Correlation: extract dynamic identifiers (e.g., cartId, orderId) via JSON Extractor / regex as required by the API.
- Assertions: validate critical response properties to prevent “fast but wrong” results.
- Pacing: timers/think time to approximate realistic user behavior.
- Repeatability: unique guest data and collision avoidance to ensure repeatable runs.

### 7.3 Minimum viable observability (during test execution)
- From JMeter: latency percentiles, throughput, response codes, assertion failures.
- From existing monitoring (where available): app container CPU/memory; PostgreSQL connections and slow-query indicators.
- Optional later: push test metrics to InfluxDB/Prometheus and correlate in Grafana (including a baseline vs current view).

---

## 8. Reporting and Communication
### 8.1 Standard deliverables per run
- Execution context: build/version, environment, test window, configuration parameters.
- Results summary per transaction: p95/p99 latency, error rate, throughput.
- Baseline comparison: current vs baseline (trend / regression signal).
- Observability snapshot aligned with the steady-state window (infra + DB metrics) — I typically attach Grafana screenshots if dashboards are available; otherwise via the best available monitoring source.
- Findings: what changed, hypotheses, recommended next steps.
- Artifacts: HTML report, raw JTL/CSV, test plan/config version; monitoring evidence (screenshots/links), if available.

### 8.2 Stakeholder focus
- Product Owner: “better/worse” trend and release risk (fast signal).
- QA: repeatable baseline/sanity runs to remove subjectivity.
- DevOps: explicit environment differences; alignment on execution windows and metrics.

---

## 9. Integration into Agile Rituals
- Sprint Planning: flag perf-risk changes during planning (checkout/cart/backend/DB).
- DoD for perf-risk stories: sanity executed; no regression vs baseline or documented exception.
- Daily triage (as needed): if sanity fails: quick triage with Dev + QA + DevOps as needed.
- Sprint Review: show 1-slide trend summary (baseline vs current).
- Retrospective: refine cadence, improve data stability, reduce flakiness.

---

## 10. Assumptions and Open Questions
### 10.1 Assumptions:
- Checkout/place order is accessible to guests and does not require authentication.
- Critical APIs exist for catalog, cart operations, and order placement as stated in the brief.
- Any required “user data” can be provided as generated test payloads or from CSV to ensure uniqueness and repeatability.

### 10.2 Open questions:
- What is the expected peak traffic / business targets for future PROD?
- What monitoring is currently available (Prometheus/Grafana, DB metrics, logs)?
- Is there a safe test-data strategy which DevOps could provide for PREPROD (test tenant / read-only mode / cleanup process) given production data constraints?
- Are there known data constraints (e.g., limited products, DB resets, shared fixtures)?

---

## 11. First 2-Week Execution Plan
### 11.1 Week 1:
- Align on scope and TEST windows.
- Build JMeter skeleton for browse/add-to-cart/checkout; stabilize data.
- Run initial low-load baseline + capture metrics.

### 11.2 Week 2:
- Run cart size variants (1/10/50); refine assertions/correlation.
- Establish sanity configuration + reporting template.
- Deliver findings on cart-size sensitivity and CPU/RAM growth signals with next-step recommendations.
