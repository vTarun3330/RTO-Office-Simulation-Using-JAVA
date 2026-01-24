$ErrorActionPreference = "Stop"
Write-Host "RTO System Setup"

# 1. Java Setup
$javaPath = "C:\Program Files\Java\jdk-23"
if (Test-Path $javaPath) {
    Write-Host "Java 23 found."
    $env:JAVA_HOME = $javaPath
    $env:Path = "$javaPath\bin;$env:Path"
}
else {
    Write-Host "Java 23 check failed. Using system default."
}

# 2. Maven Setup
try {
    mvn -version | Out-Null
    Write-Host "Maven found."
}
catch {
    Write-Host "Installing Maven..."
    $url = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip"
    $zip = "$PSScriptRoot\maven.zip"
    $dir = "$PSScriptRoot\maven"
    
    if (-not (Test-Path "$dir\apache-maven-3.9.6\bin\mvn.cmd")) {
        Invoke-WebRequest -Uri $url -OutFile $zip
        Expand-Archive -Path $zip -DestinationPath $dir -Force
        Remove-Item $zip
    }
    $env:Path = "$dir\apache-maven-3.9.6\bin;$env:Path"
}

# 3. Build and Run
Write-Host "Compiling..."
cmd /c mvn clean compile

if ($?) {
    Write-Host "Running Application..."
    cmd /c mvn javafx:run
}
else {
    Write-Host "Build failed."
}

Write-Host "DONE"
