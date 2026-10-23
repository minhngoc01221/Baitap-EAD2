// Dùng lại schema assertion của mock, nhưng sắp kịch bản có trạng thái và lấy ID/token từ response thật.
const fs=require('node:fs');const source=JSON.parse(fs.readFileSync('tests/contracts/shop-api.postman_collection.json','utf8'));
const byName=Object.fromEntries(source.item.map(i=>[i.name,i])),items=[];
function add(name,path,body,save){
 const item=structuredClone(byName[name]);item.request.url='{{baseUrl}}'+path;
 if(body!==undefined)item.request.body={mode:'raw',raw:JSON.stringify(body)};
 if(save)item.event[0].script.exec.push(`if(pm.response.code<300)pm.collectionVariables.set('${save}',pm.response.json().${save==='token'?'token':'id'});`);
 items.push(item);
}
add('register','/registrations',{name:'Contract Customer',email:'contract{{nonce}}@example.com',address:'123 Nguyen Trai',phone:'0901234567',password:'Customer123!'});
add('login','/sessions',{email:'admin@shop.local',password:'Admin123!'},'token');
add('getProfile','/profiles/current');add('updateProfile','/profiles/current',{name:'Quan tri vien',email:'admin@shop.local',phone:'0901000001',address:'123 Nguyen Trai, TP HCM'});
add('listCategories','/categories');add('createCategory','/categories',{name:'Contract category'},'categoryId');add('getCategory','/categories/{{categoryId}}');add('updateCategory','/categories/{{categoryId}}',{name:'Contract updated category'});add('deleteCategory','/categories/{{categoryId}}');
add('listCustomers','/customers?page=0&size=10&sort=id,asc');
const customer={name:'Contract Customer',email:'crud{{nonce}}@example.com',address:'123 Nguyen Trai',phone:'0901234567'};
add('createCustomer','/customers',customer,'customerId');add('getCustomer','/customers/{{customerId}}');add('updateCustomer','/customers/{{customerId}}',customer);
add('listProducts','/products?page=0&size=10&sort=price,desc');
const product={name:'Contract Keyboard',description:'Contract verification',price:750000,stock:10,categoryId:1,imageUrl:'/images/keyboard.svg'};
add('createProduct','/products',product,'productId');add('getProduct','/products/{{productId}}');add('updateProduct','/products/{{productId}}',product);add('updateProductStock','/products/{{productId}}/stock',{stock:10});
add('createOrder','/orders',{customerId:'{{customerId}}',items:[{productId:'{{productId}}',quantity:2}]},'orderId');
// ID được Postman thay biến; bỏ dấu nháy cho trường số để server nhận đúng kiểu JSON.
items.at(-1).request.body.raw=items.at(-1).request.body.raw.replace(/"(\{\{(?:customerId|productId)\}\})"/g,'$1');
add('getOrder','/orders/{{orderId}}');add('listOrders','/orders?page=0&size=10&sort=id,desc');add('listCustomerOrders','/customers/{{customerId}}/orders?page=0&size=10&sort=id,desc');add('updateOrderStatus','/orders/{{orderId}}',{status:'PAID'});
// Bản ghi đã gắn đơn phải giữ lịch sử. Tạo bản ghi rời để xác minh DELETE 204 đúng nghiệp vụ.
add('createProduct','/products',{...product,name:'Disposable Contract Product'},'disposableProductId');add('deleteProduct','/products/{{disposableProductId}}');
add('createCustomer','/customers',{...customer,email:'disposable{{nonce}}@example.com'},'disposableCustomerId');add('deleteCustomer','/customers/{{disposableCustomerId}}');
add('logout','/sessions');
const out={...source,info:{...source.info,name:'Shop API - backend contracts, all 26 operations'},event:[{listen:'prerequest',script:{type:'text/javascript',exec:["if(pm.info.iteration===0 && pm.info.requestName==='register')pm.collectionVariables.set('nonce',Date.now().toString());"]}}],variable:[{key:'baseUrl',value:'http://localhost:8080/api/v1'},{key:'token',value:''},{key:'nonce',value:''}],item:items};
fs.writeFileSync('tests/contracts/shop-backend.postman_collection.json',JSON.stringify(out,null,2));console.log(items.length,'requests /',new Set(items.map(i=>i.name)).size,'unique operations');
