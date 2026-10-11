# Combined project integration notes

## Merge decisions

- GK is the active application base. Its Java 21 / Spring Boot 4.0.8 setup, single
  application entry point, security configuration, REST-client services, and
  Dashboard, Personnel, Equipment & Assets, Vehicles, Inventory, Maintenance,
  Logistics Requests, and Readiness Reports modules are retained.
- Aditya's distinct Audit History and Notifications controllers, DTOs, services,
  templates, styles, scripts, and shared fragments are included. The GK
  dashboard and security configuration remain authoritative where Aditya has
  duplicate files. Aditya's `HomeController` root/dashboard mappings are
  superseded by GK's dashboard route; its login and access-denied views are
  served by `AuthPageController`. Aditya's alternate `SecurityConfig` was not
  activated because it changes authorization behavior and defines a
  hard-coded development user.
- Aditya's demo `application.properties` was not copied: it contains a
  development credential and sets service URLs that are not confirmed for this
  combined application. Audit History and Notifications therefore report their
  service as unconfigured until their API base URLs and contracts are supplied.
  The existing source values were `http://localhost:8082` for Audit History and
  `http://localhost:8081` for Notifications. Aditya's clients issue a GET to
  their configured base URL; no more specific endpoint contract is present in
  those clients.
- GK's Readiness Reports route is `/reports/readiness`; combined navigation
  points to that existing route. The logistics-request navigation uses GK's
  `/logistics-requests` route rather than Logesh's `/requests`.

## Active integration status

The following modules are now part of Combined's active source tree and are routed through the application:

- Approvals: active controller at `src/main/java/com/team11/defense_logistics/controller/ApprovalController.java`; active model at `src/main/java/com/team11/defense_logistics/model/Approval.java`; active service at `src/main/java/com/team11/defense_logistics/service/ApprovalService.java`; templates at `src/main/resources/templates/approvals/list.html` and `src/main/resources/templates/approvals/form.html`; routes `/approvals` and `/approvals/new`.
- Resource Allocation: active controller at `src/main/java/com/team11/defense_logistics/controller/ResourceAllocationController.java`; active model at `src/main/java/com/team11/defense_logistics/model/ResourceAllocation.java`; active service at `src/main/java/com/team11/defense_logistics/service/ResourceAllocationService.java`; template at `src/main/resources/templates/allocations/list.html`; route `/allocations`.
- Movements: active controller at `src/main/java/com/team11/defense_logistics/controller/MovementController.java`; active model at `src/main/java/com/team11/defense_logistics/model/Movement.java`; active service at `src/main/java/com/team11/defense_logistics/service/MovementService.java`; template at `src/main/resources/templates/movements/list.html`; route `/movements`.

Navigation was updated in `src/main/resources/templates/fragments/sidebar.html` and `src/main/resources/templates/dashboard.html` to expose all three modules using the existing app conventions.

These active modules use in-memory state stored in the service instance; they are not backed by a database or external API and will reset when the application restarts. The work is therefore active in the MVC app, but it still carries the same unresolved persistence/blocker that the original Logesh flow was designed around.

## Preserved but not activated

Logesh contains alternate same-package implementations for the dashboard and
core modules, additional API controllers, and approval, movement, and resource
allocation screens. The alternate core controllers/services and templates
conflict with GK's same-named classes, template names, and route behavior.
Logesh's additional workflow services also use in-memory maps and depend on
Logesh's alternate personnel, vehicle, maintenance, and request services; they
cannot safely be connected to GK's REST-backed services without changing their
business behavior or substituting fake state.

### Approvals module

Files preserved in the project archive for review and comparison:

