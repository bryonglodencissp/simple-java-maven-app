pipeline {
    agent any
    environment { 
        MAVEN_HOME = '/usr/local/Cellar/maven/3.6.1/libexec/bin'
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
