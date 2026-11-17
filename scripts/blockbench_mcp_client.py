"""Small desktop Blockbench MCP client used by the reproducible art builders."""
import json, urllib.request

class Blockbench:
    def __init__(self):
        self.headers = {'Accept': 'application/json, text/event-stream', 'Content-Type': 'application/json'}
        self.request('initialize', {'protocolVersion': '2024-11-05', 'capabilities': {}, 'clientInfo': {'name': 'Revo art builder', 'version': '1'}})

    def request(self, method, params):
        data = json.dumps({'jsonrpc': '2.0', 'id': 1, 'method': method, 'params': params}).encode()
        req = urllib.request.Request('http://localhost:3000/bb-mcp', data=data, headers=self.headers)
        with urllib.request.urlopen(req, timeout=120) as response:
            if response.headers.get('mcp-session-id'): self.headers['Mcp-Session-Id'] = response.headers['mcp-session-id']
            result = json.loads(response.read())
        if result.get('error'): raise RuntimeError(result['error'])
        return result['result']

    def call(self, name, args):
        result = self.request('tools/call', {'name': name, 'arguments': args})
        if result.get('isError'): raise RuntimeError(result)
        return result

def literal(value):
    return json.dumps(value).replace('/', '\\u002f')

def embedded(result):
    for block in result['content']:
        if block['type'] == 'resource' and 'text' in block.get('resource', {}): return block['resource']['text']
        if block['type'] == 'text':
            try: meta = json.loads(block['text'])
            except ValueError: continue
            if meta.get('content') and not meta.get('truncated'): return meta['content']
    raise RuntimeError('Export did not return a complete file')
