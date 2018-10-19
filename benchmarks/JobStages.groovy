import groovy.json.JsonOutput


def setUpCli(environment) {
  stage("Set up Agave CLI") {
    try {
      sh "tenants-list"
      sh "tenants-init -t sandbox"
    } catch (error) {
      print "Tenant already initialized"
    }

    try {
      sh "clients-delete -u '${environment.AGAVE_USERNAME}' -p '${environment.AGAVE_PASSWORD}' ${environment.AGAVE_APP_NAME}"
    } catch (error) {
      print "Cannot delete client: ${environment.AGAVE_APP_NAME} does not exist"
    }

    try {
      sh "clients-create -u '${environment.AGAVE_USERNAME}' -p '${environment.AGAVE_PASSWORD}' -N '${environment.AGAVE_APP_NAME}' -S"
    } catch (error) {
      print "Cannot create client: ${environment.AGAVE_APP_NAME} already exists"
    }

    try {
      sh "auth-tokens-create -u '${environment.AGAVE_USERNAME}' -p '${environment.AGAVE_PASSWORD}' "
    } catch (error) {
      print "Cannot create auth tokens for CLI. Aborting..."
      throw error
    }
  }
}

def cloneRepository() {
  stage("Clone Funwave TVD") {
    checkout scm
  }
}

def updateApp(jobType, environment) {
  stage("Update app") {
    sh "apps-addupdate -F funwave-${jobType}-app.txt"

    env.WEBHOOK_URL = sh(returnStdout: true, script: "requestbin-create")
    env.WEBHOOK_URL = env.WEBHOOK_URL.trim()

    def notificationsSpecs = [
      url: env.WEBHOOK_URL,
      event: "*",
      persistent: "true"
    ]

    def parametersSpecs = [
      code_version: "latest"
    ]

    def appSpecs = [
      name: "funwave-${jobType}",
      appId: "${environment.AGAVE_USERNAME}-${environment.MACHINE_NAME}-funwave-dbuild-1.0",
      maxRunTime: "00:10:00",
      archive: false,
      notifications: [notificationsSpecs],
      parameters: parametersSpecs
    ]

    def jsonContent = JsonOutput.toJson(appSpecs)
    writeFile(file: "funwave-${jobType}-job.json", text: jsonContent)
  }
}

def submitJob(jobType, environment) {
  stage("Submit job") {
    def submitOutput = sh(returnStdout: true, script: "jobs-submit -F funwave-${jobType}-job.json")
    def jobId = submitOutput.split(" ")[-1]
    env.AGAVE_JOB_ID = jobId.trim()

    sleep 90

    try {
      sh "jobs-output-list --rich --filter=type,length,name ${environment.AGAVE_JOB_ID}"
      sh "jobs-output-get -P ${environment.AGAVE_JOB_ID} funwave-${jobType}.out"
    } catch (error) {
      print "Could not get output from job: ${environment.AGAVE_JOB_ID}"
    }
  }
}

return [
    setUpCli: this.&setUpCli,
    cloneRepository: this.&cloneRepository,
    updateApp: this.&updateApp,
    submitJob: this.&submitJob
]
