[![Build Status](https://travis-ci.org/easeci/easeci-core-java.svg?branch=master)](https://travis-ci.org/easeci/easeci-core-java)
![LINE](https://img.shields.io/badge/line--coverage-60%25-orange.svg)

Ease CI core
================

![EaseCI Project logo](docs/static/full-logo.png "Ease CI core")

EaseCI is a project that enables rapid deployment of applications in popular technologies. 
A simple interface allows creating complex Delivery Pipeline even for newbies. 
The project is an open source created by people who want to save the suffering associated with deployment.

## Main principles

If you want to develop the EaseCI project, you must follow several community rules:
- the code must be logically divided into packages
- we follow the rules of clean SOLID code etc.
- every interface and base class must be provided with valuable comments
- the project must have high code coverage, unit tests are required for each commit

## Technologies used

In Ease CI you can write in any JVM language. No matter which one.
The only condition is that the application works properly.
<br>Main:
- Java 11
- Groovy 2.5
- Ratpack 1.8
- Netty 4.1
<hr>

<br>Libraries:
- Junit 5
- Mockito 3
- RxJava 3
- Vavr 0.10.2
- Guava 28.1
- Lombok 1.18

## Plugin system

EaseCI core as the name implies only provides an easily expandable core. Plugins for EaseCI can be developed 
in any language that compiles into the jar archive and meets the requirements described in the documentation.
