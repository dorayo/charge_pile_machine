pipeline {
    agent none

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }



    stages {

        stage('git checkout'){
            steps{
                script {
                    echo "hello word"
                    sh "docker images"
                }
            }
        }

        stage('Build') {
            agent {
                docker {
                    image 'maven:3-jdk-8-alpine'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps{
                sh 'mvn --version'
            }
        }

    }
}




