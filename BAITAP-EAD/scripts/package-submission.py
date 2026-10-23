"""Đóng gói bài nộp; bỏ qua thư viện tải về, file build và database thử nghiệm."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
root=Path(__file__).resolve().parents[1]
output=root/'output/shop-api-bai-nop.zip'
output.parent.mkdir(exist_ok=True)
# Danh sách cho phép giúp ZIP không chứa .tools, node_modules hoặc dữ liệu máy cá nhân.
paths=[root/name for name in ['pom.xml','README.md','run.ps1','start-xampp-shop.ps1','compose.yaml','package.json','package-lock.json','.gitignore']]
for folder in ['src','docs','scripts','.run','tests','codegen-templates','database']:
    paths.extend(p for p in (root/folder).rglob('*') if p.is_file())
# ZIP_DEFLATED nén các file; giữ cấu trúc thư mục để giải nén là mở được bằng IntelliJ.
with ZipFile(output,'w',ZIP_DEFLATED) as archive:
    for path in paths: archive.write(path,'shop-api/'+path.relative_to(root).as_posix())
with ZipFile(output) as archive:
    assert archive.testzip() is None
    print(f'{output}: {len(archive.namelist())} files, {output.stat().st_size:,} bytes')
