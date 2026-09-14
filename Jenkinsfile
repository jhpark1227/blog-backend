pipeline {
    agent any

    environment {
        AWS_REGION = 'ap-northeast-2'
        ECR_REPO   = credentials('ecr-repo-url')
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
        EC2_HOST   = credentials('prod-ec2-host')
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

        stage('CD') {
            when {
                branch 'main'
            }
            stages {
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
                                    docker exec nginx nginx -s reload &&
                                    docker image prune -f
                                '
                            """
                        }
                    }
                }
            }
        }
    }
}
