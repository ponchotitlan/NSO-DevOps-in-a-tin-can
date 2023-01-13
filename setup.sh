#!/bin/bash

## NSO-DevOps-in-a-tin-can
## Author: @ponchotitlan
##
## This bash script initializes the containers required for a full CICD NSO demonstration
## Additionally, Python dependencies are installed, as mentioned in the requirements.txt file of this repository
## 
## The following containers are deployed (in order):
## - Gitlab-CE
## - Jenkins
## 
## The following directories are created:
## - ${HOME}/gitlab
## 
## IMPORTANT!
## Docker must have access permission to the ${HOME} directory of the host system
## The deployment of the containers aforementioned was tested on Mac M1, which means that it is ideal for ARM64 architectures

echo "\\nWarming up the contents of this tin can! 🥫🥫🥫\\n"

echo "Setting up the Gitlab-CE container. This is our Control Versioning System (Setup will take some time) ... 🦊\\n"
mkdir -p ${HOME}/gitlab
export GITLAB_HOME=/${HOME}/gitlab
cd gitlab-ce
docker-compose up -d 2> gitlab_setup.log

echo "Now, let's wait for Gitlab-CE web portal to be available ... 🦊\\n"
until $(curl --output /dev/null --silent --head --fail localhost:8080); do
    printf '⏳'
    sleep 5
done

gitlab_pwd=`docker exec -it gitlab-ce grep 'Password:' /etc/gitlab/initial_root_password`
echo "Success! You can login now to Gitlab-CE with the following information\\n
    🦊 Address: localhost:8080
    👤 User: root
    🔑 ${gitlab_pwd}\\n
You can change these credentials later on via the platform.
These credentials are available in the log file /gitlab-ce/gitlab_setup.log in this repository"

printf """
Success! You can login now to Gitlab-CE with the following information\\n
🦊 localhost:8080\\n
👤 User: root\\n
🔑 Address: ${gitlab_pwd}\\n\\n
""" >> gitlab_setup.log

echo "Setting up the Jenkins container. This is our orchestrator (Setup will take some time as well) ... 💂\\n"
mkdir -p ${HOME}/jenkins
export JENKINS_HOME=/${HOME}/jenkins
cd jenkins
docker-compose up -d 2> jenkins_setup.log

echo "Likewise, let's wait for Jenkins web portal to be available ... 💂\\n"
jenkins_ok_check=False
until jenkins_ok_check=='success'; do
     jenkins_ok_check=$(curl --output /dev/null --silent --head --fail localhost:4040 && echo "success")
     printf '⏳'
     sleep 5
done

jenkins_pwd=`docker exec -it jenkins cat /var/jenkins_home/secrets/initialAdminPassword`