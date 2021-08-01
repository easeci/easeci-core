# In order to run this script correctly,
# first of all run at least one pipeline on EaseCI Core Master node
import requests
import json
import time

iterations = 200 # how many request will be sent
interval = 2 # seconds

def publish_log(uuid):
    for i in range(iterations):
        payload = {
            "pipelineContextId": uuid,
            "workerNodeId": "488ff302-7ea6-471f-9fd7-c661cf6f4e99",
            "workerNodeHostname": "python_script",
            "incomingLogEntries": [
                {"title": "Python Script Test", "content": "Test content from python script", "header": "[TEST]", "timestamp": 1627329435}
            ]
        }
        request_body = json.dumps(payload)
        requests.post('http://localhost:5050/api/v1/ws/logs', request_body)
        print('Log sent to master node, iteration=' + str(i+1) + ' of ' + str(iterations))
        time.sleep(interval)


print('Fetching all available running context')
response = requests.get('http://localhost:5050/api/v1/pipeline/runtime/list')
ctx_list = response.json()
if len(ctx_list) == 0:
    print('No context is running')
else:
    first_ctx_uuid = ctx_list[0]['pipelineContextId']
    print('Starting publishing to context: ' + first_ctx_uuid)
    publish_log(first_ctx_uuid)


