#!/bin/bash

## NSO-DevOps-in-a-tin-can
## Author: @ponchotitlan
##
## This bash script initializes the containers required for a full CICD NSO demonstration
## Additionally, Python dependencies are installed, as mentioned in the requirements.txt file of this repository
## 
## The following containers are deployed (in order):
## - Gitlab-CE
## - Gitlab runner
## 
## The following directories are created:
## - ${HOME}/gitlab
## 
## IMPORTANT!
## Docker must have access permission to the ${HOME} directory of the host system
## The deployment of the containers aforementioned was tested on Mac M1, which means that it is ideal for ARM64 architectures

echo "\\nWarming up the contents of this tin can! 🥫🥫🥫\\n"

echo "\\nFirst, let's install some required python dependencies ..."
pip3 install -r requirements.txt

echo "Setting up the Gitlab-CE container. This is our Control Versioning System (Setup will take some time) ... 🦊\\n"
mkdir -p ${HOME}/gitlab
export GITLAB_HOME=/${HOME}/gitlab
docker-compose up -d 2> gitlab_setup.log

echo "Now, let's wait for Gitlab-CE web portal to be available ... 🦊\\n"
while true
do
    if curl -I "localhost:4040" 2>&1 | grep -w "302" ; then
        break
    else
        printf '⏳'
        sleep 5
    fi
done

#If login root@cisco123 doesn't work, delete the HOME/gitlab folder!
echo "Success! You can login now to Gitlab-CE with the following information\\n
    🦊 Address: localhost:4040
    👤 User: root
    🔑 Password: cisco123

You can change these credentials later on via the platform.
These credentials are available in the log file /gitlab-ce/gitlab_setup.log in this repository\\n"

printf """
Success! You can login now to Gitlab-CE with the following information\\n
🦊 Address: localhost:4040\\n
👤 User: root\\n
🔑 Password: cisco123
""" >> gitlab_setup.log