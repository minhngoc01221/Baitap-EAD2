// Báo cáo Newman có thể chứa Bearer token thật; chỉ giữ kết quả kiểm thử, che credential trước khi đóng ZIP.
const fs=require('node:fs');
function clean(value){
 if(typeof value==='string')return value.replace(/Bearer [A-Za-z0-9_.-]+/g,'Bearer [hidden]');
 if(!value||typeof value!=='object')return value;
 if(value.type==='Buffer'&&Array.isArray(value.data)){
  try{const text=Buffer.from(value.data).toString();const parsed=JSON.parse(text);return {type:'Buffer',data:[...Buffer.from(JSON.stringify(clean(parsed)))]};}catch{return value;}
 }
 if(Array.isArray(value))return value.map(clean);
 const out={};for(const [k,v]of Object.entries(value)){
  if(['token','password','passwordHash'].includes(k)){out[k]='[hidden]';continue;}
  if(k==='value'&&['token','Authorization','authorization','password'].includes(value.key)){out[k]='[hidden]';continue;}
  if(k==='raw'&&typeof v==='string'){try{out[k]=JSON.stringify(clean(JSON.parse(v)));continue;}catch{}}
  out[k]=clean(v);
 }return out;
}
for(const file of ['docs/evidence/contract-newman.json','docs/evidence/contract-backend-newman.json'])if(fs.existsSync(file)){fs.writeFileSync(file,JSON.stringify(clean(JSON.parse(fs.readFileSync(file,'utf8'))),null,2));console.log('Sanitized',file);}
