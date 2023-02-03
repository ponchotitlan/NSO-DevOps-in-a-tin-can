#!/usr/bin/env groovy
/**
 * NSO-DevOps-in-a-tin-can
 * Author: @ponchotitlan
 * 
 * Pipeline management support functions.
 * To be used in the Jenkinsfile steps definition.
 */

def spinNSOContainer(String imageName, String containerName){
    /**
    * This function creates a new Docker container in the local
    * environment based on the image name provided. The following
    * attributes are assigned to the new container:
    *
    * - Volume ${HOME}/nso_cicd_tincan/neds mapped to /my_neds folder
    * TODO: remove AMD64 architecture from building
    */

    // Spin a brand-new NSO container
    sh(
        script: "docker run -itd --platform=linux/amd64 -p 8089:8080 --name ${containerName} ${imageName}",
        returnStdout: true
    )
    // Copy the nso setup script from the repository into the container
    sh(
        script: "cd /var/jenkins_home/workspace/nso-pipeline-demo_${BRANCH_NAME}/ && docker cp pipeline_utils/nso_setup.sh ${containerName}:/",
        returnStdout: true
    )
    // Execute the nso setup script to enable RESTCONF in port 8089 and system authentication for admin:admin
    sh(
        script: "docker exec -i ${containerName} bash -l -c \"sh nso_setup.sh\"",
        returnStdout: true
    )
    // Start NSO
    sh(
        script: "docker exec -i ${containerName} bash -l -c \"/etc/init.d/ncs start\"",
        returnStdout: true
    )
}

def loadNEDs(String[] neds, String containerName){
    /**
    * This function retrieves the specified NEDs from the 
    * location ${HOME}/nso_cicd_tincan/neds, and copies them into
    * the active NSO container
    */
    neds.each{ ned ->
        sh(
            script: "docker cp ~/../my_neds/${ned}/ ${containerName}:/nso/run/packages/",
            returnStdout: true
        )
    }
}

def loadServicePackages(String[] packages, String containerName){
    /**
    * This function retrieves the specified packages from the 
    * checked repository, copies them into the active NSO container,
    * compiles them, and issues a packages reload in NSO
    */

    // Copy the specified packages into the NSO container
    packages.each{ pack -> 
        sh(
            script: "docker cp /var/jenkins_home/workspace/nso-pipeline-demo_${BRANCH_NAME}/my_packages/${pack}/ ${containerName}:/nso/run/packages/",
            returnStdout: true
        )
    }
    // Compile each of the specified packages
    packages.each{ pack ->
        sh(
            script: "docker exec -i ${containerName} bash -l -c \"cd /nso/run/packages/${pack}/src/ && make clean all\"",
            returnStdout: true
        )
    }
    // Issue a packages reload in NSO
    refresh_result = sh(
        script: "docker exec -i ${containerName} bash -l -c \"echo 'packages reload' | ncs_cli -Cu admin\"",
        returnStdout: true
    )
    print(refresh_result)
}

def spinTestDevices(Map devices, String containerName){
    /**
    * This function spins the specified netsim devices in the NSO container
    * and onboards them on NSO
    */

    // Create netsim devices
    devices.eachWithIndex{ device, index  ->
        if(index == 0){
            sh(
                script: "docker exec -i ${containerName} bash -l -c \"ncs-netsim create-network /nso/run/packages/${device.key}/ 1 ${device.value}\"",
                returnStdout: true
            )
        }
        else{
            sh(
                script: "docker exec -i ${containerName} bash -l -c \"ncs-netsim add-to-network /nso/run/packages/${device.key}/ 1 ${device.value}\"",
                returnStdout: true
            )
        }
    }
    // Start the netsim devices
    print(sh(
        script: "docker exec -i ${containerName} bash -l -c \"ncs-netsim start\"",
        returnStdout: true
    ))
    // Log the netsim list
    print(sh(
        script: "docker exec -i ${containerName} bash -l -c \"ncs-netsim list\"",
        returnStdout: true
    ))    
}

