import groovy.json.JsonOutput


node {
  currentBuild.result = "SUCCESS"

  env.AGAVE_TENANTS_API_BASEURL = "https://sandbox.agaveplatform.org/tenants"
  env.MACHINE_NAME = "sandbox"
  env.AGAVE_APP_NAME = "funwave-tvd-build-${env.AGAVE_USERNAME}"
  env.AGAVE_CLIENT_NAME = "jenkins-cli-${env.AGAVE_USERNAME}"

  try {

    stage("Set up Agave CLI") {
      try {
        sh "tenants-list"
        sh "tenants-init -t sandbox"
      } catch (error) {
        print "Tenant already initialized"
      }

      try {
        sh "clients-delete -u '${env.AGAVE_USERNAME}' -p '${env.AGAVE_PASSWORD}' ${env.AGAVE_APP_NAME}"
      } catch (error) {
        print "Cannot delete client: ${env.AGAVE_APP_NAME} does not exist"
      }

      try {
        sh "clients-create -u '${env.AGAVE_USERNAME}' -p '${env.AGAVE_PASSWORD}' -N '${env.AGAVE_APP_NAME}' -S"
      } catch (error) {
        print "Cannot create client: ${env.AGAVE_APP_NAME} already exists"
      }

      try {
        sh "auth-tokens-create -u '${env.AGAVE_USERNAME}' -p '${env.AGAVE_PASSWORD}' "
      } catch (error) {
        print "Cannot create auth tokens for CLI. Aborting..."
        throw error
      }

    }

    stage("Clone Funwave TVD") {
      checkout scm
    }

    stage("Update app") {
      sh "apps-addupdate -F funwave-build-app.txt"

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
        name: "funwave-build",
        appId: "${env.AGAVE_USERNAME}-${env.MACHINE_NAME}-funwave-dbuild-1.0",
        maxRunTime: "00:10:00",
        archive: false,
        notifications: [notificationsSpecs],
        parameters: parametersSpecs
      ]

      def jsonContent = JsonOutput.toJson(appSpecs)
      writeFile(file: "funwave-build-job.json", text: jsonContent)
    }

    stage("Submit job") {
      def submitOutput = sh(returnStdout: true, script: "jobs-submit -F funwave-build-job.json")
      env.BUILD_JOB_ID = submitOutput.split(" ")[-1]
      sleep 90
      sh "jobs-output-get -r ${env.BUILD_JOB_ID} funwave-build.out"
    }

  } catch (error) {
    currentBuild.result = "FAILURE"
    throw error
  }
}
