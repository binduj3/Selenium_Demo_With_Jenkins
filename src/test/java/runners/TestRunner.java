package runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * JUnit 5 runner. Plugins, tags and parallel settings live in
 * src/test/resources/junit-platform.properties so they can be overridden with -D
 * (values set here with @ConfigurationParameter can NOT be overridden from the command line).
 *
 *   mvn test -Dcucumber.filter.tags="@smoke" -Dbrowser=firefox
 *
 * Re-running failures (replaces the old FailedRunner):
 *   mvn test -Dcucumber.features="$(paste -sd, target/rerun.txt)"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "steps")
public class TestRunner {
}
