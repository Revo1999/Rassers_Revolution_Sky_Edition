"""Use the installed desktop Blockbench MCP over its local Streamable HTTP endpoint."""
from pathlib import Path
import json,sys,requests
ROOT=Path(__file__).resolve().parents[1]
def call(name,args):
    config=json.loads((ROOT/'.local/steel-mcp-session.json').read_text())
    headers={'Content-Type':'application/json','Accept':'application/json, text/event-stream','Mcp-Session-Id':config['session']}
    response=requests.post(config['url'],headers=headers,json={'jsonrpc':'2.0','id':10,'method':'tools/call','params':{'name':name,'arguments':args}},timeout=180)
    response.raise_for_status();result=response.json()
    if 'error' in result:raise RuntimeError(result['error'])
    result=result['result']
    if result.get('isError'):raise RuntimeError(result)
    return result
if __name__=='__main__':
    if sys.argv[1]=='eval':result=call('risky_eval',{'code':'\n'.join(line for line in Path(sys.argv[2]).read_text(encoding='utf-8').splitlines() if not line.lstrip().startswith('//'))})
    else:result=call(sys.argv[1],json.loads(sys.argv[2]) if len(sys.argv)>2 else {})
    print(json.dumps(result,ensure_ascii=True))
