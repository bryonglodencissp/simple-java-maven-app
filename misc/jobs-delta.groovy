#!/usr/bin/env/groovy

pipeline {
	agent {
	    node {
	        label ''
	        // Important! So we use same workspace as zlib-nightly job
	        customWorkspace 'workspace/zlib-custom'
	    }
	}
	environment {
		COV_HOST = 'cov-connect'
		COV_PORT = '8080'
		COV_USER = 'admin'
		COV_PASS = 'coverity'
	}
	stages {
	    stage('Fetch') {
	        steps {
	            // Pull changes from repo (or clone if empty) [actually this should be fetch + checkout, but doesn't work]
	            sh '[ -d .git ] && git pull origin master || git clone ssh://git@cov-git/opt/git/zlib.git .'
			    // Compute analyze file set (last commit); replace -n with --since <date> if needed
			    //sh 'git whatchanged -n 1 --oneline --name-only --pretty=format: | sort | uniq | grep . > filelist.txt || rm -f filelist.txt'
			    sh 'git whatchanged --since="`cat timestamp`" --oneline --name-only --pretty=format: | sort | uniq | grep . > filelist.txt || rm -f filelist.txt'
			    sh 'date > timestamp'
			    sh 'rm -f cov-errors.txt'
	        }
	    }
	    stage('Build') {
		    agent {
			    docker {
			        image 'gubraun/coverity'
					// Fixed hostname and mac to enable node locked license
					args '-h build-vm -u jenkins --mac-address 02:42:ac:11:00:03 --network docker_coverity -e JENKINS_HOME -e WORKSPACE'
					//registryUrl "https://registry.hub.docker.com/u/gubraun/coverity/"
					//registryCredentialsId "d45e6e2a-59fd-41de-80cc-68d0ee4a2e68"
					// This is important, otherwise copying data between workspace (@1, @2) becomes quite complicatied
					reuseNode true 
				}
			}
			steps {
			    // Compiler configuration is done in any case, Coverity is smart enough to not redo it
			    sh 'cov-configure --config ${WORKSPACE}/cov-config/compiler_config.xml --gcc'
				sh 'cov-configure --config ${WORKSPACE}/cov-config/compiler_config.xml --compiler cc --comptype gcc --template'
				
				// ./configure is only done if not done yet (initial build)
				sh '[ ! -e zlib.pc ] && ./configure || echo'

                // Incremental capture build, to capture e.g. new files				
				sh 'cov-build --config ${WORKSPACE}/cov-config/compiler_config.xml --dir ${WORKSPACE}/idir --desktop make'

                // Incremental analysis (only most recent git commit); fail build if new defects (not in last nightly) are found
                sh 'rm -f cov-errors.txt'
                sh '[ -s filelist.txt ] && cov-run-desktop --config ${WORKSPACE}/cov-config/compiler_config.xml --dir ${WORKSPACE}/idir --host ${COV_HOST} --port ${COV_PORT} --user $COV_USER --password $COV_PASS --stream zlib-jenkins-nightly --text-output cov-errors.txt --present-in-reference false --set-new-defect-owner false --ignore-uncapturable-inputs true --strip-path ${WORKSPACE} --all @@filelist.txt > cov-errors.txt || echo'
                sh '[ ! -f cov-errors.txt ] && touch cov-errors.txt || echo'

				// Commit defects to Coverity Connect (separate stream) for analysis (optional)
				//sh '[ -s filelist.txt ] && cov-commit-defects --dir ${WORKSPACE}/idir --encryption none --host ${COV_HOST} --port ${COV_PORT} --user ${COV_USER} --password ${COV_PASS} --stream zlib-jenkins-nightly --ticker-mode none || echo "Nothing to commit"''
			}
		}
	}
	post {
	    always {
            archiveArtifacts artifacts: 'cov-errors.txt'
	    }
	}
}

