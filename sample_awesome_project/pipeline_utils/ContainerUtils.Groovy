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
    sh(
        script: "docker run -itd --platform=linux/amd64 --name ${containerName} ${imageName}",
        returnStdout: true
    )
 }

 def loadNEDs(String[] neds, String containerName){
    /**
    * This function retrieves the specified NEDs from the 
    * location ${HOME}/nso_cicd_tincan/neds, copies them into
    * the active NSO container, and issues a packages reload
    */
    neds.each{ ned ->
        sh(
            script: "docker cp ~/../my_neds/${ned}/ ${containerName}:/nso/run/packages/",
            returnStdout: true
        )
    }
    echo "Executing packages reload in the NSO container"
    sh(
        script: "docker exec -i ${containerName} bash -l -c \"echo 'packages reload' | ncs_cli -Cu admin\"",
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