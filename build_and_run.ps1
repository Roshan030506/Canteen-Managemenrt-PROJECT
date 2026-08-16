$ErrorActionPreference = "Stop"

Write-Host "Checking target directories..." -ForegroundColor Cyan
if (!(Test-Path "target/classes")) {
    New-Item -ItemType Directory -Force -Path "target/classes" | Out-Null
}

$sqliteJar = "C:\Users\ACER\.m2\repository\org\xerial\sqlite-jdbc\3.46.1.3\sqlite-jdbc-3.46.1.3.jar"
if (!(Test-Path $sqliteJar)) {
    Write-Error "SQLite JDBC driver not found at $sqliteJar"
}

Write-Host "Compiling Java files..." -ForegroundColor Cyan
$javaFiles = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

& javac -d target/classes -cp "$sqliteJar;target/classes" $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Error "Compilation failed!"
}
Write-Host "Compilation successful!" -ForegroundColor Green

Write-Host "Running Canteen Management System..." -ForegroundColor Cyan
& java -cp "target/classes;$sqliteJar" com.canteen.Main
