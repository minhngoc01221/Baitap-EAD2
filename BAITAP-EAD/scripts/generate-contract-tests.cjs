// Sinh Postman collection từ chính OpenAPI: chạy trên mock trước, rồi cùng collection chạy trên backend.
const fs=require('node:fs'),yaml=require('js-yaml');
const spec=yaml.load(fs.readFileSync('src/main/resources/shop-api.yaml','utf8'));
const items=[];
for(const [path,ops] of Object.entries(spec.paths))for(const [method,op] of Object.entries(ops)){
 let url='{{baseUrl}}'+path.replaceAll('{id}','1');
 const params=(op.parameters||[]).filter(p=>p.in==='query'&&['page','size','sort'].includes(p.name));
 if(params.length)url+='?'+params.map(p=>p.name+'='+encodeURIComponent(p.schema.default)).join('&');
 const request={method:method.toUpperCase(),header:[{key:'Authorization',value:'Bearer {{token}}'}],url};
 const bodySchema=op.requestBody?.content['application/json'].schema;
 function sample(s){if(s.$ref)return sample(spec.components.schemas[s.$ref.split('/').pop()]);if(s.example!==undefined)return s.example;if(s.enum)return s.enum[0];if(s.type==='object')return Object.fromEntries(Object.entries(s.properties).filter(([k])=>!s.required||s.required.includes(k)).map(([k,v])=>[k,sample(v)]));if(s.type==='array')return [sample(s.items)];return s.type==='string'?'sample':1;}
 if(bodySchema){request.header.push({key:'Content-Type',value:'application/json'});request.body={mode:'raw',raw:JSON.stringify(sample(bodySchema),null,2)};}
 const status=Object.keys(op.responses).find(s=>s.startsWith('2'));
 const schema=op.responses[status].content?.['application/json'].schema;
 // Postman hỗ trợ JSON Schema; giải tham chiếu schema trước khi đưa vào assertion.
 function expand(s){if(!s||typeof s!=='object')return s;if(s.$ref)return expand(spec.components.schemas[s.$ref.split('/').pop()]);return Object.fromEntries(Object.entries(s).filter(([k])=>!['example','format'].includes(k)).map(([k,v])=>[k,Array.isArray(v)?v.map(expand):typeof v==='object'?expand(v):v]));}
 const checks=[`pm.test('HTTP ${status}',()=>pm.response.to.have.status(${status}));`];
 if(schema)checks.push(`pm.test('Response matches OpenAPI schema',()=>pm.response.to.have.jsonSchema(${JSON.stringify(expand(schema))}));`);
 if(status==='201')checks.push("pm.test('Location header exists',()=>pm.expect(pm.response.headers.get('Location')).to.be.a('string')); ");
 if(status==='204')checks.push("pm.test('No body',()=>pm.expect(pm.response.text()).to.eql('')); ");
 items.push({name:op.operationId,request,event:[{listen:'test',script:{type:'text/javascript',exec:checks}}]});
}
fs.mkdirSync('tests/contracts',{recursive:true});
fs.writeFileSync('tests/contracts/shop-api.postman_collection.json',JSON.stringify({info:{name:'Shop API - all 26 operations (mock contract)',schema:'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'},variable:[{key:'baseUrl',value:'http://localhost:4010'},{key:'token',value:'mock-session-token'}],item:items},null,2));
console.log('Contract collection:',items.length,'operations');
