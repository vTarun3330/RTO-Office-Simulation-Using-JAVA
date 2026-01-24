# RTO System - Setup and Run Script
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   RTO System - Environment Setup" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Setup Java
$javaPath = "C:\Program Files\Java\jdk-23"
if (Test-Path $javaPath) {
    Write-Host "✅ Java 23 found at: $javaPath" -ForegroundColor Green
    $env:JAVA_HOME = $javaPath
    $env:Path = "$javaPath\bin;$env:Path"
}
else {
    Write-Host "⚠️  Java 23 not found at standard location. Checking system..." -ForegroundColor Yellow
}

# 2. Setup Maven (Install locally if missing)
Write-Host "`nchecking Maven..." -ForegroundColor Cyan
try {
    mvn -version | Out-Null
    Write-Host "✅ Maven is already installed." -ForegroundColor Green
}
catch {
    Write-Host "⚠️  Maven not found. Installing local instance..." -ForegroundColor Yellow
    $mavenUrl = "https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    $mavenZip = "$PSScriptRoot\maven.zip"
    $mavenDir = "$PSScriptRoot\maven"
    
    if (-not (Test-Path "$mavenDir\apache-maven-3.9.6\bin\mvn.cmd")) {
        Write-Host "   Downloading Maven..." -ForegroundColor Gray
        Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip
        Write-Host "   Extracting Maven..." -ForegroundColor Gray
        Expand-Archive -Path $mavenZip -DestinationPath $mavenDir -Force
        Remove-Item $mavenZip
    }
    
    $env:Path = "$mavenDir\apache-maven-3.9.6\bin;$env:Path"
    if (Test-Path "$mavenDir\apache-maven-3.9.6\bin\mvn.cmd") {
        Write-Host "✅ Maven installed locally." -ForegroundColor Green
    }
    else {
        Write-Host "❌ Failed to install Maven." -ForegroundColor Red
        exit
    }
}

# 3. Install Eclipse (via Winget)
Write-Host "`nChecking Eclipse..." -ForegroundColor Cyan
if (Get-Command eclipse -ErrorAction SilentlyContinue) {
    Write-Host "✅ Eclipse is potentially installed (found in path)." -ForegroundColor Green
}
else {
    Write-Host "ℹ️  attempting to install Eclipse IDE via Winget..." -ForegroundColor Gray
    try {
        if (Get-Command winget -ErrorAction SilentlyContinue) {
            winget install -e --id EclipseFoundation.Eclipse.Java --source winget --accept-package-agreements --accept-source-agreements
        }
        else {
            Write-Host "⚠️  Winget not found. Skipping Eclipse installation." -ForegroundColor Yellow
        }
    }
    catch {
        Write-Host "⚠️  Eclipse installation failed. Continuing..." -ForegroundColor Yellow
    }
}

# 4. Compile and Run
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "   Building and Running Application" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

Write-Host "Compiling..." -ForegroundColor Gray
mvn clean compile

if ($?) {
    Write-Host "`n✅ Build Successful. Launching Application..." -ForegroundColor Green
    mvn javafx:run
}
else {
    Write-Host "`n❌ Build Failed. Please check the errors above." -ForegroundColor Red
}

Write-Host "`nDONE"
