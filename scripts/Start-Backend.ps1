[CmdletBinding()]
param()
$ErrorActionPreference='Stop'
$projectDir=Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot 'Start-LocalMySql.ps1')
. (Join-Path $PSScriptRoot 'Use-LocalEnvironment.ps1')
if (!$env:JAVA_HOME) {
    $portableJdk=Join-Path (Split-Path -Parent $projectDir) '.tools\java27\jdk-27.0.1.0.3'
    if (Test-Path -LiteralPath (Join-Path $portableJdk 'bin\javac.exe')) { $env:JAVA_HOME=$portableJdk }
}
try {
    Push-Location $projectDir
    & '.\mvnw.cmd' spring-boot:run
    if ($LASTEXITCODE -ne 0) { throw 'Backend startup failed.' }
} finally { Pop-Location }
