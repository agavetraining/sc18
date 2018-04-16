import jenkins.*
import hudson.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*;
import hudson.model.*
import jenkins.model.*
import hudson.security.*

global_domain = Domain.global()
credentials_store =
  Jenkins.instance.getExtensionList(
    'com.cloudbees.plugins.credentials.SystemCredentialsProvider'
  )[0].getStore()

credentials = new BasicSSHUserPrivateKey(CredentialsScope.GLOBAL,null,"root",new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource("/var/jenkins_home/.ssh/id_rsa"),"","")

credentials_store.addCredentials(global_domain, credentials)

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
def adminUsername = System.getenv('JENKINS_USERNAME') ?: 'admin'
def adminPassword = System.getenv('JENKINS_PASSWORD') ?: 'password'
hudsonRealm.createAccount(adminUsername, adminPassword)
//hudsonRealm.createAccount("charles", "charles")

def instance = Jenkins.getInstance()
instance.setSecurityRealm(hudsonRealm)
instance.save()


def strategy = new GlobalMatrixAuthorizationStrategy()

//  Slave Permissions
//strategy.add(hudson.model.Computer.BUILD,'charles')
//strategy.add(hudson.model.Computer.CONFIGURE,'charles')
//strategy.add(hudson.model.Computer.CONNECT,'charles')
//strategy.add(hudson.model.Computer.CREATE,'charles')
//strategy.add(hudson.model.Computer.DELETE,'charles')
//strategy.add(hudson.model.Computer.DISCONNECT,'charles')

//  Credential Permissions
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.CREATE,'charles')
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.DELETE,'charles')
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.MANAGE_DOMAINS,'charles')
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.UPDATE,'charles')
//strategy.add(com.cloudbees.plugins.credentials.CredentialsProvider.VIEW,'charles')

//  Overall Permissions
//strategy.add(hudson.model.Hudson.ADMINISTER,'charles')
//strategy.add(hudson.PluginManager.CONFIGURE_UPDATECENTER,'charles')
//strategy.add(hudson.model.Hudson.READ,'charles')
//strategy.add(hudson.model.Hudson.RUN_SCRIPTS,'charles')
//strategy.add(hudson.PluginManager.UPLOAD_PLUGINS,'charles')

//  Job Permissions
//strategy.add(hudson.model.Item.BUILD,'charles')
//strategy.add(hudson.model.Item.CANCEL,'charles')
//strategy.add(hudson.model.Item.CONFIGURE,'charles')
//strategy.add(hudson.model.Item.CREATE,'charles')
//strategy.add(hudson.model.Item.DELETE,'charles')
//strategy.add(hudson.model.Item.DISCOVER,'charles')
//strategy.add(hudson.model.Item.READ,'charles')
//strategy.add(hudson.model.Item.WORKSPACE,'charles')

//  Run Permissions
//strategy.add(hudson.model.Run.DELETE,'charles')
//strategy.add(hudson.model.Run.UPDATE,'charles')

//  View Permissions
//strategy.add(hudson.model.View.CONFIGURE,'charles')
//strategy.add(hudson.model.View.CREATE,'charles')
//strategy.add(hudson.model.View.DELETE,'charles')
//strategy.add(hudson.model.View.READ,'charles')

//  Setting Anonymous Permissions
//strategy.add(hudson.model.Hudson.READ,'anonymous')
//strategy.add(hudson.model.Item.BUILD,'anonymous')
//strategy.add(hudson.model.Item.CANCEL,'anonymous')
//strategy.add(hudson.model.Item.DISCOVER,'anonymous')
//strategy.add(hudson.model.Item.READ,'anonymous')

// Setting Admin Permissions
//strategy.add(Jenkins.ADMINISTER, "admin")
strategy.add(Jenkins.ADMINISTER, adminUsername)

// Setting easy settings for local builds
def local = System.getenv("BUILD").toString()
if(local == "local") {
  //  Overall Permissions
  strategy.add(hudson.model.Hudson.ADMINISTER,'anonymous')
  strategy.add(hudson.PluginManager.CONFIGURE_UPDATECENTER,'anonymous')
  strategy.add(hudson.model.Hudson.READ,'anonymous')
  strategy.add(hudson.model.Hudson.RUN_SCRIPTS,'anonymous')
  strategy.add(hudson.PluginManager.UPLOAD_PLUGINS,'anonymous')
}

instance.setAuthorizationStrategy(strategy)
instance.save()