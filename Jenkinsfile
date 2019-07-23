pipeline {
    agent any
    environment { 
        M2_HOME='/usr/local/Cellar/maven/3.6.1/libexec'
        COVERITY_TOOL_HOME="/Applications/cov-analysis-macosx-2019.06"
        PATH="$PATH:$M2_HOME/bin:$COVERITY_TOOL_HOME/bin"
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
        stage('cov-configure') { 
            steps {
                sh 'cov-configure --config idir/conf.xml --java'
            }
        }
        stage('cov-build') { 
            steps {
                sh 'cov-build --config idir/conf.xml --dir idir --delete-stale-tus --desktop mvn -B -DskipTests package'
            }
        }
        stage('cov-analyze') { 
            steps {
                sh 'cov-analyze --config idir/conf.xml --dir idir --all --strip-path ${WORKSPACE} --allow-unmerged-emits --disable-fb --export-summaries false'
            }
        }
    }
}
