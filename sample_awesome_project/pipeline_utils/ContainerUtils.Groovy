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
        script: "docker run -itd --platform=linux/amd64 --name ${containerName} ${imageName}",
        returnStdout: true
    )
    // Copy the nso setup script from the repository into the container
    sh(
        script: "cd /var/jenkins_home/workspace/nso-cicd-pipeline/ && docker cp pipeline_utils/nso_setup.sh ${containerName}:/",
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
    packages.each{ pack -> 
        sh(
            script: "docker cp /var/jenkins_home/workspace/nso-cicd-pipeline/my_packages/${pack}/ ${containerName}:/nso/run/packages/",
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