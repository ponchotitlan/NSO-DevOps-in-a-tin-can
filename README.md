# 💻🤖🥫 NSO DevOps in a tin can 🥫🤖💻
A warm bowl of NSO CICD soup straight from a tin can (containerized), yet with a homemade taste. 
Ingredients are: GitLab-CE + NSO + Jenkins + 🧡 

## 🔥 But first, a horror story 🔥

Cisco NSO (Network Services Orchestrator) is the leading framework for our Service Provider and Enterprise network automation and orchestration. Entities of any size and reach can strongly benefit of its support for services development, as it is the power code what allows NetOps teams to unleash their true potential and craft all sorts of functionalities for automating their day-to-day operations, enhancing efficiency and reaching KPIs faster.

Now, imagine that incredibly awesome service which was developed for enhancing the operations of our network. NetOps team is very happy about it, and of course what makes sense now is to add more features. So, the effort estimation is done with the Software Development Team, and new User Stories begin to unroll. After the expected time, the new code is ready and deployed in production.

However, the next morning, the software team is flooded with tickets! The service seems to be unstable as features that were working before are crashing, and the operations can't be done through it no more. _"Guess it will be one more night of debugging and troubleshooting"_, the dev team wonders ... Now, the NetOps team has to fallback to doing things manually, which is taking time and money that automation was already saving.

<p align="center">
  <img src="https://media.tenor.com/QCWto5N6k0EAAAAC/caos-bob.gif" />
</p>

In the Post Mortem analysis, it was determined that many factors alligned to cause the malfunction of the service package: Code which corresponded to other User Stories was modified, tests focused entirely on the new features (no regresive testing), and a wrong version of the service package was put in the production NSO server.

The Software Development Team sits together to find a solution, while they share a warm bowl of soup right out of a practical, containerized tin can which will help them embrace a NetDevOps mindset across all their development lifecycle.

## 🍲 Motivation behind the soup 🍲 

<p align="center">
  <img src="https://i.gifer.com/BAas.gif" />
</p>

The concept of *DevOps* was created to close the gap between the development and operations teams in Software projects, so that the new source code can make its way into a finalized, stable product in the most efficient and error-prone way possible.

TRIVIA: How frequently does Spotify perform product releases?
a) Two months    b) Two weeks    c) Yearly

Answer: b) Two weeks!
From code to our fingertips, the app and the services within are updated with bug fixes and new features in such short time, and most importantly, without any of us users noticing it.

The idea of bringing the goodness of DevOps into the network operations realm derived in the creation of a new term: NetDevOps. It consists on an incremental approach which makes use of the DevOps techniques to automate the development of new services, enhance the quality within, and ensure a proper, flawless deployment in production. The key tool from this mindset is, notoriously, the CICD workflows, or Continuous Integration / Continuous Delivery.

The purpose of this containerized mockup is to showcase a very basic CICD workflow oriented towards NSO Use Case design and deployment. The different stages contemplate the setup required for automated testing, and the release of artifacts as a single source of truth for deployment in production. The purpose is not to execute the services developped right away, but rather to have consistent, error-proof releases ready to be operated by our NetOps users at their earliest convenience.

## 🥫 What's inside the tin can 🥫

The following diagram shows the architecture of the ingredients (containers) within this tomato-ey repository:


<p align="center">
  <img src="images/architecture_01.png" />
</p>

The current version of the project makes use of a Gitlab-CE container as a SCM (Source Code Manager) and a Jenkins container as the CICD platform. For the very basic purposes of NSO testing, Gitlab-CE along with Gitlab Runner is enough. However, in this version of the project it is intended to demonstrate that the communication between Jenkins and Gitlab-CE is possible, allowing for the usage of the rich plugin marketplace that is available for Jenkins.

Our CICD environment makes use of the NSO as Docker project to provide a testing and deployment platform which is dynamically setup and wiped out based on your project's requirements. That means, for every run we can specify the NSO version that our project needs. Moreover, the project uses ncs-netsim virtual devices right inside this NSO container. This is a very simple approach for quick services ad-hoc testing, as we can specify in our environment which are the NEDs, amount of devices, and device names that we want to use for testing. And again, everything is wiped out once our workflow run is complete.

Finally, the project structure provides all the scripts that we need for customizing our CICD environment based on our needs. As of now, the code within is hardcoded for a demo testcase, however the code is well documented to make it easy to navigate and modify.

<p align="center">
  <img src="images/project_structure.png" />
</p>


## 🍜 Warming up the delicious soup 🍜

First of all, let's pour the contents of the tin can into a bowl. This repository has a setup file for spinning up all the containers that we need to get started. 
The system requirements consist on the following:

- Linux-based OS. This repository has been tested on CentOS and MacOS
- Docker engine
- Docker compose
- Python v3.9x

To get started, clone the repository in your host:
```
git clone https://github.com/ponchotitlan/NSO-DevOps-in-a-tin-can.git
```

Once copied, navigate to the repository source and run the following command:
```
sh setup.sh
```

The output shall be similar to the following one:
```

Warming up the contents of this tin can! 🥫🥫🥫

Setting up the Gitlab-CE container. This is our Control Versioning System (Setup will take some time) ... 🦊

Now, let's wait for Gitlab-CE web portal to be available ... 🦊

⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳HTTP/1.1 302 Found
Success! You can login now to Gitlab-CE with the following information

    🦊 Address: localhost:8080
    👤 User: root
    🔑 Password: cisco123

You can change these credentials later on via the platform.
These credentials are available in the log file /gitlab-ce/gitlab_setup.log in this repository

Setting up the Jenkins container. This is our orchestrator (Setup will take some time as well) ... 💂

Likewise, let's wait for Jenkins web portal to be available ... 💂

⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳⏳HTTP/1.1 403 Forbidden
Success! You can login now to Jenkins with the following information

    💂 Address: localhost:4040
    🔑 Unlocking password: 24c86e86888b471bab7ed95049833811

Please perform the initial setup as shown in the README and the Wiki.
These credentials are available in the log file /jenkins/gitlab_setup.log in this repository

```

This will create a series of folders and spin-up the required containers for our CICD system:

- "/gitlab" folder in your ${HOME} directory
- "/jenkins" folder in your ${HOME} directory
- "gitlab-ce" Docker container
- "jenkins" Docker container

```

% docker ps
CONTAINER ID   IMAGE                    COMMAND                  CREATED          STATUS                    PORTS                                                 NAMES
3b82a46a94f9   jenkins/jenkins:lts      "/usr/bin/tini -- /u…"   21 minutes ago   Up 21 minutes             0.0.0.0:50000->50000/tcp, 0.0.0.0:4040->8080/tcp      jenkins
dc2f982239e8   yrzr/gitlab-ce-arm64v8   "/assets/wrapper"        48 minutes ago   Up 48 minutes (healthy)   22/tcp, 0.0.0.0:8080->80/tcp, 0.0.0.0:8443->443/tcp   gitlab-ce

```

The platform web interfaces will be available in the following locations:

- Gitlab-CE: localhost:8080
- Jenkins: localhost:4040

Our Jenkins server requires some additional configurations, such as the installation of Docker and pip. Please refer to this wiki entry for details about this process. An automated way of fulfilling this is on the works :)

