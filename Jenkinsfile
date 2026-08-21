pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-northeast-2'
        ECR_REPO   = credentials('ecr-repo-url')
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
        EC2_HOST   = credentials('prod-ec2-host')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                url: 'https://github.com/jhpark1227/blog-backend.git',
                credentialsId: 'github-token'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
            post {
                failure {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Build & Push to ECR') {
            steps {
                sh './gradlew copyApiSpec'
                sh './gradlew build -x test'
                script {
                    docker.withRegistry("https://${ECR_REPO.tokenize('/')[0]}", 'ecr:ap-northeast-2:aws-credentials') {
                        def img = docker.build("${ECR_REPO}:${IMAGE_TAG}", "--platform linux/amd64 .")
                        img.push()
                        img.push('latest')
                    }
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(credentials: ['ec2-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_HOST} '
                            aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO.tokenize("/")[0]} &&
                            cd ~/blog &&
                            docker compose pull spring &&
                            docker compose up -d &&
                            docker image prune -f
                        '
                    """
                }
            }
        }
    }
}