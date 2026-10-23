// Máy chủ Node đơn giản phục vụ prototype; không chứa logic nghiệp vụ Shop API.
const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');
const root = path.resolve(__dirname, '../src/main/resources/static');
http.createServer((req, res) => {
  // Giới hạn file trong static; frontend mock cũng cần CSS, JS và ảnh SVG.
  const pathname = new URL(req.url, 'http://localhost').pathname;
  const file = path.resolve(root, '.' + (pathname === '/' ? '/index.html' : pathname));
  if(!file.startsWith(root + path.sep) || !fs.existsSync(file) || !fs.statSync(file).isFile()){res.writeHead(404);res.end();return;}
  const types={'.html':'text/html; charset=utf-8','.css':'text/css; charset=utf-8','.js':'application/javascript; charset=utf-8','.svg':'image/svg+xml'};
  res.writeHead(200, {'Content-Type':types[path.extname(file)] || 'application/octet-stream'});
  // Đọc file HTML theo luồng rồi gửi về trình duyệt với encoding UTF-8.
  fs.createReadStream(file).pipe(res);
}).listen(5500, '127.0.0.1', () => console.log('UI: http://127.0.0.1:5500'));
