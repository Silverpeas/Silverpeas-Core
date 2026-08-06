# Silverpeas Core

__Silverpeas Core__ is the foundation of the [Silverpeas](https://www.silverpeas.org) Collaborative
Web Portal.

It provides both a business and a web APIs for the applications dedicated to run within the portal.
Technically, it is a multi-module Maven project (`org.silverpeas:core`) producing the libraries, the
REST/Web services, the configuration files and the WAR of JSP/JSTL views and JavaScript widgets that
make up the runtime kernel of a Silverpeas instance. It is a Jakarta EE application, deployed within
a [WildFly](https://www.wildfly.org) application server.

* [Overview](#overview)
* [The business core](#the-business-core)
* [The business services](#the-business-services)
* [The web core](#the-web-core)
* [Project layout](#project-layout)
* [Technology stack](#technology-stack)
* [Architectural conventions](#architectural-conventions)
* [Building the project](#building-the-project)
* [Testing](#testing)
* [Continuous integration and versioning](#continuous-integration-and-versioning)
* [The Silverpeas ecosystem](#the-silverpeas-ecosystem)
* [Contributing](#contributing)
* [License](#license)

## Overview

Silverpeas is a collaborative portal in which the collaboration is performed through *applications*
(also named *components*) instantiated within *collaborative spaces*. Silverpeas Core provides
everything such applications require to exist and to interoperate:

* a **service bus** of transverse services (authentication, authorization, notification,
  scheduling, indexation, persistence, …) upon which any application can be built,
* a set of **ready-to-use business services** (search, comment, workflow, classification, …) that an
  application can plug in to enrich its own functionalities,
* a **web layer** (MVC framework, REST-services framework, GUI widgets and page layouts) providing
  both the look and feel of the portal and the mechanisms for the applications to expose their own
  web pages and web resources.

All the Java code is located under the `org.silverpeas.core` package (plus a vendored copy of
`org.monte.media`).

## The business core

The business API defines a framework and a bus through which the services can interact with each
other. This bus is made up of transverse services for:

* authenticating and authorizing users and groups of users,
* scheduling tasks in the time,
* notifying about actions performed by an application or a business service,
* notifying the users,
* managing the life-cycle of business and technical services,
* invoking a service by a functional or business interface,
* logging information,
* ...

More concretely, the core provides among others:

* **Administration** (`org.silverpeas.core.admin`): the organizational model of the portal — users
  and groups of users, user domains (with their drivers: internal SQL, LDAP/Active Directory,
  Google Workspace, SCIM), spaces, application instances, right profiles and quotas.
* **Authentication and security** (`org.silverpeas.core.security`): authentication protocols (SQL,
  LDAP, CAS), password policies and credential verifiers, user sessions, security tokens (both
  synchronizer and API tokens), encryption services, and HTML sanitization.
* **Authorization** (`org.silverpeas.core.security.authorization`): the accessors deciding whether a
  user can access a space, an application instance, a node, a publication or an attachment.
* **Contributions** (`org.silverpeas.core.contribution`): the abstract model of any content produced
  by a user in an application — contribution identifiers and locators, contribution life-cycle
  events (creation, modification, deletion, move), attachments and documents, XML/form-based
  contents, contribution tracking and rating.
* **Persistence** (`org.silverpeas.core.persistence`): a JDBC layer with a fluent SQL query API and
  a JPA-based layer (entities, repositories, `Transaction` support) working over the supported
  RDBMS. Data-source and JCR-related types are also defined here.
* **Notification** (`org.silverpeas.core.notification`): the user notifications (with their channels:
  SMTP, popup, silverpeas server, …), the system notifications (a publish/subscribe bus of business
  events) and the server-sent events (SSE) mechanism for pushing information to the browsers.
* **Scheduler** (`org.silverpeas.core.scheduler`): the scheduling of jobs in time, backed by
  [Quartz](https://www.quartz-scheduler.org) or by a lighter internal implementation.
* **Calendar and reminders** (`org.silverpeas.core.calendar`, `.reminder`): a full iCalendar-based
  calendar engine (events, recurrences, occurrences, attendees, iCal4J import/export) and the
  reminders attached to any contribution.
* **Internationalization** (`org.silverpeas.core.i18n`): multilingual contributions and multilingual
  UI resources.
* **Templating** (`org.silverpeas.core.template`): a templating engine based on
  [StringTemplate](https://www.stringtemplate.org) used for emails, notifications and view fragments.
* **Media and documents** (`org.silverpeas.core.media`, `.io`, `.viewer`): media metadata,
  thumbnails, file repositories and conversions.
* **Indexation and search** (`org.silverpeas.core.index`): the indexation engine based on
  [Apache Lucene](https://lucene.apache.org) and the search API over it.
* **Caching** (`org.silverpeas.core.cache`): request, session and application caches.
* **Initialization** (`org.silverpeas.core.initialization`): the bootstrap mechanism by which a
  service declares some setting up to perform at Silverpeas startup.

## The business services

Besides and atop of the core foundation of Silverpeas, this project provides also a set of
pre-defined services ready to be used by the applications:

* chat engine,
* search engine,
* commenting engine,
* classification engine,
* statistics engine,
* workflow engine,
* ...

Each of them is an independent Maven module under `core-services`:

| Module              | Purpose                                                                                    |
|---------------------|--------------------------------------------------------------------------------------------|
| `chat`              | the chat engine, interfacing Silverpeas with an XMPP server and its REST API                 |
| `comment`           | the commenting engine: comments on any contribution, with their own notifications and indexes|
| `contact`           | the contacts (directory entries) service                                                     |
| `documentTemplate`  | the document templating engine (documents produced from predefined templates)                |
| `importExport`      | the import/export engine of contributions (XML descriptors, ZIP archives, …)                 |
| `mylinks`           | the user's personal links (favourites) API                                                   |
| `pdc`               | the PdC (*Plan de Classement*): the taxonomy/classification engine of the contributions      |
| `personalOrganizer` | the personal calendar service (diary, todos, journal)                                        |
| `questioncontainer` | the questions/answers containers used by surveys, quizzes and polls                          |
| `search`            | the search engine: query building, result ranking, and the search over the indexed contents  |
| `sharing`           | the sharing of contributions by tickets (public, time-limited links)                         |
| `silverstatistics`  | the statistics service: volume, access and connection statistics                             |
| `tagcloud`          | the tag cloud service                                                                        |
| `viewer`            | the document viewer/preview service (conversion of documents to images/PDF/Flash-less viewers)|
| `workflow`          | the workflow engine: process models, instances, states, actions, users roles and forms       |

## The web core

The web API defines a GUI layout, a core Web framework, and it provides a set of GUI services
written both in Java and in Javascript. It is built upon the following mechanisms:

* the web navigation with the web pages is motorized by a custom MVC framework along with JSP/JSTL 
  and Javascript files,
* the dynamics of the inner parts of web pages (id est, the communication of widgets with 
  Silverpeas) is done by requesting asynchronously REST-style web services,
* an API for external applications and tools is provided as a set of REST-style web services.

For doing, the Web Core provides:

* a MVC framework (whose the new version uses some JAX-RS annotations and provides its own),
* a REST-style web services framework based upon JAX-RS,
* a set of Web components built atop of AngularJS 2 (for the older ones) and VueJS (for the newer
  ones),
* a set of plain-old Javascript functions and services,
* a set of reusable HTML canvas and widgets written in JSP and in JSTL,
* a layout of HTML parts to build Web pages or some parts of them (in JSP/JSTL),
* ...

The REST-style web services are all published under the `/services` base path of the Silverpeas web
application (see `SilverpeasWebResource.BASE_PATH`). A web service is written by extending
`RESTWebService`; the authentication (session-based, basic or bearer token) and the authorization of
the requester are then automatically performed by the framework
(`UserPrivilegeValidation`, `WebAuthenticationValidation`, `WebAuthorizationValidation`).

## Project layout

The modules are built in the order they are declared in the root `pom.xml`; the dependencies flow
downward:

| Module            | Artifact                        | Description                                                                                        |
|-------------------|---------------------------------|----------------------------------------------------------------------------------------------------|
| `core-configuration` | `silverpeas-core-configuration` | all the configuration of Silverpeas: properties, settings, string templates, XML descriptors of the applications, data to bootstrap, and the SQL migration scripts (H2, PostgreSQL, Oracle, MS-SQLServer) |
| `core-api`        | `silverpeas-core-api`           | the public API: interfaces, model types, exceptions and the DI stereotype annotations. Depends on the external `org.silverpeas.kernel:silverpeas-kernel` library which provides the IoC abstraction |
| `core-test`       | `silverpeas-core-test`          | the test support (base classes, `WarBuilder`s, rules, mocks) reused by the other modules; published as a test-jar |
| `core-jcr`        | `silverpeas-core-jcr`           | the JCR (Apache Jackrabbit Oak) repository integration for the document storage, along with its WebDAV access and its security layer |
| `core-library`    | `silverpeas-core`               | the bulk of the business-logic implementations of the API                                            |
| `core-services`   | (aggregator)                    | the independent business services, each of them being a submodule (see above)                        |
| `core-rs`         | `silverpeas-core-rs`            | the JAX-RS REST-services framework of Silverpeas                                                     |
| `core-web-test`   | `silverpeas-core-web-test`      | the test support dedicated to the web layer                                                          |
| `core-web`        | `silverpeas-core-web`           | the web/MVC layer (Java side): controllers, request routers, servlets, filters, session management, portlets, look & feel |
| `core-war`        | `silverpeas-core-war`           | the WAR: about 500 JSP/JSTL views, the tag libraries, the CSS/graphical charter and the JavaScript widgets |

Some noticeable non-module resources:

* `.devcontainer/` — the definition of the development container (based upon the
  `silverpeas/silverdev` image) with WildFly and all the required native tools already installed;
* `Jenkinsfile` — the CI pipeline (build, quality gate, release);
* `src/site/` — the Maven site of the project;
* `license.txt`, `silverpeas-license.txt`, `CDDLv1.0.txt`, `exceptions.txt` — the licensing terms;
* `CLAUDE.md` — the guidance for the Claude Code AI assistant.

## Technology stack

* **Java 17** and **Maven 3.9.x**. The build inherits the Java version, the dependency versions and
  the surefire/failsafe wiring from the external parent POM `org.silverpeas:silverpeas-project`.
* **Jakarta EE 10** (CDI, JPA, JTA, JAX-RS, Servlet, JSP/JSTL, Mail, Bean Validation) running on
  **WildFly 34.0.1.Final**.
* **Hibernate** as the JPA provider, over **PostgreSQL**, **Oracle**, **MS-SQLServer** or **H2**.
* **Apache Jackrabbit Oak** as the JCR implementation for the documents (with a segment-based or a
  MongoDB-based node store).
* **Apache Lucene** for the indexation and the full-text search.
* **Quartz** for the scheduling.
* **Apache Tika**, **PDFBox**, **Apache POI**, **iText**, **metadata-extractor**, **im4java**
  (ImageMagick), **JODConverter** (LibreOffice) for the document and media processing.
* **Jackson** for the JSON serialization, **JAXB** for the XML binding.
* **jQuery**, **AngularJS** (older widgets) and **VueJS** (newer widgets) on the browser side, along
  with CKEditor, FlowPlayer and the Silverpeas own JavaScript library (`silverpeas-*.js`).
* **JUnit 5**, **Mockito**, **Hamcrest**, **Weld-JUnit5**, **DbSetup**/**DbUnit**, **GreenMail**,
  **OpenDJ** and **Arquillian** for the tests.

## Architectural conventions

### Dependency injection

Silverpeas deliberately wraps the CDI/Jakarta EE container behind its own annotations so that the
IoC implementation could be changed without impacting the business code. **Prefer these annotations
over the raw CDI ones** (they are all defined in `org.silverpeas.core.annotation`):

* `@Service` — a transactional, `@ApplicationScoped` business service (a CDI stereotype),
* `@Repository` — a persistence/data-access bean,
* `@Provider`, `@Bean`, `@WebService` — the other managed-bean stereotypes.

The managed beans get their collaborators by injection. **Unmanaged objects** (typically the
entities loaded from a data source, or the code within the JSPs) cannot inject anything: they obtain
their services through `org.silverpeas.core.util.ServiceProvider`
(`ServiceProvider.getService(Type.class)` or `ServiceProvider.getService("name")`), a thin delegator
over the kernel's `ManagedBeanProvider`. For generic (parameterized) service types, `ServiceProvider`
isn't able to resolve them: use then a `jakarta.enterprise.inject.Instance` injection point within a
managed bean.

### Initialization

A bean requiring some setting up at the startup of Silverpeas has just to implement
`org.silverpeas.core.initialization.Initialization`; it will be automatically invoked by the
initialization mechanism (the order can be tuned by overriding `getPriority()`).

### Configuration

All the settings and the localized resources are gathered in the `core-configuration` module and are
accessed through the `SettingBundle` and `LocalizationBundle` objects of the Silverpeas Kernel
library. Never read a properties file directly.

### Persistence

Two complementary approaches coexist:

* the **JPA layer** — entities extending `BasicJpaEntity`/`SilverpeasJpaEntity` and repositories
  extending the Silverpeas JPA repository types, all of them being transactional through
  `Transaction` (or the `@Transactional` support brought by the `@Service` stereotype);
* the **JDBC layer** — `JdbcSqlQuery`, a fluent API to write SQL queries, used for the older or the
  performance-sensitive parts of the code.

The database schema is created and upgraded by the SQL/Groovy migration scripts and descriptors in
`core-configuration/src/main/config/migrations`, applied by the Silverpeas Installer.

### Web

* The **MVC framework** (`org.silverpeas.core.web.mvc`) routes the HTTP requests to *request
  routers* and to *web components* (the newer flavour, annotation-driven, in
  `org.silverpeas.core.web.mvc.webcomponent`).
* The **REST framework** (`org.silverpeas.core.web.rs`) hosts the JAX-RS resources published under
  `/services`, along with the exception mappers, the authentication and the token validation.
* The JSP views come with the Silverpeas tag libraries and with a graphical charter (the *look*)
  which can be overridden by a custom look.

### Licensing header

Every source file carries the AGPL-v3 + Silverpeas FLOSS-exception license header; keep it on any
new file (just copy the header of an existing one).

## Building the project

The recommended way to build Silverpeas Core is to use the development container defined in
`.devcontainer/` (or directly the `silverpeas/silverdev:latest` Docker image): it provides Java 17,
Maven, WildFly 34.0.1 (with a `wildfly start|stop|status` helper) and all the native tools required
by the tests.

```shell
# full build with the unit tests
mvn clean install

# build a single module along with the modules it depends on within this repository
mvn install -pl core-library -am

# build without running the tests
mvn clean install -DskipTests
```

The artifacts are published to (and the third-party dependencies fetched from) the Silverpeas Nexus
repository at https://nexus3.silverpeas.org/repository/silverpeas.

Two Maven profiles are worth knowing:

* `deployment` — attaches the sources and the javadoc JARs; used when deploying or releasing,
* `restapi` — generates the documentation of the REST API with Miredot.

## Testing

### Unit tests

The unit tests are located in `src/test/` of each module and are run by Surefire at each build. They
are written with JUnit 5, Mockito and Hamcrest; the beans requiring a CDI container are tested
within a custom CDI container simulator (provided by the `silverpeas-kernel-test` library) set 
up by the test support classes of `core-test`.

```shell
# run a single test class or a single test method
mvn test -pl core-library -Dtest=SomeClassTest
mvn test -pl core-library -Dtest=SomeClassTest#someMethod
```

### Integration tests

The integration tests are located in `src/integration-test/` (a source directory distinct from
`src/test/`) and they are run by Failsafe. They are gated behind the `integration-test` Maven
profile, which is activated **only** when the property `-Dcontext=ci` is set.

They run with [Arquillian](https://arquillian.org) against a **real running WildFly** instance
(started with the `standalone-full.xml` configuration). Each test deploys a purpose-built WAR
assembled by a `WarBuilder` class (`core-test/.../WarBuilder.java` and its per-module subclasses:
`WarBuilder4LibCore`, `WarBuilder4Web`, `WarBuilder4Comment`, …) that declares exactly which classes
and resources are packaged into the test archive.

These tests also require some native tools to be available in the PATH: `ffmpeg`, `imagemagick`,
`ghostscript`, `libreoffice`, `swftools` and `pdf2json`. Do **not** expect them to run in a bare
checkout: use the development container.

```shell
# start the application server (within the dev container)
wildfly start

# the complete build, as performed by the CI
mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci
```

## Continuous integration and versioning

The CI is a Jenkins pipeline (see `Jenkinsfile`) running within the `silverpeas/silverbuild` Docker
image. For each branch or pull request, it:

1. starts a WildFly instance dedicated to the integration tests,
2. computes the version to build and rewrites it in the POMs (`versions:set`), along with the version
   of the parent POM,
3. builds the whole project with the integration tests enabled,
4. for the pull requests targeting the `Silverpeas` organization and for the `master` branch, runs a
   [SonarCloud](https://sonarcloud.io) quality analysis and waits for the quality gate.

Because the CI rewrites the versions by itself, **don't hand-edit the version of the POMs** to match
what the CI does. The `next.release` property of the root POM indicates the next stable version to be
released.

## The Silverpeas ecosystem

Silverpeas Core is one project among several others making up a Silverpeas distribution. The other
main repositories of the [Silverpeas organization](https://github.com/Silverpeas) are:

* **Silverpeas-Components** — the collaborative applications (Kmelia, Almanach, Blog, Forums,
  Gallery, Quizz, Survey, Yellow Pages, …) built atop of Silverpeas Core,
* **Silverpeas-Assembly** — the assembly of the Core and of the Components into a distribution,
* **Silverpeas-Setup** / **Silverpeas-Installer** — the installation and configuration tooling,
* **Silverpeas-Kernel** — the low-level library (IoC abstraction, settings, logging) upon which
  Silverpeas Core is built,
* **Silverpeas-Project** — the parent POM defining the build rules and the dependency versions,
* **Silverpeas-Mobile** — the mobile web application.

## Contributing

Contributions are welcome. Please have a look at the
[collaboration rules](https://www.silverpeas.org/dev/collaboration.html) and at the
[technical documentation](https://www.silverpeas.org/docs/core/index.html) before submitting a pull
request, and:

* respect the coding conventions and the architectural conventions described above (in particular
  the use of the Silverpeas stereotypes instead of the raw CDI annotations),
* add the license header to any new source file,
* cover your change with unit tests and, when the change involves the container (persistence,
  transactions, CDI wiring, web resources), with integration tests,
* make sure the whole build passes before opening the pull request.

Bugs and improvement requests are tracked in the
[Silverpeas tracker](https://tracker.silverpeas.org/projects/silverpeas).
Pull requests are welcome; their title has to start with `Bug #<n>`, `Feature #<n>` or `Support 
#<n>`, referring to the tracked issue, as the continuous integration relies on it.

## License

Silverpeas Core is released under the __GNU Affero General Public License v3__ with a special
exception, the *Silverpeas FLOSS exception*, allowing to redistribute this program in connection
with Free/Libre Open Source Software applications. See `license.txt`, `silverpeas-license.txt` and
`exceptions.txt`, or the [legal page](https://www.silverpeas.org/legal/floss_exception.html) of the
Silverpeas web site.

Copyright &copy; 2000 - 2026 Silverpeas.
