import java.util.regex.Matcher

pipeline {
  environment {
    lockFilePath = null
    version = null
  }
  agent {
    docker {
      image 'silverpeas/silverbuild'
      args '''
        -v $HOME/.m2:/home/silverbuild/.m2 
        -v $HOME/.gitconfig:/home/silverbuild/.gitconfig 
        -v $HOME/.ssh:/home/silverbuild/.ssh 
        -v $HOME/.gnupg:/home/silverbuild/.gnupg
        '''
    }
  }
  stages {
    stage('Build') {
      steps {
        script {
          sh "/opt/wildfly-for-tests/wildfly-*.Final/bin/standalone.sh -c standalone-full.xml &> /dev/null &"
          version = computeSnapshotVersion()
          checkParentPOMVersion(version)
          waitForDependencyRunningBuildIfAny(version, 'core')
          lockFilePath = createLockFile(version, 'core')
          sh """
            mvn -U versions:set -DgenerateBackupPoms=false -DnewVersion=${version}
            mvn clean install -Pdeployment -Djava.awt.headless=true -Dcontext=ci
            /opt/wildfly-for-tests/wildfly-*.Final/bin/jboss-cli.sh --connect :shutdown
            """
          deleteLockFile(lockFilePath)
        }
      }
    }
    stage('Quality Analysis') {
      // quality analyse with our SonarQube service is performed only for PR against our main
      // repository and for master branch
      when {
        expression {
          env.BRANCH_NAME.startsWith('PR') &&
              env.CHANGE_URL?.startsWith('https://github.com/Silverpeas')
        }
      }
      steps {
        script {
          withSonarQubeEnv {
            sh """
                mvn ${SONAR_MAVEN_GOAL} -Dsonar.projectKey=Silverpeas_Silverpeas-Core2 \\
                  -Dsonar.organization=silverpeas \\
                  -Dsonar.pullrequest.branch=${env.BRANCH_NAME} \\
                  -Dsonar.pullrequest.key=${env.CHANGE_ID} \\
                  -Dsonar.pullrequest.base=master \\
                  -Dsonar.pullrequest.provider=github \\
                  -Dsonar.host.url=${SONAR_HOST_URL} \\
                  -Dsonar.login=${SONAR_AUTH_TOKEN} \\
                  -Dsonar.scanner.force-deprecated-java-version=true
                """
          }
          timeout(time: 30, unit: 'MINUTES') {
            // Just in case something goes wrong, pipeline will be killed after a timeout
            def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
            if (qg.status != 'OK' && qg.status != 'WARNING') {
              error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }
          }
        }
      }
    }
  }
  post {
    always {
      deleteLockFile(lockFilePath)
      step([$class                  : 'Mailer',
            notifyEveryUnstableBuild: true,
            recipients              : "miguel.moquillon@silverpeas.org, yohann.chastagnier@silverpeas.org, nicolas.eysseric@silverpeas.org",
            sendToIndividuals       : true])
    }
  }
}

def computeSnapshotVersion() {
  def pom = readMavenPom()
  final String version = pom.version
  final String defaultVersion = env.BRANCH_NAME == 'master' ? version :
      env.BRANCH_NAME.toLowerCase().replaceAll('[# -]', '')
  Matcher m = env.CHANGE_TITLE =~ /^(Bug #?\d+|Feature #?\d+).*$/
  String snapshot = m.matches()
      ? m.group(1).toLowerCase().replaceAll(' #?', '')
      : ''
  if (snapshot.isEmpty()) {
    m = env.CHANGE_TITLE =~ /^\[([^\[\]]+)].*$/
    snapshot = m.matches()
        ? m.group(1).toLowerCase().replaceAll('[/><|:&?!;,*%$=}{#~\'"\\\\°)(\\[\\]]', '').trim().replaceAll('[ @]', '-')
        : ''
  }
  return snapshot.isEmpty() ? defaultVersion : "${pom.properties['next.release']}-${snapshot}"
}

static def createLockFilePath(version, projectName) {
  final String lockFilePath = "\$HOME/.m2/${version}_${projectName}_build.lock"
  return lockFilePath
}

def createLockFile(version, projectName) {
  final String lockFilePath = createLockFilePath(version, projectName)
  sh "touch ${lockFilePath}"
  return lockFilePath
}

def deleteLockFile(lockFilePath) {
  if (isLockFileExisting(lockFilePath)) {
    sh "rm -f ${lockFilePath}"
  }
}

def isLockFileExisting(lockFilePath) {
  if (lockFilePath?.trim()?.length() > 0) {
    def exitCode = sh script: "test -e ${lockFilePath}", returnStatus: true
    return exitCode == 0
  }
  return false
}

def waitForDependencyRunningBuildIfAny(version, projectName) {
  final String dependencyLockFilePath = createLockFilePath(version, projectName)
  timeout(time: 3, unit: 'HOURS') {
    waitUntil {
      return !isLockFileExisting(dependencyLockFilePath)
    }
  }
  if (isLockFileExisting(dependencyLockFilePath)) {
    error "After timeout dependency lock file ${dependencyLockFilePath} is yet existing!!!!"
  }
}

def checkParentPOMVersion(version) {
  def pom = readMavenPom()
  int idx = pom.parent.version.indexOf('-SNAPSHOT')
  if (idx > 0) {
    String[] snapshot = version.split('-')
    String parentVersion = pom.parent.version.substring(0, idx) + '-' + snapshot[snapshot.length - 1]
    echo "Update parent POM to ${parentVersion}"
    sh """
      mvn versions:update-parent -DgenerateBackupPoms=false -DparentVersion="[${parentVersion}]"
      """
  }
}