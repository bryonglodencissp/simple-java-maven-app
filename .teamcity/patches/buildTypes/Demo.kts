package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'Demo'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("Demo")) {
    expectSteps {
        maven {
            name = "Initialize"
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true -DskipTests"
        }
        script {
            name = "Analyze"
            scriptContent = """
                pwd;
                cov-capture --project-dir ./;
                cov-analyze --dir idir --webapp-security --disable-fb --export-summaries false;
            """.trimIndent()
        }
    }
    steps {
        update<ScriptBuildStep>(1) {
            name = "Capture"
        }
        insert(2) {
            script {
                name = "FastDesktop"
                scriptContent = """
                    #!/bin/sh
                    COVERITY_TOOL_HOME="/Applications/cov-analysis-macosx-2019.06"
                    PATH="${'$'}PATH:${'$'}COVERITY_TOOL_HOME/bin"
                    rm filelist.txt; git whatchanged -n 1 --oneline --name-only --pretty=format: | sort | uniq | grep . > filelist.txt; echo "cat filelist.txt"; cat filelist.txt; echo "git diff"; git diff;
                    if [ -d "idir" ]
                    then
                    	cov-run-desktop --config idir/conf.xml --dir idir --disconnected --text-output cov-errors.txt --exit1-if-defects true --present-in-reference false --set-new-defect-owner false --ignore-uncapturable-inputs true --strip-path `pwd` --all --disable-fb --scm git filelist.txt
                    	if [ ${'$'}? -eq 1 ]
                    	then
                    		echo "cov-run-desktop found defects"
                    		echo "Stop the commit"
                    		exit 1
                    	else
                    		echo "cov-run-desktop did not find defects"
                    		echo "Do not stop commit"
                    		exit 0
                    	fi
                    else
                    	M2_HOME='/usr/local/Cellar/maven/3.6.1/libexec'
                    	PATH="${'$'}PATH:${'$'}M2_HOME/bin"
                    	mvn -B -DskipTests clean
                    	cov-configure --config idir/conf.xml --java
                    	cov-build --config idir/conf.xml --dir idir --delete-stale-tus --desktop mvn -B -DskipTests package
                    	cov-run-desktop --config idir/conf.xml --dir idir --disconnected --text-output cov-errors.txt --exit1-if-defects true --present-in-reference false --set-new-defect-owner false --ignore-uncapturable-inputs true --strip-path `pwd` --all --disable-fb --analyze-captured-source --scm git
                    	if [ ${'$'}? -eq 1 ]
                    	then
                    		echo "cov-analyze found defects"
                    		echo "Stop the commit"
                    		exit 1
                    	else
                    		echo "cov-analyze did not find defects"
                    		echo "Do not stop commit"
                    		exit 0
                    	fi
                    fi
                """.trimIndent()
            }
        }
    }
}