- `integration-conflicts/logesh/java/com/team11/defense_logistics/model/Approval.java.original`
- `integration-conflicts/logesh/java/com/team11/defense_logistics/service/ApprovalService.java.original`
- `integration-conflicts/logesh/java/com/team11/defense_logistics/controller/ApprovalController.java.original`
- `integration-conflicts/logesh/java/com/team11/defense_logistics/controller/ApprovalApiController.java.original`
- `integration-conflicts/logesh/templates/approvals/form.html.original`
- `integration-conflicts/logesh/templates/approvals/list.html.original`

The Approval workflow relies on `LogisticsRequestService` to validate that a
request exists and is still `Pending` before recording `Approved` or `Rejected`.
The service stores approval records in an in-memory `LinkedHashMap` keyed by a
sequence number; it does not use a database repository or persistent table. The
route is `/approvals`, and the template flow matches the Logesh workflow, but it
is not activated in Combined because GK's active request flow already owns the
request lifecycle and its status semantics.

### Resource Allocation module

Files preserved in the project archive for review and comparison:

- `integration-conflicts/logesh/java/com/team11/defense_logistics/model/ResourceAllocation.java.original`
- `integration-conflicts/logesh/java/com/team11/defense_logistics/service/ResourceAllocationService.java.original`
- `integration-conflicts/logesh/java/com/team11/defense_logistics/controller/ResourceAllocationController.java.original`
- `integration-conflicts/logesh/java/com/team11/defense_logistics/controller/ResourceAllocationApiController.java.original`
- `integration-conflicts/logesh/templates/allocations/list.html.original`

This module depends on GK-like request and vehicle services for validation but
uses Logesh-specific in-memory state to track allocations. It validates that a
request is `Approved`, that the vehicle is `Available`, that no active
maintenance or active movement conflicts exist, and that only one active
allocation is present per request or vehicle. It also calls vehicle reconciliation
methods after release or cancellation. Because Combined keeps GK's existing
rest-backed request and vehicle services as the active source of truth, the
Logesh allocation logic remains preserved as a reference only.

### Movements module

Files preserved in the project archive for review and comparison:

- `integration-conflicts/logesh/java/com/team11/defense_logistics/model/Movement.java.original`
- `integration-conflicts/logesh/java/com/team11/defense_logistics/service/MovementService.java.original`
- `integration-conflicts/logesh/java/com/team11/defense_logistics/controller/MovementController.java.original`
- `integration-conflicts/logesh/java/com/team11/defense_logistics/controller/MovementApiController.java.original`
- `integration-conflicts/logesh/templates/movements/list.html.original`

The Movement workflow depends on `VehicleService` and `ResourceAllocationService`
for ownership- and allocation-based validation. It stores records in memory, enforces
status transitions (`Scheduled` -> `In Transit` -> `Completed`, plus cancellation),
and prevents invalid origin/destination changes or mismatched vehicles. It is a
useful module design reference but not a safe active addition to Combined without
additional compatibility work and a confirmed backend contract.

The Logesh Java sources and templates, its POM, and its non-secret memory and
Oracle profile files are retained under `integration-conflicts/logesh/` with an
`.original` suffix. They are reference copies, not active application sources.
Logesh's `application.properties` was not copied because it contains a
development security credential. Its `SecurityConfig` is likewise not active;
GK's form-login and authorization rules are retained.

All three POMs declare Spring Boot 4.0.8 and Java 21. GK's dependencies cover the
active application. Logesh's JPA and Oracle driver dependencies are not added
to the active POM: the Logesh services use in-memory collections rather than
repositories, and enabling its database configuration would introduce a
separate, unverified persistence setup. The Oracle profile reference is kept
for review; it still requires externally supplied `DB_USERNAME` and
`DB_PASSWORD` values and an Oracle database.

## Configuration and validation status

`src/main/resources/application.properties` remains GK's configuration.
Existing GK microservice URL defaults and security behavior are unchanged.
No teammate credentials, database settings, or unverified service URLs were
made active. The original `GK/`, `Aditya/`, and `logesh/` folders were not
modified.

Builds, tests, and application startup were intentionally not run, as requested.
