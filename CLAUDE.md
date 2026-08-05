# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

Silverpeas Core is the foundation of the Silverpeas collaborative web portal: a multi-module
Maven project (`org.silverpeas:core`, currently `6.5-SNAPSHOT`) that produces both a business API
(a service bus for authentication/authorization, scheduling, notification, persistence, etc.), a
set of ready-to-use business services (search, comment, workflow, chat, PdC classification,
statistics…), and a web layer (a custom MVC framework, a JAX-RS REST framework, and JSP/JSTL +
JavaScript widgets). It is a Jakarta EE application deployed on a WildFly application server.

## Build & test

The build inherits almost everything (Java version, dependency versions, failsafe/surefire wiring,
integration-test source dirs) from the external parent POM `org.silverpeas:silverpeas-project`, not
from this repo. Assume Java 17 and Maven 3.9.x (as pinned in `.devcontainer/devcontainer.json`).

To build and test the project, use the devcontainer whenever possible. Otherwise, if a container 
from the `silverpeas/silverdev:latest` Docker image is available on the host, starts it (if not 
already done) and uses it.

- **Full build + unit tests:** `mvn clean install`
- **Build a single module (with its intra-repo deps):** `mvn install -pl core-library -am`
- **Run one unit-test class/method:** `mvn test -pl core-library -Dtest=SomeClassTest#someMethod`
- **Skip tests:** add `-DskipTests`

### Integration tests (Arquillian + WildFly)

Integration tests live under `src/integration-test/` in most modules (separate from `src/test/`).
They are gated behind the `integration-test` Maven profile, which activates **only** when the
property `-Dcontext=ci` is set (see each module's `pom.xml`). They run against a **real running
WildFly** instance (the CI does `standalone.sh -c standalone-full.xml` before the build — see
`Jenkinsfile`). Each test deploys a purpose-built WAR assembled by a `WarBuilder*` class
(`core-test/.../WarBuilder.java` and per-module subclasses such as `WarBuilder4LibCore`,
`WarBuilder4Web`, `WarBuilder4Comment`, …) that declares exactly which classes/resources go into
the test archive.

These tests also require native tools on the PATH (ffmpeg, imagemagick, ghostscript, libreoffice,
swftools, pdf2json). Do **not** expect integration tests to run in a bare checkout — use the dev
container (`.devcontainer/`, in which is installed WildFly 34.0.1 and these tools and provides a
`wildfly start|stop|status` helper) or `silverpeas/silverdev:latest` image. The full CI command is 
roughly: `mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci`.

## Module layout & build order

Modules build in the order declared in the root `pom.xml`; dependencies flow downward:

- `core-configuration` — configuration / settings infrastructure.
- `core-api` — the public API: interfaces, model types, and the DI/stereotype annotations. Depends
  on the **external** `org.silverpeas.kernel:silverpeas-kernel` library, which provides the IoC
  abstraction (`ManagedBeanProvider`).
- `core-test` / `core-web-test` — test support (base classes, `WarBuilder`s, mocks) reused by other
  modules' integration tests. Published as test-jars.
- `core-jcr` — JCR (Jackrabbit Oak) repository integration for document storage.
- `core-library` (`artifactId: silverpeas-core`) — the bulk of the business-logic implementations.
- `core-services` — a POM aggregator of independent business services, each its own submodule:
  `chat`, `comment`, `contact`, `documentTemplate`, `importExport`, `mylinks`, `pdc`,
  `personalOrganizer`, `questioncontainer`, `search`, `sharing`, `silverstatistics`, `tagcloud`,
  `viewer`, `workflow`.
- `core-rs` (`silverpeas-core-rs`) — the JAX-RS REST-services framework.
- `core-web` (`silverpeas-core-web`) — the web/MVC layer (Java side).
- `core-war` (`silverpeas-core-war`) — the WAR: JSP/JSTL views (~500 JSPs) and JavaScript widgets
  (AngularJS for older components, VueJS for newer ones) under `core-war/src/main/webapp/`.

All Java code is under the `org.silverpeas.core` package (plus a vendored `org.monte.media`).

## Dependency injection — the key architectural convention

Silverpeas deliberately wraps the CDI/Jakarta-EE container behind its own annotations so the IoC
implementation could be swapped without touching business code. **Prefer these over raw CDI
annotations** when writing beans:

- `@Service` — a transactional, `@ApplicationScoped` business service (a CDI stereotype).
- `@Repository` — a persistence/data-access bean.
- `@Provider`, `@Bean`, `@WebService` — other managed-bean stereotypes.
  (defined in `core-api/.../org/silverpeas/core/annotation/`)

Managed beans get their collaborators via injection points. **Unmanaged objects** (e.g. entities
loaded from a datasource, JSP-side code) cannot inject, so they obtain services through
`org.silverpeas.core.util.ServiceProvider` (`ServiceProvider.getService(Type.class)` /
`getService("name")`), a thin delegator over the kernel's `ManagedBeanProvider`. For generic
(parameterized) service types, `ServiceProvider` won't resolve them — use
`jakarta.enterprise.inject.Instance` in a managed bean instead.

Beans needing startup logic implement
`org.silverpeas.core.initialization.Initialization`.

## CI / versioning notes

- CI is Jenkins (`Jenkinsfile`) running in the `silverpeas/silverbuild` Docker image; it rewrites
  the version (`versions:set`) and the parent-POM version per branch/PR before building, then runs a
  SonarCloud quality gate on PRs against the `Silverpeas` org. Don't hand-edit versions to match CI
  behaviour.
- Releases/deploys use the `deployment` profile (attaches sources + javadoc jars). REST-API docs
  use the `restapi` profile (Miredot).
- Every source file carries the AGPL-v3 + Silverpeas FLOSS-exception license header; keep it on new
  files (copy an existing header).
