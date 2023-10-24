pipeline {
  agent { label 'master' }

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
  }
}
