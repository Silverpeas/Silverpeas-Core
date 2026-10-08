# AGENTS.md

This file provides guidance to AI coding agents (Claude Code, Codex, Cursor, Gemini CLI, Copilot…)
when working with code in this repository. Claude Code loads it through `CLAUDE.md`, which only
imports this file.

## What this is

Silverpeas Core is the foundation of the Silverpeas collaborative web portal: a multi-module
Maven project (`org.silverpeas:core`, currently `6.5-SNAPSHOT`) that produces both a business API
(a service bus for authentication/authorization, scheduling, notification, persistence, etc.), a
set of ready-to-use business services (search, comment, workflow, chat, PdC classification,
statistics…), and a web layer (a custom MVC framework, a JAX-RS REST framework, and JSP/JSTL +
JavaScript widgets). It is a Jakarta EE application deployed on a WildFly application server.

## Common rules

These rules are shared, word for word, by the `AGENTS.md` of Silverpeas-Core, Silverpeas-Components
and Silverpeas-Looks. Change them in the three repositories at once.

### Toolchain & build environment

- The build inherits almost everything (Java release, dependency versions, surefire/failsafe wiring,
  integration-test source dirs, profiles) from the external parent POM
  `org.silverpeas:silverpeas-project`, not from this repository. Read it (in `~/.m2`) when a build
  behaviour is not explained by the POMs in this repository.
- Java 21 (`maven.compiler.release` of the parent POM) and Maven 3.9.x. The platform is Jakarta EE 10
  (`jakarta.*` namespaces everywhere) deployed on WildFly.
- Build and test in the devcontainer (`.devcontainer/`, built on the `silverpeas/silverdev:latest`
  image) whenever possible. Otherwise, if a container of that image is available on the host, start
  it if needed and run the Maven commands inside it. The image provides Java, Maven, a WildFly under
  `/opt/wildfly-for-tests/` with a `wildfly start|stop|status` helper, and the native tools some
  tests need (ffmpeg, imagemagick, ghostscript, libreoffice, swftools, pdf2json). Don't expect the
  tests to run in a bare checkout.
- Profiles and switches from the parent POM: `-DskipTests`, `-PskipMinify` (skips the JS/CSS
  minification, much faster when iterating on web assets), `-Pcoverage` (JaCoCo), `-Pdeployment`
  (attaches sources and javadoc jars), `-Plicense` (rewrites the license header of every source file).

### Tests come with any code change

Any code that is modified or added has to be covered by unit or integration tests, written
preferably **before** the code itself: either to guard the modified code against regressions, or to
validate the new code and to help to design it (its call must be simple; any new code follows the
clean code principles). This is true even for a module or a repository without any test yet: set up
its test resources instead of skipping the tests. Code that is hard to test is a design signal: fix
the design rather than giving up the test.

**Unit tests** (surefire, `src/test/`, `**/*Test.java`): JUnit 5 with
`@EnableSilverTestEnv(context = JEETestContext.class)`.
- A bean under test declared with `@TestedBean` gets its `@Inject` dependencies resolved from the
  test bean container, the missing ones being automatically mocked; declare with `@TestManagedMock`
  only the collaborators to stub.
- A module without tests yet needs `silverpeas-core-test` as a test dependency and the
  `src/test/resources/META-INF/services/org.silverpeas.kernel.BeanContainer` file (plus
  `org.silverpeas.kernel.util.SystemWrapper` when system properties are read, and
  `org/silverpeas/util/stringtemplate.properties` when templates are involved); otherwise the CDI
  bean container is loaded instead of the test one.
- To check the user notifications asked by a service without rendering any template, send them
  through `UserNotificationHelper.buildAndSend(...)`, capture the builders with
  `mockStatic(UserNotificationHelper.class)` and put the test in the package of the builders so
  that their protected properties are reachable.
- The parent POM forces the `fr`/`FR` locale and the `Europe/Paris` timezone: date and number
  assertions are locale-sensitive.

**Integration tests** (failsafe, `src/integration-test/`, `**/*IT.java`): JUnit **4** with Arquillian.
- They run only with the `integration-test` profile, activated by `-Dcontext=ci`, against an
  **already running** WildFly started with `standalone-full.xml` (Arquillian uses the
  `wildfly-remote` container). The full CI command is
  `mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci`.
