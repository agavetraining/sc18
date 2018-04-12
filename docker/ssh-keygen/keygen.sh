#!/bin/sh
KEYS_PATH=${KEYS_PATH:-$HOME/.ssh}
PRIVATE_KEY=$KEYS_PATH/id_rsa
PUBLIC_KEY=${PRIVATE_KEY}.pub
#set -x
if [ -n "$ROTATE" ]; then
    [ -n "$DEBUG" ] && echo "Existing keys will be backed before generating new keys"
    OLD_KEY_TIMESTAMP=$(date +%s)
    OLD_PRIVATE_KEY=${KEYS_PATH}/id_rsa-${OLD_KEY_TIMESTAMP}
    OLD_PUBLIC_KEY=${OLD_PRIVATE_KEY}.pub
    #echo "Before rotation...\n"
    #ls -al $KEYS_PATH
    #if [ -f "$KEYS_PATH/authorized_keys" ]; then
    #        cat $KEYS_PATH/authorized_keys
    #fi

    [ -f "$PRIVATE_KEY" ] && mv $PRIVATE_KEY $OLD_PRIVATE_KEY
    [ -f "$PUBLIC_KEY" ] && mv $PUBLIC_KEY $OLD_PUBLIC_KEY
    #echo "After rotation...\n"
    #ls -al $KEYS_PATH
    if [ -f "$KEYS_PATH/authorized_keys" ]; then
        sed -i 's/^.* jovyan@.*$//g' $KEYS_PATH/authorized_keys
    fi
fi

if [ ! -f "$PRIVATE_KEY" ] || [ -n "$REPLACE" ]; then

  #echo "\n\nWriting key pair to ${KEYS_PATH}..."
  if [ -n "$REPLACE" ]; then
    [ -n "$DEBUG" ] && echo "Existing keys will be replaced."
    rm -f $PRIVATE_KEY $PUBLIC_KEY

    # remove old public key from authorized keys
    if [ -f "$KEYS_PATH/authorized_keys" ]; then
        sed -i 's/^.* jovyan@.*$//g' $KEYS_PATH/authorized_keys
    fi
  fi

  /usr/bin/ssh-keygen -q -t rsa -N '' -f $PRIVATE_KEY
  chmod 700 $KEYS_PATH
  chmod 644 $PUBLIC_KEY
  chmod 600 $PRIVATE_KEY

  if [ -f "$KEYS_PATH/authorized_keys" ]; then
#    echo ""
#    echo "Before appending authorized key...\n"
#    cat $KEYS_PATH/authorized_keys
    cat $PUBLIC_KEY >> $KEYS_PATH/authorized_keys
#    echo "After appending authorized key...\n"
#    cat $KEYS_PATH/authorized_keys
  fi

else
  echo "Private key already exists and will be used. You may rotate the current key by setting the \"ROTATE\" environment variable or overwrite it by setting the \"REPLACE\" environment variable."
fi

echo "========= PUBLIC KEY ============"
cat $PUBLIC_KEY
echo "======= END PUBLIC KEY ========="

exit 0