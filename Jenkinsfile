// Build, scan, containerise, then write the new tag into the chart's dev
// values file. Jenkins never deploys: ArgoCD watches deploy/chart and applies
// whatever the committed tag points at.
pipeline {
    agent any

    environment {
        IMAGE      = 'reminder-api'
        VALUES     = 'deploy/chart/envs/dev.yaml'
        REPO       = 'github.com/Wasseel/calendar-reminder.git'
        BOT_NAME   = 'jenkins-bot'
        BOT_EMAIL  = 'jenkins-bot@localhost'
        NODES      = 'desktop-control-plane desktop-worker desktop-worker2'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {

        // App and config share one repo, so the tag-bump commit at the end of
        // this pipeline would retrigger it forever. Three independent checks,
        // because any one of them can be defeated on its own.
        stage('Guard against the deploy loop') {
            steps {
                script {
                    def author  = sh(script: 'git log -1 --pretty=%an', returnStdout: true).trim()
                    def message = sh(script: 'git log -1 --pretty=%B',  returnStdout: true).trim()
                    def touched = sh(script: 'git show --name-only --pretty=format: HEAD',
                                     returnStdout: true).trim()

                    def paths      = touched.split('\n').findAll { it.trim() }
                    def deployOnly = paths && paths.every { it.startsWith('deploy/') }

                    if (author == env.BOT_NAME || message.contains('[skip ci]') || deployOnly) {
                        env.SKIP_BUILD = 'true'
                        currentBuild.result = 'NOT_BUILT'
                        currentBuild.description = 'deploy-only commit, nothing to rebuild'
                        echo "Skipping: author=${author} deployOnly=${deployOnly}"
                    } else {
                        def pom = readMavenPom file: 'pom.xml'
                        def sha = sh(script: 'git rev-parse --short=7 HEAD', returnStdout: true).trim()
                        env.TAG = "${pom.version.replace('-SNAPSHOT', '')}-${env.BUILD_NUMBER}-${sha}"
                        currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.TAG}"
                        echo "Building ${env.IMAGE}:${env.TAG}"
                    }
                }
            }
        }

        stage('Build and test') {
            when { expression { env.SKIP_BUILD != 'true' } }
            steps {
                // No mvn in the Jenkins image; the wrapper fetches its own.
                sh 'chmod +x mvnw && ./mvnw -B clean verify'
            }
            post {
                always { junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml' }
            }
        }

        stage('SonarQube scan') {
            when { expression { env.SKIP_BUILD != 'true' } }
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh './mvnw -B sonar:sonar -Dsonar.projectKey=reminder-api'
                }
            }
        }

        // Depends on the SonarQube webhook calling back to Jenkins. Without it
        // this waits until the timeout with no error, which reads as a hang.
        stage('Quality gate') {
            when { expression { env.SKIP_BUILD != 'true' } }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build image') {
            when { expression { env.SKIP_BUILD != 'true' } }
            steps {
                sh 'docker build -t $IMAGE:$TAG .'
            }
        }

        // Interim: push straight into the cluster's containerd. Replace this
        // whole stage with `docker push nexus:8082/$IMAGE:$TAG` once the Nexus
        // registry accepts logins, and set image.repository in values.yaml.
        stage('Load into cluster') {
            when { expression { env.SKIP_BUILD != 'true' } }
            steps {
                sh '''
                    set -e
                    docker save $IMAGE:$TAG -o /tmp/$IMAGE-$TAG.tar
                    for node in $NODES; do
                        echo "loading into $node"
                        docker exec -i "$node" ctr -n=k8s.io images import - < /tmp/$IMAGE-$TAG.tar
                    done
                    rm -f /tmp/$IMAGE-$TAG.tar
                '''
            }
        }

        // The only thing that actually causes a deployment. ArgoCD sees this
        // commit and rolls it out; Jenkins never touches the cluster's state.
        stage('Bump the tag') {
            when { expression { env.SKIP_BUILD != 'true' } }
            steps {
                withCredentials([usernamePassword(credentialsId: 'github-creds',
                                                  usernameVariable: 'GIT_USER',
                                                  passwordVariable: 'GIT_TOKEN')]) {
                    // Single quotes: the shell expands the credentials, not
                    // Groovy, so they never land in the build log.
                    sh '''
                        set -e
                        yq -i ".image.tag = \\"$TAG\\"" "$VALUES"
                        git config user.name  "$BOT_NAME"
                        git config user.email "$BOT_EMAIL"
                        git add "$VALUES"
                        if git diff --cached --quiet; then
                            echo "tag unchanged, nothing to commit"
                        else
                            git commit -m "Deploy $TAG to dev [skip ci]"
                            git push "https://${GIT_USER}:${GIT_TOKEN}@${REPO}" HEAD:main
                        fi
                    '''
                }
            }
        }
    }

    post {
        success { echo "ArgoCD will pick up ${env.TAG ?: 'no new tag'} within ~3 minutes." }
        cleanup { sh 'docker image prune -f --filter "until=24h" || true' }
    }
}
