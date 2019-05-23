node {
  catchError {
    def remoteUrl = 'https://github.com'
    def remote = [:]
    docker.image('silverpeas/silverbuild').inside('-u root -v $HOME/.m2/settings.xml:/root/.m2/settings.xml -v $HOME/.m2/settings-security.xml:/root/.m2/settings-security.xml -v $HOME/.gitconfig:/root/.gitconfig -v $HOME/.ssh:/root/.ssh -v $HOME/.gnupg:/root/.gnupg') {
      stage('Preparation') {
        withSonarQubeEnv {
          echo "BRANCH NAME IS ${env.BRANCH_NAME}"
          echo "CHANGE ID IS ${env.ghprbPullId}"
          echo "GIT URL IS ${env.GIT_URL}"
          echo "SONAR MAVEN GOAL IS $SONAR_MAVEN_GOAL"
          echo "GITHUB OAUTH IS $SONAR_GITHUB_OAUTH"
          echo "SONAR HOST URL IS $SONAR_HOST_URL"
          echo "SONAR AUTH TOKEN IS $SONAR_AUTH_TOKEN"
        }
        //cleanDir()
        //git credentialsId: 'cacc0467-7c85-41d1-bf4e-eaa470dd5e59', branch: env.BRANCH_NAME, poll: false, url: env.GIT_URL
      }
    }
  }
  step([$class                  : 'Mailer',
        notifyEveryUnstableBuild: true,
        recipients              : "miguel.moquillon@silverpeas.org",
        sendToIndividuals       : true])
}

def cleanDir() {
  sh "rm -rf *"
}