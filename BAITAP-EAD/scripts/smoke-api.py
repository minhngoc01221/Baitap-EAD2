"""Smoke test v2 bằng thư viện chuẩn Python; chạy backend và Prism trước. Tạo dữ liệu thử riêng."""
import json,time,urllib.request,urllib.error
from pathlib import Path
base='http://localhost:8080/api/v1'
checks=[]
token=None
def call(method,path,body=None,status=200,root=base):
    headers={'Content-Type':'application/json'}
    if token: headers['Authorization']='Bearer '+token
    request=urllib.request.Request(root+path,data=json.dumps(body).encode() if body is not None else None,method=method,headers=headers)
    try: response=urllib.request.urlopen(request,timeout=20)
    except urllib.error.HTTPError as error: response=error
    text=response.read().decode();data=json.loads(text) if text else None
    safe={**data,'token':'[hidden]'} if isinstance(data,dict) and 'token' in data else data
    checks.append({'method':method,'path':path,'status':response.status,'response':safe})
    assert response.status==status,checks[-1]
    if status==201: assert response.headers.get('Location')
    return data

# Dùng admin để chuẩn bị catalog; sau đó đổi sang token khách để checkout theo quyền thực tế.
token=call('POST','/sessions',{'email':'admin@shop.local','password':'Admin123!'},201)['token']
category=call('GET','/categories')[0]['id']
p=call('POST','/products',{'name':'Smoke Keyboard','description':'Smoke test v2','price':750000,'stock':5,'categoryId':category,'imageUrl':'/images/keyboard.svg'},201)['id']
auth=call('POST','/registrations',{'name':'Smoke Customer','email':f'smoke{time.time_ns()}@example.com','address':'123 Nguyen Trai','phone':'0901234567','password':'Customer123!'},201)
token=auth['token'];c=auth['customer']['id']
o=call('POST','/orders',{'customerId':c,'items':[{'productId':p,'quantity':2}]},201)
assert o['total']==1500000
assert call('GET',f'/products/{p}')['stock']==3
call('GET',f'/customers/{c}/orders')
call('PATCH',f'/orders/{o["id"]}',{'status':'CANCELLED'})
call('PATCH',f'/orders/{o["id"]}',{'status':'CANCELLED'})
assert call('GET',f'/products/{p}')['stock']==5
call('POST','/orders',{'customerId':c,'items':[{'productId':p,'quantity':99}]},422)
call('GET','/orders',status=403)
call('DELETE','/sessions',status=204)
call('GET','/profiles/current',status=401)
call('GET','/products',root='http://localhost:4010')
output=Path(__file__).resolve().parents[1]/'docs/evidence/http-smoke-v2.json'
output.write_text(json.dumps({'result':'PASS','requests':len(checks),'checks':checks},ensure_ascii=False,indent=2),encoding='utf-8')
print(f'PASS: {len(checks)} HTTP checks; {output}')
