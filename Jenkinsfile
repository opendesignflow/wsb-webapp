

// Start Build Step function
def transformIntoStep(jobFullName) {
    return {
       build job: jobFullName , wait: false, propagate: false
    }
}

// WSB-Webapp
node {
 
  properties([pipelineTriggers([[$class: 'GitHubPushTrigger']])])
  def mvnHome = tool 'maven3'

  stage('Clean') {
    checkout scm
    sh "${mvnHome}/bin/mvn -B clean"
  }

  stage('Build') {
    sh "${mvnHome}/bin/mvn -B  compile test-compile"
  }

  stage('Test') {
    sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore test"
    //junit '**/target/surefire-reports/TEST-*.xml'
  }

  // Deploy or Package
  //---------
  if (env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == 'master') {
    stage('Deploy') {
        sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore deploy"
        step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
    }

  } else {
    stage('Package') {
        sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore deploy"
        step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
    }
  }
  


}
