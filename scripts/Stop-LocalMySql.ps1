[CmdletBinding()]
param()
$ErrorActionPreference='Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$mysqlAdmin = Join-Path (Split-Path -Parent $projectDir) '.tools\mysql\mysql-8.4.13-winx64\bin\mysqladmin.exe'
$rootClient = Join-Path $projectDir '.tools\mysql-local\root-client.cnf'
if (!(Test-Path -LiteralPath $rootClient)) { throw 'No credentials for this local instance were found.' }
& $mysqlAdmin "--defaults-file=$rootClient" shutdown
if ($LASTEXITCODE -ne 0) { throw 'Could not shut down this local MySQL instance.' }
Write-Output 'The local MySQL instance on port 3307 was stopped.'
