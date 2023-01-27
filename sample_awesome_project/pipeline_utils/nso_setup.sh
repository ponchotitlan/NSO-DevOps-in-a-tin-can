#!/bin/bash

## NSO-DevOps-in-a-tin-can
## Author: @ponchotitlan
##
## Setup of a container based on the cisco-nso-dev image for allowing RESTCONF communication

## Create a log folder in the base location. This is required for NSO startup in this image flavour if no volumes for log/ are mounted
mkdir /log

## Copy the initial config file /etc/ncs/ncs.conf.ini in the same location as ncs.conf for proper NSO startup
cp /etc/ncs/ncs.conf.in /etc/ncs/ncs.conf
CONF_FILE=${CONF_FILE:-/etc/ncs/ncs.conf}

## Enable webUI with no TLS on port 8080
xmlstarlet edit --inplace -N x=http://tail-f.com/yang/tailf-ncs-config \
    --update '/x:ncs-config/x:webui/x:transport/x:tcp/x:enabled' --value "true"\
    $CONF_FILE

## Enablement of PAM authentication with system-auth
## This is intended for using the credentials admin:admin in the testsuite
xmlstarlet edit --inplace -N x=http://tail-f.com/yang/tailf-ncs-config \
    --update '/x:ncs-config/x:aaa/x:pam/x:enabled' --value 'true' \
    --update '/x:ncs-config/x:aaa/x:pam/x:service' --value 'system-auth' \
    --update '/x:ncs-config/x:aaa/x:local-authentication/x:enabled' --value 'false' \
    $CONF_FILE

## Configure CLI style to Cisco (Specific for this use case)
xmlstarlet edit --inplace -N x=http://tail-f.com/yang/tailf-ncs-config \
    --update '/x:ncs-config/x:cli/x:style' --value "c" \
    --subnode '/x:ncs-config/x:cli[not(x:style)]' \
    --type elem -n style \
    --value "c" \
    $CONF_FILE

## Configuration of the "admin:admin" local credentials in the host system
useradd -p $(openssl passwd -1 admin) admin
usermod -a -G ncsadmin admin