def onboardTestDevices(String containerName){
    /**
    * This function generates the configuration xml file of the created
    * netsim devices and loads it into NSO along with the netsim_authentication.xml
    * file from this repository
    */

    // Copy the netsim_authentication.xml file from the repository into the NSO container
    sh(
        script: "docker cp /var/jenkins_home/workspace/nso-pipeline-demo_${BRANCH_NAME}/pipeline_utils/netsim_authgroup.xml ${containerName}:/",
        returnStdout: true
    )
    // Load the netsim_authentication.xml file into NSO
    sh(
        script: "docker exec -i ${containerName} bash -l -c \"ncs_load -l -m netsim_authgroup.xml\"",
        returnStdout: true
    )
    // Generate device configuration devices.xml file
    sh(
        script: "docker exec -i ${containerName} bash -l -c \"ncs-netsim ncs-xml-init > devices.xml\"",
        returnStdout: true
    )
    // Load the devices.xml file into NSO
    sh(
        script: "docker exec -i ${containerName} bash -l -c \"ncs_load -l -m devices.xml\"",
        returnStdout: true
    )
    // Issue a devices sync-from in NSO
    sync_result = sh(
        script: "docker exec -i ${containerName} bash -l -c \"echo 'devices device * sync-from' | ncs_cli -Cu admin\"",
        returnStdout: true
    )
    print(sync_result)
}

def runTestBed(){
    /**
    * This function executes the unit tests within
    * the testsuite.py file from the repository against
    * the RESTCONF interface of the NSO container
    */

    // Installation of any required python libraries as per the requirements.txt file
    sh(
        script: "cd /var/jenkins_home/workspace/nso-pipeline-demo_${BRANCH_NAME}/ && pip install -r requirements.txt",
        returnStdout: true
    )
    // Execution of the test suite
    print(sh(
        script: "cd /var/jenkins_home/workspace/nso-pipeline-demo_${BRANCH_NAME}/ && python3 testsuite.py",
        returnStdout: true
    ))
}

def createArtifacts(String[] packages, String containerName){
    /**
    * This function retrieves the compiled packages from the
    * NSO container and pushes them in the /releases folder
    * within the repository
    */

    // Generate timestamp for artifacts suffix
    Date date = new Date()
    String thisDate = date.format("dd-MM-yyyy")
    String thisTime = date.format("HH-mm-ss")

    // Create a releases folder in the Jenkins workdir
    sh(
        script: "cd /var/jenkins_home/workspace/nso-pipeline-demo_${BRANCH_NAME}/ && mkdir releases/",
        returnStdout: true
    )

    // Create a tar file of each of the specified packages and copy them to the Jenkins workdir
    packages.each{ pack ->
        sh(
            script: "docker exec -i ${containerName} bash -l -c \"cd /nso/run/packages/ && tar -czvf ${pack}_${BRANCH_NAME}_${thisDate}_${thisTime}.tar.gz ${pack}/\"",
            returnStdout: true
        )
        sh(
        script: "docker cp ${containerName}:/nso/run/packages/${pack}_${BRANCH_NAME}_${thisDate}_${thisTime}.tar.gz /var/jenkins_home/workspace/nso-pipeline-demo_${BRANCH_NAME}/releases/",
            returnStdout: true
        )
    }
}

def publishArtifacts(){
    /**
    * This function saves the created artifacts in a local directory
    * The intention of this stage is to upload them in some server like
    * Artifactory - but it doesn't have an open source version :(
    */

    // Commit and push of the specified packages
    sh(
        script: "cd /var/jenkins_home/workspace/nso-pipeline-demo_${BRANCH_NAME}/releases/ && cp * /my_neds/artifacts/",
        returnStdout: true
    )
}

def destroyNSOContainer(String containerName){
    /**
    * This function destroys the container named after the
    * string provided
    */
    sh(
        script: "docker rm -f ${containerName}",
        returnStdout: true
    )
}

return this