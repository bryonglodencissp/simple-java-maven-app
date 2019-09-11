package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs

object Build : BuildType({
    name = "Build"

    vcs {
        root(HttpsGithubComBryonglodencisspSimpleJavaMavenAppGitRefsHeadsMaster)
    }

    steps {
        maven {
            goals = "clean package"
            runnerArgs = "-Dmaven.test.failure.ignore=true -DskipTests"
        }
        script {
            name = "Coverity"
            scriptContent = """
                pwd;
                cov-capture --project-dir ./;
                cov-analyze --dir idir --webapp-security --disable-fb --export-summaries false;
            """.trimIndent()
        }
    }

    triggers {
        vcs {
        }
    }
})
