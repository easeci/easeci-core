import requests
import json
import time
import os
import base64

def payload_for_save_easefile(filename, content_base64):
    return json.dumps({
        "filename": filename_to_send_filename,
        "encodedEasefileContent": content_base64
    })

def payload_for_parse_easefile(filename):
    return json.dumps({
        "filename": filename,
        "source": "EASEFILE_NAME"
    })

def run_pipeline(pipeline_id):
    request_body = json.dumps({
            "pipelineId": pipeline_id
        })
    response = requests.post('http://localhost:5050/pipeline/runtime/run', request_body)
    status = response.json()['pipelineRunStatus']
    print(response.json()['message'])


filename = 'Easefile_helloworld'

pwd = os.getcwd()
splitted_pwd = pwd.split('/')
result = ''
for i, part in enumerate(splitted_pwd):
    if i+1 == len(splitted_pwd):
        break
    if len(part) > 0:
        result = result + '/' + part
absolute_easefile_path = result + '/workspace/' + filename
easefile_suffix = int(time.time())
filename_to_send_filename = filename + str(easefile_suffix)

file_content = ''
with open(absolute_easefile_path, 'r') as reader:
    file_content = reader.read()
    encoded = base64.b64encode(file_content.encode('ascii'))
    file_content = encoded.decode('ascii')


# save Easefile_helloworld
request_body = payload_for_save_easefile(filename_to_send_filename, file_content)
response = requests.post('http://localhost:5050/api/v2/easefile/save', request_body)
filename_saved = response.json()['filename']
print('Added Easefile with name: ' + filename_saved)


# parse Easefile_helloworld to receive pipeline object
time.sleep(2)
response = requests.post('http://localhost:5050/api/v2/parse', payload_for_parse_easefile(filename_saved))
pipeline_id = response.json()['easefileParseResult']['pipelineId']
is_success = response.json()['isSuccessfullyDone']
print(response.json())

if is_success:
    print('Successfully parsed Easefile and Pipeline received with pipelineId: ' + pipeline_id)
    time.sleep(2)
    run_pipeline(pipeline_id)
else:
    print('Could not parse Easefile')
