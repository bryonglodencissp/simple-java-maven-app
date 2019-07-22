pipeline {
    agent any
    environment { 
        M2_HOME='/usr/local/Cellar/maven/3.6.1/libexec'
        PATH="$PATH:$M2_HOME/bin"
        COVERITY_TOOL_HOME="/Applications/cov-analysis-macosx-2019.06"
        PATH="$PATH:$COVERITY_TOOL_HOME/bin"
        COV_HOST = '192.168.56.101'
		COV_PORT = '8080'
		COV_USER = 'admin'
		COV_PASS = 'SIGpass8!'
    }
    options {
        skipStagesAfterUnstable()
    }
    stages {
        stage('Clean') { 
            steps {
                sh 'mvn -B -DskipTests clean'
            }
        }
        stage('Confif') { 
            steps {
				sh 'cov-configure --config idir/conf.xml --java'
            }
        }
    }
}
