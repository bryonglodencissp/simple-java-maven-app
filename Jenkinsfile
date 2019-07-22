pipeline {
    agent any
    environment { 
        M2_HOME = '/usr/local/Cellar/maven/3.6.1/libexec'
    }
    options {
        skipStagesAfterUnstable()
    }
    stages {
        stage('Build') { 
            steps {
                sh 'mvn -B -DskipTests clean package' 
            }
        }
    }
}
