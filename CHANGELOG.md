# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
- GutHub pages showcasing the plugin
- GitHub workflows
  - Generate `plugin.xml` description from the [README](README.md) to avoid duplicities
  - Release automation (see https://github.com/JetBrains/intellij-platform-plugin-template/blob/main/.github/workflows/release.yml) 
- Creation of new tests for classes (`Navigate -> Go To Test -> Create new test`)
  - Persisted the New Specification dialog's values to be used as defaults next time
  - Configurable Spec class name suffix in IntelliJ settings
  - An IntelliJ settings preference allowing to define test annotation/baseClass mapping to a suffix.
    - E.g. When test is annotated with @IntegrationSpec, the new test will get an `IntegrationSpec` classname suffix.
  - Allow generating feature methods for selected class methods
- Highlighting
  - `when`, `then`, `and`, and `then` sections
  - Colour override in settings
  - Data table columns and pipes (The columns behind `||` could be visually different)
- Auto-completion (or live templates)
  - Properties within `unrolled` feature methods names (expressions are denoted with `#` and only support property access and zero-arg method calls)
  - Block names in feature methods (i.e. `when/then/expect`, etc.)
  - Fixture method names (i.e. `setup`, `setupSpec`, etc.)
  - New feature methods based on class-in-test methods (For instance, by selecting `Clazz.something()` method, a Spock Feature method will be created and named `def "Something returns true"() {}`)
- Inspections
  - Empty `setup`, `cleanupSpec`, etc.
  - Unused variables in the `where` block
  - Warning when a `then` block contains anything else than assertions (assignments to variables, etc.)
    - Allow `def e = thrown(Exception)` so that exception messages can be asserted
- Quick actions
  - Errors (Spock blocks)
  - Data tables
      - Swap columns
      - Delete columns
      - Split long tables into two

## [1.1.0] - 2024-??-??

## [1.0.0] - 2024-03-23

### Added

- `Go To Test` support for Specifications in Groovy, Java and Kotlin files (`Navigate -> Test`)

## [0.2.0] - 2024-01-22
  
### Added
 
- Spock Specification file template
- Spock Specification action for quick Specification creation (`Project -> New -> Spock Specification`)

## [0.1.0] - 2023-12-20

- Spock block order and completeness inspection 
