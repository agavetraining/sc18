#!/bin/bash

AGAVE_TENANT=${AGAVE_TENANT:-agave.prod}
MACHINE_IP=${MACHINE_IP:-$HOSTNAME}

# check whether we need to configure a tenant
if [[ -n "$AGAVE_TENANT" ]]; then

   # check whether the tenant is already configured
   current_tenant=$(auth-check -v | jq -r '.tenantid')
   if [[ "$?" == "0" ]]; then
        if [[ "$AGAVE_TENANT" == "$current_tenant" ]]; then
            echo "Container was already configured for the $AGAVE_TENANT tenan., Skipping configuration"
        else
            # no existing config. init the tenant from scratch
            tenants-init -t "$AGAVE_TENANT"
        fi
   else
        echo "Container was previously configured for the $current_tenant. Previous configuration will be backed up and a new tenant initialized"
        tenants-init -b -t "$AGAVE_TENANT"
   fi
fi


# initialize a client if not already present and a username and password are present
if [[ -n "$AGAVE_USERNAME" ]] && [[ -n "$AGAVE_PASSWORD" ]]; then

    current_client_secret=$(auth-check -v | jq -r '.apisecret')
    current_client_key=$(auth-check -v | jq -r '.apisecret')

    if [[ -z "$current_client_secret" ]] || [[ -z "current_client_key" ]]; then
        clients-delete "${AGAVE_USERNAME}-jenkins-training"
        clients-create -N "${AGAVE_USERNAME}-jenkins-training" -C "${MACHINE_IP}/jenkins" -D "Jenkins training host for ${AGAVE_USERNAME}"
    else
        echo "Client is already configured for this tenant. Using the existing keys."
    fi


    JENKINS_USERNAME=$AGAVE_USERNAME
    JENKINS_PASSWORD=$AGAVE_PASSWORD

elif [[ -z "$AGAVE_USERNAME" ]]; then

    echo "AGAVE_USERNAME is not set. No client credentials will be set. Use the admin user to login."

elif [[ -z "$AGAVE_PASSWORD" ]]; then

    echo "AGAVE_PASSWORD is not set. No client credentials will be set. Use the default admin password to login."
fi

export JENKINS_USERNAME=${JENKINS_USERNAME:admin}
export JENKINS_PASSWORD=${JENKINS_PASSWORD:changeit}

export JAVA_OPTS="$JAVA_OPTS -DJENKINS_USERNAME=$JENKINS_USERNAME -DJENKINS_PASSWORD=$JENKINS_PASSWORD "

eval "$@"


