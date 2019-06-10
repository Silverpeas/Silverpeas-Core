import java.util.regex.Matcher

node {
  catchError {
    def baseNexusRepo = 'https://www.silverpeas.org/nexus/content/repositories/'
    def version
    docker.image('silverpeas/silverbuild')
        .inside('-u root -v $HOME/.m2/settings.xml:/root/.m2/settings.xml -v $HOME/.m2/settings-security.xml:/root/.m2/settings-security.xml -v $HOME/.gitconfig:/root/.gitconfig -v $HOME/.ssh:/root/.ssh -v $HOME/.gnupg:/root/.gnupg') {
      stage('Preparation') {
        checkout scm
      }
      stage('Build') {
        version = computeSnapshotVersion()
        sh """
mvn -U versions:set -DgenerateBackupPoms=false -DnewVersion=${version}
mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci
"""
      }
      stage('Quality Analysis') {
        // quality analyse with our SonarQube service is performed only for PR against our main
        // repository
        if (env.BRANCH_NAME.startsWith('PR') &&
            env.CHANGE_URL?.startsWith('https://github.com/Silverpeas')) {
          withSonarQubeEnv {
            sh """
mvn ${SONAR_MAVEN_GOAL} -Dsonar.analysis.mode=issues \\
    -Dsonar.github.pullRequest=${env.CHANGE_ID} \\
    -Dsonar.github.repository=Silverpeas/Silverpeas-Core \\
    -Dsonar.github.oauth=${SONAR_GITHUB_OAUTH} \\
    -Dsonar.host.url=${SONAR_HOST_URL} \\
    -Dsonar.login=${SONAR_AUTH_TOKEN}
"""
          }
        } else {
          echo "It isn't a PR validation for the Silverpeas organization. Nothing to analyse."
        }
      }
      stage('Deployment') {
        // deployment to ensure dependencies on this snapshot version of Silverpeas Core for other
        // projects to build downstream. By doing so, we keep clean the local maven repository for
        // reproducibility reason
        def nexusRepo = baseNexusRepo + (version.endsWith('SNAPSHOT') ? 'snapshots/' : 'dev/')
        sh "mvn deploy -DaltDeploymentRepository=silverpeas::default::${nexusRepo} -Pdeployment -Djava.awt.headless=true -Dmaven.test.skip=true"
      }
    }
  }
  step([$class                  : 'Mailer',
        notifyEveryUnstableBuild: true,
        recipients              : "miguel.moquillon@silverpeas.org, yohann.chastagnier@silverpeas.org, nicolas.eysseric@silverpeas.org",
        sendToIndividuals       : true])
}

def computeSnapshotVersion() {
  def pom = readMavenPom()
  final String version = pom.version
  final String defaultVersion = env.BRANCH_NAME == 'master' ? version :
      env.BRANCH_NAME.toLowerCase().replaceAll('[# -]', '')
  Matcher m = env.CHANGE_TITLE =~ /^(Bug #\d+|Feature #\d+).*$/
  final String snapshot =
      m.matches() ? m.group(1).toLowerCase().replaceAll(' #', '') : ''
  return snapshot.isEmpty() ? defaultVersion : "${pom.properties['next.release']}-${snapshot}"
}