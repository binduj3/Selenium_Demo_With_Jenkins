# Cucumber_demo – SauceDemo login tests

Selenium 4 + Cucumber 7 on JUnit 5, Java 25. Scenarios run in parallel (one browser per thread) on Chrome, Edge, Firefox or Safari, locally or on Selenium Grid.

## Run locally

```bash
mvn clean test                                   # everything, Chrome, 3 threads
mvn clean test -Dcucumber.filter.tags="@smoke"   # smoke only
mvn clean test -Dbrowser=firefox -Dheadless=true
mvn clean test -Dcucumber.execution.parallel.config.fixed.parallelism=5 \
               -Dcucumber.execution.parallel.config.fixed.max-pool-size=5
```

Re-run only the failed scenarios:

```bash
mvn test -Dcucumber.features="$(paste -sd, target/rerun.txt)"
```

## Run on a local Selenium Grid (Docker)

```bash
docker compose up -d
mvn clean test -Dbrowser=edge -DgridUrl=http://localhost:4444
docker compose down
```

Grid UI: http://localhost:4444/ui

## Settings

`src/test/resources/config/config.properties`. Each value can be overridden with `-Dkey=value` or an env var (`BROWSER`, `HEADLESS`, `GRIDURL`).

| key      | values                          |
|----------|---------------------------------|
| browser  | chrome, edge, firefox, safari   |
| headless | true / false                    |
| gridUrl  | empty = local browser           |
| url      | app under test                  |

Safari allows only one session per Mac, so run it with parallelism 1.

## Reports

- `target/cucumber-html-reports/overview-features.html`: full report (masterthought)
- `target/cucumber-reports/`: cucumber.html, cucumber.json, cucumber.xml (JUnit format)
- `target/screenshots/`: screenshots of failed scenarios (they're also attached in the report)
- `target/logs/test.log`

## CI (Jenkinsfile)

Set this up as a Multibranch Pipeline.

- **PRs and pushes:** run `@smoke`.
- **Nightly on `main`:** runs `@regression`.
- **Browsers:** Chrome, Firefox and Edge run side by side, each against its own Selenium container.
- **Manual runs:** with "Build with Parameters" you can pick the suite, the thread count or a custom tag expression.

Jenkins plugins needed: Docker Pipeline, JUnit, HTML Publisher, Timestamper. The agent needs Docker on x86_64.
# Selenium_Demo_With_Jenkins
