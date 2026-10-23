param([ValidateSet('xampp','demo','mysql','test','package')][string]$Mode='xampp')
# Nhận chế độ chạy từ dòng lệnh; dừng ngay khi gặp lỗi và làm việc tại thư mục dự án.
$ErrorActionPreference='Stop'
Set-Location $PSScriptRoot
# Tìm JDK từ JAVA_HOME hoặc thư mục .jdks mà IntelliJ thường dùng để cài JDK.
$javaCandidates=@($env:JAVA_HOME)+@(Get-ChildItem (Join-Path $env:USERPROFILE '.jdks') -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | ForEach-Object FullName)
$selectedJava=$null
foreach($candidate in $javaCandidates){
  if($candidate -and (Test-Path (Join-Path $candidate 'bin/javac.exe'))){
    $versionText=(& (Join-Path $candidate 'bin/javac.exe') -version 2>&1 | Out-String)
    if($versionText -match 'javac\s+(\d+)' -and [int]$Matches[1] -ge 21 -and [int]$Matches[1] -le 24){$selectedJava=$candidate;break}
  }
}
if(-not $selectedJava){throw 'Can JDK 21 (hoac 22-24). Cai JDK va dat JAVA_HOME, sau do chay lai.'}
$env:JAVA_HOME=$selectedJava
# Ưu tiên Maven có sẵn; nếu chưa có thì tải bản Maven riêng vào .tools của dự án.
$mavenCommand=Get-Command mvn.cmd -ErrorAction SilentlyContinue
if($mavenCommand){$maven=$mavenCommand.Source}else{
  $version='3.9.16'
  $maven=Join-Path $PSScriptRoot ".tools/apache-maven-$version/bin/mvn.cmd"
  if(-not (Test-Path $maven)){
    New-Item -ItemType Directory -Force '.tools' | Out-Null
    $url="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/apache-maven-$version-bin.zip"
    $zip=Join-Path $PSScriptRoot '.tools/maven.zip'
    Invoke-WebRequest $url -OutFile $zip
    # So sánh SHA-512 để phát hiện file tải về bị lỗi trước khi giải nén/chạy.
    $expected=((Invoke-WebRequest "$url.sha512").Content -split '\s+')[0]
    if((Get-FileHash $zip -Algorithm SHA512).Hash -ne $expected){throw 'Maven checksum mismatch'}
    Expand-Archive $zip -DestinationPath '.tools' -Force
  }
}
# Giữ thư viện Maven trong dự án; demo bật H2, mysql dùng cấu hình MySQL mặc định.
$arguments=@('-Dmaven.repo.local=.tools/repository')
switch($Mode){
  'test' {$arguments+='test'}
  'package' {$arguments+='package'}
  'demo' {$arguments+=@('spring-boot:run','-Dspring-boot.run.profiles=demo')}
  'mysql' {$arguments+=@('spring-boot:run','-Dspring-boot.run.profiles=mysql')}
  'xampp' {$arguments+=@('spring-boot:run','-Dspring-boot.run.profiles=xampp')}
}
& $maven @arguments
exit $LASTEXITCODE
