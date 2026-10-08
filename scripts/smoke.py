"""Check an initialized local demo backend; no production accounts are used."""
import json, os, time, urllib.request
base = os.environ.get('SMOKE_BASE_URL', 'http://127.0.0.1:9001')
def request(path, payload=None):
    data = json.dumps(payload).encode() if payload is not None else None
    req = urllib.request.Request(base + path, data=data, headers={'Content-Type': 'application/json'})
    with urllib.request.urlopen(req, timeout=10) as response:
        return json.load(response)
for attempt in range(90):
    try:
        result = request('/labinfo/basic')
        if result['code'] == 200:
            break
    except (OSError, ValueError):
        pass
    time.sleep(1)
else:
    raise RuntimeError('Backend did not become ready with the demo database')
assert request('/labinfo/contacts')['code'] == 200
result = request('/auth/login', {'username': 'admin', 'password': 'local-demo-change-me'})
assert str(result['code']) == '200' and result['data']['isAdmin'], result
print('Lab info, contacts and demo administrator login passed.')
