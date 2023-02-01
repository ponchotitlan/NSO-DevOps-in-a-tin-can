# -*- mode: python; python-indent: 4 -*-
__author__ = "@ponchotitlan"
__version__ = "1.0.0"
__maintainer__ = "@ponchotitlan"
__status__ = "Release"

"""
This is a testsuite python file intended to demonstrate the CICD capabilities
of NSO unit testing. To be used as part of a stage in Jenkins following
the procedures of the repository:

https://github.com/ponchotitlan/NSO-DevOps-in-a-tin-can

The following Unit Tests are covered:
✅ test_access_list_router_rfs: Test the execution of the access_list_router_rfs service in NSO
"""

import requests,unittest,subprocess

def nso_restconf_patch_request(containerAddress:str, nsoservice:str, payload:str) -> dict:
  """Issues a RESTCONF request to the specified NSO container address using the payload provided

  Args:
      containerAddress (str): IP_address:HTTP_port
      payload (str): Request content in JSON format
      nsoservice (str): Name of the NSO service to query

  Returns:
      dict: Request response from the NSO container
  """
  HEADERS = {
    'Content-Type': 'application/yang-data+json', 
    'Accept': 'application/yang-data+json', 
    'Authorization': 'Basic YWRtaW46YWRtaW4='
    }
  
  requests.patch(
    url = f'http://{containerAddress}/restconf/data/{nsoservice}', 
    headers = HEADERS, 
    data = payload
  )


def nso_cli_command(containerName:str, command:str) -> str:
  """Sending of CLI commands to the specified NSO container

  Args:
      containerName (str): Name of the NSO container
      command (str): CLI command

  Returns:
      str: CLI output
  """
  return str(subprocess.check_output(f'docker exec -i {containerName} bash -l -c \"echo \'{command}\' | ncs_cli -Cu admin\"',shell=True),'utf-8').replace('\\n','\n')


class TestNSOService(unittest.TestCase):

  def test_access_list_router_rfs(self):
    """Test the execution of the access_list_router_rfs service in NSO"""
    # This is the information for the provisioning of our service
    REQUEST_PAYLOAD = """
    {
        "router-rfs:access-list-router-rfs": [
          {
            "device": "test-ios-0",
            "access_list": [
              {
                "id": "30",
                "action": "permit",
                "destination": "10.2.3.8"
              }
            ]
          }
        ]
    }
    """
    
    # This is the output that we are expecting to get when querying the entry in our service
    EXPECTED_PAYLOAD = """access-list-router-rfs device test-ios-0
access_list 30
action      permit
destination 10.2.3.8
!
!

"""

    # First, we perform a service provisioning in our NSO container against a netsim device
    nso_restconf_patch_request(
      containerAddress='192.168.1.80:8089',
      nsoservice='router-rfs:access-list-router-rfs',
      payload=REQUEST_PAYLOAD)
    
    # Then, we query our service to validate that the provisioning was correct
    actual_payload = nso_cli_command(
      containerName='nso6.0',
      command='show running-config access-list-router-rfs device test-ios-0 access_list 30')
    
    # We do some data massaging to match the expected payload as accurately as possible
    final_string =''
    for line in actual_payload.split('\n'):
      final_string += f'{line.strip()}\n'

    # Finally, we issue an assert unit test!
    self.assertEqual(EXPECTED_PAYLOAD,final_string)
    print(f'✅  {self._testMethodName} ({self._testMethodDoc})')
    
if __name__ == '__main__':
  unittest.main()