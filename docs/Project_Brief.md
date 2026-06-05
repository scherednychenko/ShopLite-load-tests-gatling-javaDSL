# ShopLite – Project Brief

> _This is an anonymized take-home assignment brief. Company name, the product name, and personal details have been replaced with neutral placeholders. Stakeholder names in section 5 are fictional, as noted in the original brief._

## 1. Project Context
ShopLite is an e-commerce application developed using Agile methodology that allows:
- browsing the product catalog (GUI and REST API),
- adding products to the cart and placing orders,
- basic user interface (no login or payment).

The team consists of 6 people:
- 3 developers (frontend, backend, full-stack),
- 1 functional tester,
- 1 DevOps,
- 1 Product Owner.

Work is carried out in 2-week sprints, with releases planned every 3-4 months.
The roadmap anticipates development for the next 2 years (payments, user accounts, admin panel, ERP integration).
The team operates independently but uses a shared CI/CD and monitoring infrastructure.

---

## 2. Current Situation
The project has never conducted performance testing.
Until now, the focus has been solely on functional and regression testing.

In recent sprints, testers have begun reporting that the cart add and checkout processes **seem slower**, especially with larger product counts.
These reports are **subjective** and unsupported by any measurements.

At the same time, DevOps noticed that **CPU and RAM usage** increases with application growth, even with low traffic.
It's unclear whether this is normal or a symptom of problems.
The following are missing:
- historical data and metrics,
- a dedicated performance testing environment,
- observability and reporting standards,
- formal **non-functional requirements**.

---

## 3. System environments

### DEV
- Local or containerized developer environment,
- Unstable, frequently restarted,
- 1 vCPU / 1 GB RAM, no persistent database.

### TEST
- shared test environment (functional + performance),
- 2 vCPUs / 2 GB RAM, single application container,
- shared with other tests, **used in this task**.

### PREPROD
- production-like integration environment (4 vCPU / 8 GB RAM),
- supports acceptance testing and is used for hot fix testing before deployment to Prod,
- **automatically scales** as needed,
- available only for DevOps - the environment contains production data.

### PROD (planned)
- **not yet launched**,
- plan: 4–8 vCPUs, 16 GB RAM, 2 replicas,
- **horizontal scaling**,  

---

## 4. Architecture (technical overview)
The application consists of:
- **frontend** – React/TypeScript (GUI),
- **backend** – Spring Boot (REST API),
- **database** – PostgreSQL,
- **launch** – Docker Compose (frontend + backend + DB).

---

## 5. Team Voice (fictional stakeholders)

> **Product Owner**
> "I'd like to know if new features are slowing down the app. But we can't delay releases by a week just to test."

> **DevOps Engineer**
> "We have one server in the test environment. Only PreProd can scale.
> I'd like the test plan to account for these differences."

> **Functional Tester**
> "Sometimes checkout is slower, but I don't know how to measure it.
> I'd like to have a simple way to see whether the next version is improving or worsening."

---

## 6. Candidate Task
You are joining as our first Performance Engineer.

a). **Describe your approach** in the `Proposed_Test_Approach.md` file:
- what, when, and how to test,
- what data and metrics to collect,
- how to report results,
- how to incorporate the testing process into the Agile team's rituals,
- what SLIs/SLOs you propose and how you want to measure them.

b). The document should be your proposal.
What matters is consistency, realism, and the ability to justify your decision.

c). Submit your proposed testing approach for review before the technical interview.

d). Prepare for a 10-minute presentation of your approach.
You can expect questions about the rationale behind the steps, risks, and trade-offs.

---

## 7. What we will assess
- The ability to **read the project context** and understand the constraints of the environment.
- The ability to design the **first performance testing process**.
- Consistency of approach and realism with respect to the project and the client.
- Awareness of the proposed metrics and reasoning.
- The ability to **communicate and argue technically** (can you defend your decisions).
- The ability to **prioritize** – what is a "must have" and what is a "nice to have."

---
