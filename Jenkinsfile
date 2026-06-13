pipeline {
    agent any

    stages {
        stage('Test') {
            when {
                branch 'main'
            }
            steps {
                sh './gradlew clean test'
            }
        }
    }
}