- Each test deploys a purpose-built WAR assembled by a `WarBuilder*` class that declares exactly
  which classes and resources go into the archive. Any type in the signature of a managed bean
  (fields, parameters, returned and thrown types) has to be embedded: otherwise Weld silently
  ignores the bean instead of failing the deployment.
- Inside an integration test, beans are looked up with `ServiceProvider.getService(...)`, not
  injected.

### Dependency injection

Silverpeas deliberately wraps the CDI/Jakarta-EE container behind its own annotations so the IoC
implementation could be swapped without touching business code. **Prefer these over raw CDI
annotations** when writing beans (they are defined in `org.silverpeas.core.annotation`):

- `@Service` — a transactional, `@ApplicationScoped` business service (a CDI stereotype).
- `@Repository` — a persistence/data-access bean.
- `@Provider`, `@Bean`, `@WebService` — other managed-bean stereotypes.

Managed beans get their collaborators via injection points. **Unmanaged objects** (e.g. entities
loaded from a datasource, JSP-side code) cannot inject, so they obtain services through
`org.silverpeas.core.util.ServiceProvider` (`ServiceProvider.getService(Type.class)` /
`getService("name")`), a thin delegator over the kernel's `ManagedBeanProvider`. For generic
(parameterized) service types, `ServiceProvider` won't resolve them — use
`jakarta.enterprise.inject.Instance` in a managed bean instead.

**In a managed bean, never get another managed bean through `ServiceProvider`**, neither directly
nor through a static accessor delegating to it (`PdcManager.get()`, `OrganizationController.get()`,
…): `ServiceProvider` is first intended for the objects that aren't managed by CDI, and a
programmatic lookup costs more than an injection. When a dependency has to be resolved lazily
(it is used only in some cases, or it isn't always deployed), inject it with
`jakarta.enterprise.inject.Instance<T>` and call `get()` where it is needed, as
`ICalendarEventSynchronization` does with its `Scheduler` in Silverpeas Core.

Beans needing startup logic implement `org.silverpeas.core.initialization.Initialization`.

### Code conventions

- Follow the clean code principles. A constructor that would take more than four parameters is
  replaced by a builder.
- Prefer streams to classic loops, and the features of the Java release in use to the classic
  approaches: records rather than hand-written value classes, text blocks rather than concatenated
  multi-line strings, switch expressions and pattern matching rather than `if`/`else` chains with
  casts, and so on.
- Every source file carries the AGPL v3 + Silverpeas FLOSS-exception header (`license.txt` and
  `exceptions.txt` at the repository root); copy it into new files with the current year as upper
  bound, or run `mvn generate-sources -Plicense`.
- Logging goes through `SilverLogger.getLogger(this)`; each module declares its own logger
  namespace in `properties/org/silverpeas/util/logging/<name>Logging.properties`.
- Javadoc must satisfy the Java 21 doclint.
- LF line endings for all text and source files (enforced by `.gitattributes`).

### Git, CI & versioning

- Commit messages reference the Redmine tracker: `Feature #<n> ...`, `Fix bug #<n> ...`,
  `Fix vulnerability #<n> ...`. PR titles must start with `Bug #<n>`, `Feature #<n>`, `Support #<n>`
  or `[<label>]`: the CI derives the snapshot version from it.
- CI is Jenkins (`Jenkinsfile`) in the `silverpeas/silverbuild` image. It rewrites the project
  version (`versions:set`) and the parent-POM version per branch/PR before building, then runs a
  SonarCloud quality gate on PRs. Don't hand-edit versions to match the CI behaviour.

## Build & test

- **Full build + unit tests:** `mvn clean install`
- **Build a single module (with its intra-repo deps):** `mvn install -pl core-library -am`
- **Run one unit-test class/method:** `mvn test -pl core-library -Dtest=SomeClassTest#someMethod`
- **REST-API documentation (Miredot):** `-Prestapi` profile.

### Integration tests

Integration tests live under `src/integration-test/` in most modules. Their WARs are assembled by
`core-test/.../WarBuilder.java` and its per-module subclasses (`WarBuilder4LibCore`, `WarBuilder4Web`,
`WarBuilder4Comment`, …). The devcontainer ships WildFly 39.0.1 for them.

A module depending on both `silverpeas-core` and `silverpeas-core-test` has to declare
`silverpeas-core-test` in its own POM (as `core-services/chat` does), otherwise the CDI bean
container is loaded instead of the test one.

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
