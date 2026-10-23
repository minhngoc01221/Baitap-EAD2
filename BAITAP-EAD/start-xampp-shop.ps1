param([string]$XamppRoot='D:\Xam')
$ErrorActionPreference='Stop'
# Instance Shop dùng vùng dữ liệu riêng, không khởi động/sửa vùng data cũ của các dự án khác.
$taskMysql=Join-Path $XamppRoot 'mysql/bin/mysqld.exe'
$taskConfig=Join-Path $XamppRoot 'mysql/shop-data/my.ini'
if(-not (Test-Path -LiteralPath $taskConfig)){throw 'Chua co mysql/shop-data/my.ini. Xem docs/XAMPP.md.'}
if(Get-NetTCPConnection -State Listen -LocalPort 3308 -ErrorAction SilentlyContinue){Write-Host 'Cong 3308 dang chay. Shop dung MariaDB XAMPP tai 127.0.0.1:3308.';exit 0}
New-Item -ItemType Directory -Force (Join-Path $PSScriptRoot 'tmp') | Out-Null
Start-Process -FilePath $taskMysql -ArgumentList @("--defaults-file=`"$taskConfig`"",'--bind-address=127.0.0.1','--console') -WorkingDirectory (Join-Path $XamppRoot 'mysql') -WindowStyle Hidden -RedirectStandardOutput (Join-Path $PSScriptRoot 'tmp/xampp-shop.stdout.log') -RedirectStandardError (Join-Path $PSScriptRoot 'tmp/xampp-shop.stderr.log')
Write-Host 'Da khoi dong MariaDB XAMPP Shop (3308). Tiep theo chay ShopApplication-XAMPP trong IntelliJ.'
