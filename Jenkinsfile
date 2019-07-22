pipeline {
    agent any
    environment { 
        M2_HOME='/usr/local/Cellar/maven/3.6.1/libexec'
        PATH="$PATH:$M2_HOME/bin/"
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
