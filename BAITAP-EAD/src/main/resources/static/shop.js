'use strict';
// SPA không cần framework: state giữ dữ liệu, render dựng DOM, API là nguồn dữ liệu nghiệp vụ duy nhất.
const $=id=>document.getElementById(id), money=n=>new Intl.NumberFormat('vi-VN',{style:'currency',currency:'VND'}).format(n);
const state={categories:[],products:[],page:0,pages:0,myPage:0,adminPage:0,inventoryPage:0,register:false,pendingRoute:null,auth:null,cart:[]};
try{state.auth=JSON.parse(sessionStorage.getItem('nova-auth'));state.cart=JSON.parse(localStorage.getItem('nova-cart'))||[];}catch{}
if(location.port==='5500')$('api-base').value='http://localhost:4010';
// Chỉ dùng textContent cho dữ liệu từ server; tránh diễn giải tên/mô tả sản phẩm thành mã HTML.
function el(tag,text,cls){const e=document.createElement(tag);if(text!==undefined)e.textContent=text;if(cls)e.className=cls;return e;}
function btn(text,fn,cls='outline-button'){const b=el('button',text,cls);b.type='button';b.onclick=action(fn);return b;}
function note(message,error=false){$('notice').textContent=message;$('notice').className=error?'error':'';}
function action(fn,errorId){return async event=>{event?.preventDefault();try{if(errorId)$(errorId).textContent='';await fn(event);}catch(error){if(errorId)$(errorId).textContent=error.message;note(error.message,true);}};}
function image(url,name){const img=el('img');img.alt=name;img.src=/^(\/images\/[a-z-]+\.svg|https:\/\/)/.test(url||'')?url:'/images/keyboard.svg';img.onerror=()=>{img.onerror=null;img.src='/images/keyboard.svg';};return img;}
function badge(status){return el('span',status,'badge '+status);}
function saveAuth(auth){state.auth=auth;if(auth)sessionStorage.setItem('nova-auth',JSON.stringify(auth));else sessionStorage.removeItem('nova-auth');$('account-button').textContent=auth?auth.customer.name:'Đăng nhập';}
function saveCart(){localStorage.setItem('nova-cart',JSON.stringify(state.cart));$('cart-count').textContent=state.cart.reduce((sum,p)=>sum+p.quantity,0);}
// Phiên Bearer gửi qua Authorization, không đặt trong URL/cookie. Nhật ký không ghi mật khẩu hoặc token.
async function api(path,method='GET',body){
 const base=$('api-base').value.trim().replace(/\/$/,'');const target=new URL(base);if(!['http:','https:'].includes(target.protocol))throw Error('API URL phải dùng HTTP hoặc HTTPS.');
 const response=await fetch(base+path,{method,headers:{Accept:'application/json',...(state.auth?{Authorization:'Bearer '+state.auth.token}:{}),...(body?{'Content-Type':'application/json'}:{})},...(body?{body:JSON.stringify(body)}:{})});
 const text=await response.text();let data;try{data=text?JSON.parse(text):null;}catch{throw Error('API trả dữ liệu không phải JSON. Kiểm tra API Base URL.');}
 const safe=data?.token?{...data,token:'[hidden]'}:data;
 $('api-log').textContent=`${method} ${base+path}\nHTTP ${response.status}\nLocation: ${response.headers.get('Location')||'—'}\n${JSON.stringify(safe,null,2)}`;
 if(!response.ok){if(response.status===401)saveAuth(null);throw Error([data?.message,...(data?.details||[])].filter(Boolean).join(' · ')||`HTTP ${response.status}`);}
 return data;
}
function showView(id){for(const name of ['store','orders','admin','success'])$(name+'-view').classList.toggle('hidden',name!==id);}
async function route(){
 const route=location.hash.slice(1)||'shop';
 if(route==='catalog'){showView('store');return;}
 if(route==='orders'||route==='admin'){
  if(!state.auth){state.pendingRoute=route;openAuth();showView('store');return;}
  if(route==='admin'&&state.auth.role!=='ADMIN'){note('Đăng nhập bằng tài khoản quản trị để sử dụng trang này.',true);showView('store');return;}
  showView(route);if(route==='orders')await loadAccount();else await loadAdmin();
 }else if(route==='success')showView('success');else showView('store');
}
async function loadCategories(){
 state.categories=await api('/categories');const selected=$('category').value;$('category').replaceChildren(new Option('Tất cả danh mục',''));
 for(const c of state.categories)$('category').add(new Option(c.name,c.id));$('category').value=selected;renderCategoryTabs();
}
function renderCategoryTabs(){
 const selected=$('category').value,tabs=$('category-tabs');tabs.replaceChildren();
 for(const c of [{id:'',name:'Tất cả sản phẩm'},...state.categories])tabs.append(btn(c.name,async()=>{$('category').value=c.id;state.page=0;renderCategoryTabs();await loadProducts();},String(c.id)===selected?'active':''));
}
async function loadProducts(){
 const query=new URLSearchParams({page:state.page,size:8,sort:$('sort').value});
 for(const [key,id] of [['q','search'],['categoryId','category'],['minPrice','min-price'],['maxPrice','max-price']])if($(id).value)query.set(key,$(id).value);
 const data=await api('/products?'+query);state.products=data.content;state.pages=data.totalPages;
 $('product-count').textContent=data.totalElements+' sản phẩm';$('page-label').textContent=`Trang ${state.page+1} / ${Math.max(1,data.totalPages)}`;
 $('prev-page').disabled=state.page===0;$('next-page').disabled=state.page+1>=state.pages;
 const grid=$('products');grid.replaceChildren();
 for(const p of data.content){
  const card=el('article',undefined,'product-card');const picture=btn('',()=>openDetail(p.id),'product-image');picture.setAttribute('aria-label','Xem '+p.name);picture.append(image(p.imageUrl,p.name),el('span',p.stock>0?'CÒN HÀNG · '+p.stock:'HẾT HÀNG','stock-badge'));
  const meta=el('div',undefined,'product-meta'),info=el('div');info.append(el('h3',p.name),el('p',p.categoryName||'Bộ sưu tập'),el('div',money(p.price),'price'));
  const add=btn('+',()=>addCart(p),'add-button');add.setAttribute('aria-label','Thêm '+p.name+' vào giỏ');add.disabled=p.stock<1;meta.append(info,add);card.append(picture,meta);grid.append(card);
 }if(!data.content.length)grid.append(el('div','Không có sản phẩm phù hợp. Hãy đổi bộ lọc.','empty'));
}
async function openDetail(id){const p=await api('/products/'+id);const root=$('product-detail');root.replaceChildren();const picture=el('div',undefined,'detail-picture');picture.append(image(p.imageUrl,p.name));root.append(picture,el('p',p.categoryName,'eyebrow'),el('h2',p.name),el('p',p.description),el('div',money(p.price),'detail-price'),el('p','Còn '+p.stock+' sản phẩm','muted'));const b=btn('Thêm vào giỏ',()=>{addCart(p);$('detail-dialog').close();},'dark-button full');b.disabled=p.stock<1;root.append(b);$('detail-dialog').showModal();}
function addCart(p){const existing=state.cart.find(x=>x.id===p.id);if((existing?.quantity||0)>=p.stock)throw Error('Số lượng trong giỏ đã bằng tồn kho.');if(existing)existing.quantity++;else state.cart.push({...p,quantity:1});saveCart();note('Đã thêm '+p.name+' vào giỏ.');}
function renderCart(){
 const root=$('cart-items');root.replaceChildren();let total=0;
 for(const p of state.cart){const row=el('div',undefined,'cart-row'),info=el('div',undefined,'item-info'),qty=el('div',undefined,'quantity');qty.append(btn('−',()=>changeQuantity(p.id,-1),''),el('span',p.quantity),btn('+',()=>changeQuantity(p.id,1),''));info.append(el('strong',p.name),qty);row.append(image(p.imageUrl,p.name),info,el('div',money(p.price*p.quantity),'price'),btn('Bỏ',()=>{state.cart=state.cart.filter(x=>x.id!==p.id);saveCart();renderCart();},'remove'));root.append(row);total+=p.price*p.quantity;}
 if(!state.cart.length)root.append(el('p','Giỏ hàng đang trống. Chọn món đồ bạn yêu thích nhé.','empty'));$('cart-total').textContent=money(total);$('checkout').disabled=!state.cart.length;saveCart();
}
function changeQuantity(id,delta){const item=state.cart.find(x=>x.id===id);if(delta>0&&item.quantity>=item.stock)throw Error('Không đủ tồn kho.');item.quantity+=delta;if(item.quantity<1)state.cart=state.cart.filter(x=>x.id!==id);renderCart();}
async function checkout(){
 if(!state.auth){$('cart-dialog').close();openAuth();note('Vui lòng đăng nhập, sau đó mở lại giỏ để đặt hàng.');return;}
 $('checkout').disabled=true;
 try{const order=await api('/orders','POST',{customerId:state.auth.customer.id,items:state.cart.map(p=>({productId:p.id,quantity:p.quantity}))});state.cart=[];saveCart();$('cart-dialog').close();$('success-message').textContent=`Mã đơn #${order.id} · Tổng tiền ${money(order.total)} · ${order.status}`;location.hash='success';await loadProducts();}finally{$('checkout').disabled=!state.cart.length;}
}
function setAuthMode(register){state.register=register;$('auth-title').textContent=register?'Bắt đầu cùng NOVA.':'Chào bạn trở lại.';$('auth-submit').textContent=register?'Tạo tài khoản':'Đăng nhập';$('register-tab').classList.toggle('active',register);$('login-tab').classList.toggle('active',!register);for(const label of document.querySelectorAll('.register-only')){label.classList.toggle('hidden',!register);label.querySelector('input').required=register;label.querySelector('input').disabled=!register;}$('auth-form').elements.password.minLength=register?8:1;$('auth-error').textContent='';}
function openAuth(){setAuthMode(false);if(!$('auth-dialog').open)$('auth-dialog').showModal();}
async function submitAuth(){const data=Object.fromEntries(new FormData($('auth-form')));const auth=await api(state.register?'/registrations':'/sessions','POST',data);saveAuth(auth);$('auth-dialog').close();note('Xin chào, '+auth.customer.name+'.');const target=state.pendingRoute||'shop';state.pendingRoute=null;if(location.hash==='#'+target)await route();else location.hash=target;}
function paginate(data,page,prefix,previous,next){$(prefix).textContent=`Trang ${page+1} / ${Math.max(1,data.totalPages)}`;$(previous).disabled=page===0;$(next).disabled=page+1>=data.totalPages;}
async function loadAccount(){
 const c=await api('/profiles/current');state.auth.customer=c;saveAuth(state.auth);for(const key of ['name','email','phone','address'])$('profile-form').elements[key].value=c[key]||'';
 const data=await api(`/customers/${c.id}/orders?page=${state.myPage}&size=5&sort=id,desc`);paginate(data,state.myPage,'my-page','my-prev','my-next');$('my-orders').replaceChildren();
 for(const o of data.content){const card=el('article',undefined,'order-card'),top=el('div',undefined,'top');top.append(el('strong','#'+o.id+' · '+money(o.total)),badge(o.status));card.append(top,el('p',o.items.map(i=>`${i.productName} × ${i.quantity}`).join(', ')),el('p',new Date(o.createdAt).toLocaleString('vi-VN'),'muted'));if(['PENDING','PAID'].includes(o.status))card.append(btn('Hủy đơn',async()=>{await api('/orders/'+o.id,'PATCH',{status:'CANCELLED'});await loadAccount();await loadProducts();note('Đã hủy và hoàn tồn đơn #'+o.id);}));$('my-orders').append(card);}
 if(!data.content.length)$('my-orders').append(el('p','Bạn chưa có đơn hàng nào.','empty'));
}
async function loadAdmin(){
 const orders=await api(`/orders?page=${state.adminPage}&size=8&sort=id,desc`),products=await api(`/products?page=${state.inventoryPage}&size=8&sort=id,asc`);await loadCategories();
 paginate(orders,state.adminPage,'admin-page','admin-prev','admin-next');paginate(products,state.inventoryPage,'inventory-page','inventory-prev','inventory-next');
 $('admin-stats').replaceChildren();for(const [label,value] of [['ĐƠN HÀNG',orders.totalElements],['SẢN PHẨM',products.totalElements],['DANH MỤC',state.categories.length]]){const item=el('div');item.append(el('span',label),el('strong',value));$('admin-stats').append(item);}
 $('admin-orders').replaceChildren();for(const o of orders.content){const row=el('tr');row.append(el('td','#'+o.id),el('td','#'+o.customerId),el('td',money(o.total)));const status=el('td');status.append(badge(o.status));const controls=el('td');const next=o.status==='PENDING'?['PAID','CANCELLED']:o.status==='PAID'?['SHIPPED','CANCELLED']:[];for(const n of next)controls.append(btn(n,async()=>{await api('/orders/'+o.id,'PATCH',{status:n});await loadAdmin();await loadProducts();note('Đã chuyển đơn #'+o.id+' sang '+n);}));row.append(status,controls);$('admin-orders').append(row);}
 $('admin-products').replaceChildren();for(const p of products.content){const row=el('tr'),stockCell=el('td'),stock=el('input');stock.type='number';stock.min='0';stock.max='2147483647';stock.value=p.stock;stock.setAttribute('aria-label','Tồn kho '+p.name);stockCell.append(stock);const controls=el('td');controls.append(btn('Lưu tồn',async()=>{if(!stock.reportValidity()||!Number.isInteger(Number(stock.value)))return;await api('/products/'+p.id+'/stock','PATCH',{stock:Number(stock.value)});note('Đã lưu tồn kho '+p.name);await loadProducts();}),btn('Sửa',()=>openProduct(p)),btn('Xóa',async()=>{await api('/products/'+p.id,'DELETE');await loadAdmin();await loadProducts();}));row.append(el('td',p.name),el('td',money(p.price)),stockCell,controls);$('admin-products').append(row);}
 $('admin-categories').replaceChildren();for(const c of state.categories){const tag=el('div',undefined,'category-tag'),name=el('input');name.value=c.name;name.minLength=2;name.maxLength=100;name.required=true;name.setAttribute('aria-label','Tên danh mục '+c.id);tag.append(name,btn('Lưu',async()=>{if(!name.reportValidity())return;await api('/categories/'+c.id,'PUT',{name:name.value,description:c.description});await loadAdmin();}),btn('×',async()=>{await api('/categories/'+c.id,'DELETE');await loadAdmin();}));$('admin-categories').append(tag);}
}
function openProduct(p={}){const f=$('product-form');f.reset();f.elements.categoryId.replaceChildren();for(const c of state.categories)f.elements.categoryId.add(new Option(c.name,c.id));for(const k of ['id','name','description','price','stock','categoryId','imageUrl'])if(p[k]!==undefined&&p[k]!==null)f.elements[k].value=p[k];$('product-form-title').textContent=p.id?'Sửa sản phẩm':'Thêm sản phẩm';$('product-error').textContent='';$('product-dialog').showModal();}
// Đăng ký sự kiện ở cuối file; các hàm async đều được action bắt lỗi để UI không im lặng khi API thất bại.
for(const b of document.querySelectorAll('[data-close]'))b.onclick=()=>$(b.dataset.close).close();
$('account-button').onclick=action(async()=>{if(state.auth){location.hash='orders';await route();}else openAuth();});
$('cart-button').onclick=()=>{renderCart();$('cart-error').textContent='';$('cart-dialog').showModal();};$('checkout').onclick=action(checkout,'cart-error');
$('login-tab').onclick=()=>setAuthMode(false);$('register-tab').onclick=()=>setAuthMode(true);$('auth-form').onsubmit=action(submitAuth,'auth-error');
$('filters').onsubmit=action(async()=>{state.page=0;renderCategoryTabs();await loadProducts();});
$('prev-page').onclick=action(async()=>{state.page--;await loadProducts();});$('next-page').onclick=action(async()=>{state.page++;await loadProducts();});
for(const [prev,next,key,loader] of [['my-prev','my-next','myPage',loadAccount],['admin-prev','admin-next','adminPage',loadAdmin],['inventory-prev','inventory-next','inventoryPage',loadAdmin]]){$(prev).onclick=action(async()=>{state[key]--;await loader();});$(next).onclick=action(async()=>{state[key]++;await loader();});}
$('profile-form').onsubmit=action(async()=>{state.auth.customer=await api('/profiles/current','PUT',Object.fromEntries(new FormData($('profile-form'))));saveAuth(state.auth);note('Đã lưu hồ sơ.');});
$('logout-button').onclick=action(async()=>{await api('/sessions','DELETE');saveAuth(null);location.hash='shop';note('Đã đăng xuất.');});
$('refresh-admin').onclick=action(loadAdmin);$('new-product').onclick=()=>openProduct();
$('category-form').onsubmit=action(async()=>{await api('/categories','POST',Object.fromEntries(new FormData($('category-form'))));$('category-form').reset();await loadAdmin();});
$('product-form').onsubmit=action(async()=>{const data=Object.fromEntries(new FormData($('product-form'))),id=data.id;delete data.id;for(const k of ['price','stock','categoryId'])data[k]=Number(data[k]);await api('/products'+(id?'/'+id:''),id?'PUT':'POST',data);$('product-dialog').close();await loadAdmin();await loadProducts();},'product-error');
$('connect-button').onclick=action(async()=>{saveAuth(null);state.page=0;state.adminPage=0;state.inventoryPage=0;state.cart=[];saveCart();location.hash='shop';await initialize();});
window.addEventListener('hashchange',action(route));
async function initialize(){await loadCategories();await loadProducts();$('connection-label').textContent=$('api-base').value.includes(':4010')?'Prism / dữ liệu mẫu, không lưu thay đổi':'Spring Boot / database';await route();}
saveAuth(state.auth);saveCart();action(initialize)();
