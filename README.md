# Silverpeas Core

__Silverpeas Core__ is the foundation of the Silverpeas Collaborative Web Portal.

It provides both a business and a web APIs for the applications dedicated to run within the portal.

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