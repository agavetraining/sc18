node {
    //git url: 'ssh://jovyan@sandbox:/home/jovyan/FUNWAVE-TVD'

    sh 'rm -rf FUNWAVE-TVD'
    sh 'git clone ssh://jovyan@sandbox:/home/jovyan/FUNWAVE-TVD'
    sh 'cp -r FUNWAVE-TVD/* .'
    //sh 'env | grep -i agave'

    def AGAVE_USERNAME = "DemoAgave"
    def MACHINE_NAME = "jupyter"
    def AGAVE_APP_NAME = "funwave-tvd-jupyter-DemoAgave"

    try{
        sh 'tenants-init -t agave.prod'
        sh "clients-delete $AGAVE_APP_NAME"
    } catch(all) {
        println "No client to delete"
    }
    try{
        sh 'tenants-init -t agave.prod'
        sh "clients-create -S -N $AGAVE_APP_NAME"
    } catch(all) {
        println "Client already exists"
    }
    sh 'auth-tokens-create'


    try {
        // Set Agave environment variables?
        // Write job/app templates out using env variables?
        // Upload build and app files to Agave
        //stage('Upload'){
        //    def AGAVE_STORAGE_SYSTEM_ID = "42" //FIX ME
        //    sh "files-mkdir -S ${AGAVE_STORAGE_SYSTEM_ID} -N automation"
        //    sh "files-upload -S ${AGAVE_STORAGE_SYSTEM_ID} -F funwave-tvd-docker-automation automation"
        //}

        // Update the app definition
        stage('Update'){
            sh 'apps-addupdate -F funwave-tvd-docker-automation/funwave-build-app.txt'
        }

        // Submit funwave-tvd build job to Agave
        stage('Build'){

            // Get WEBHOOK_URL
            WEBHOOK_URL = sh returnStdout: true, script: 'requestbin-create'
            // Remove newline, extra spaces, etc
            WEBHOOK_URL = WEBHOOK_URL.trim()
            println "WEBHOOK_URL = ${WEBHOOK_URL}"


            sh """echo '{
\"name\":\"funwave-build\",
\"appId\": \"${AGAVE_USERNAME}-${MACHINE_NAME}-funwave-dbuild-1.0\",
\"maxRunTime\":\"00:10:00\",
\"archive\": false,
\"notifications\": [
{
  \"url\":\"${WEBHOOK_URL}\",
  \"event\":\"*\",
  \"persistent\":\"true\"
}
],
\"parameters\": {
 \"code_version\":\"latest\"
}
}' | tee funwave-tvd-docker-automation/job.json"""


            sh 'cat funwave-tvd-docker-automation/job.json'

            // Submit job
            OUTPUT = sh returnStdout: true, script: 'jobs-submit -F funwave-tvd-docker-automation/job.json'
            echo "OUTPUT = ${OUTPUT}"
            def JOBID = OUTPUT.tokenize('.')[-1]
            echo "JOBID = ${JOBID}"

        }


    } finally {
        println "Done!"
    }
}
