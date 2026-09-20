pipeline {
    agent any

    environment {
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
    }

    stages {
        stage('CI') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            stages {
                stage('Checkout') {
                    steps {
                        checkout scm
                    }
                }

                stage('Test') {
                    steps {
                        sh './gradlew test'
                    }
                    post {
                        always {
                            junit '**/build/test-results/test/*.xml'
                        }
                    }
                }
            }
        }
    }
}