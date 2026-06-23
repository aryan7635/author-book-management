pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/aryan7635/author-book-management.git'
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean compile'
            }
        }
        stage('Test') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw test'
            }
        }

        stage('Package') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw package'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t author-book-management:latest .'
            }
        }
    }
}