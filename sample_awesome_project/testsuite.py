import requests,json

TEST_PAYLOAD = """
{
    "router-rfs:access-list-router-rfs": [
      {
        "device": "test-ios-0",
        "access_list": [
          {
            "id": "5",
            "action": "permit",
            "destination": "10.2.3.6"
          }
        ]
      }
    ]
}
"""

HEADERS = {
    'Content-Type': 'application/yang-data+json', 
    'Accept': 'application/yang-data+json', 
    'Authorization': 'Basic YWRtaW46YWRtaW4='
    }

response = requests.patch(
    url = "http://127.0.0.1:8089/restconf/data/router-rfs:access-list-router-rfs", 
    headers = HEADERS, 
    data = TEST_PAYLOAD
    )

print(response)
print(response.content)