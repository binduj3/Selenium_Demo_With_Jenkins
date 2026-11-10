// Cucumber UI tests on Selenium Grid in Docker
//   - Pull requests / branch pushes -> @smoke
//   - Nightly (main branch only)    -> @regression
//   - Chrome, Firefox and Edge run side by side; scenarios inside each browser run in parallel threads
//
// Set up as a Multibranch Pipeline (GitHub/Bitbucket/GitLab branch source) so PRs are built automatically.
// Jenkins plugins needed: Pipeline, Docker Pipeline, JUnit, HTML Publisher, Timestamper.
// The agent needs Docker (and an x86_64 host: the Edge image has no ARM build).

pipeline {
    agent none

    options {
        timestamps()
        timeout(time: 90, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '30'))
        disableConcurrentBuilds(abortPrevious: true)   // new PR push cancels the old run
    }

    parameters {
        choice(name: 'SUITE', choices: ['auto', 'smoke', 'regression'],
               description: 'auto = smoke for PRs/pushes, regression for the nightly run')
        string(name: 'THREADS', defaultValue: '3', description: 'Parallel scenarios per browser')
        string(name: 'TAGS_OVERRIDE', defaultValue: '', description: 'Optional custom tag expression, e.g. "@login and not @excel"')
    }

    // Nightly at ~2 AM, only on main (other branches/PRs get an empty cron = no schedule)
    triggers {
        cron(env.BRANCH_NAME == 'main' ? 'H 2 * * *' : '')
    }

    environment {
        MAVEN_IMAGE      = 'maven:3.9-eclipse-temurin-25'
        SELENIUM_VERSION = 'latest'     // pin to a tag matching selenium-java in pom.xml for stable builds
    }

    stages {
        stage('Select suite') {
            steps {
                script {
                    def suite = params.SUITE ?: 'auto'
                    if (suite == 'auto') {
                        boolean nightly = !currentBuild.getBuildCauses('hudson.triggers.TimerTrigger$TimerTriggerCause').isEmpty()
                        suite = nightly ? 'regression' : 'smoke'
                    }
                    env.TEST_TAGS = params.TAGS_OVERRIDE?.trim() ? params.TAGS_OVERRIDE.trim() : "@${suite}"
                    env.THREADS   = (params.THREADS ?: '3').trim()
                    currentBuild.description = "${suite} | ${env.TEST_TAGS}" + (env.CHANGE_ID ? " | PR-${env.CHANGE_ID}" : '')
                    echo "Running tags '${env.TEST_TAGS}' with ${env.THREADS} threads per browser"
                }
            }
        }

        stage('UI tests') {
            matrix {
                // Each cell gets its own agent + workspace, so the three browsers never share target/.
                // Swap 'any' for a label like { label 'docker' } if only some agents have Docker.
                agent any
                axes {
                    axis {
                        name 'BROWSER'
                        values 'chrome', 'firefox', 'edge'
                    }
                }
                stages {
                    stage('Test') {
                        steps {
                            script {
                                def net = "grid-${env.BUILD_TAG}-${BROWSER}".toLowerCase().replaceAll('[^a-z0-9_.-]', '-')
                                sh "docker network create ${net}"
                                try {
                                    // Sidecar: standalone Selenium (browser + driver) reachable as http://selenium:4444
                                    docker.image("selenium/standalone-${BROWSER}:${SELENIUM_VERSION}").withRun(
                                            "--network ${net} --network-alias selenium --shm-size=2g " +
                                            "-e SE_NODE_MAX_SESSIONS=${env.THREADS} -e SE_NODE_OVERRIDE_MAX_SESSIONS=true") { grid ->

                                        docker.image(env.MAVEN_IMAGE).inside("--network ${net}") {
                                            sh """
                                                mvn -B -ntp clean test \
                                                  -Dmaven.repo.local="\$WORKSPACE/.m2" \
                                                  -Dmaven.test.failure.ignore=true \
                                                  -Dbrowser=${BROWSER} \
                                                  -DgridUrl=http://selenium:4444 \
                                                  -Dcucumber.filter.tags="${env.TEST_TAGS}" \
                                                  -Dcucumber.execution.parallel.config.fixed.parallelism=${env.THREADS} \
                                                  -Dcucumber.execution.parallel.config.fixed.max-pool-size=${env.THREADS}
                                            """
                                        }
                                    }
                                } finally {
                                    sh "docker network rm ${net} || true"
                                }
                            }
                        }
                        post {
                            always {
                                // Failing scenarios mark the build UNSTABLE (yellow) rather than FAILED (red)
                                junit testResults: 'target/cucumber-reports/*.xml', allowEmptyResults: true
                                publishHTML(target: [
                                        reportName           : "Cucumber Report - ${BROWSER}",
                                        reportDir            : 'target/cucumber-html-reports',
                                        reportFiles          : 'overview-features.html',
                                        keepAll              : true,
                                        alwaysLinkToLastBuild: true,
                                        allowMissing         : true
                                ])
                                archiveArtifacts artifacts: 'target/cucumber-reports/**, target/rerun.txt',
                                                 allowEmptyArchive: true, fingerprint: false
                            }
                        }
                    }
                }
            }
        }
    }
}
