pipeline {
  agent { label 'master' }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }


  stages {
    stage('git checkout'){
        steps{
            echo "hello word"
        }
    }
  }
}
