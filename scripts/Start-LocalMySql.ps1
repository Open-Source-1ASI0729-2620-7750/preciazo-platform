[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$projectDir = Split-Path -Parent $PSScriptRoot
$workspaceDir = Split-Path -Parent $projectDir
$mysqlBaseDir = Join-Path $workspaceDir '.tools\mysql\mysql-8.4.13-winx64'
$mysqld = Join-Path $mysqlBaseDir 'bin\mysqld.exe'
$mysql = Join-Path $mysqlBaseDir 'bin\mysql.exe'
$stateDir = Join-Path $projectDir '.tools\mysql-local'
$dataDir = Join-Path $stateDir 'data'
$appClient = Join-Path $stateDir 'app-client.cnf'
$rootClient = Join-Path $stateDir 'root-client.cnf'
$environmentFile = Join-Path $projectDir '.env.local'
$bootstrap = Join-Path $stateDir 'bootstrap.sql'
$encoding = New-Object System.Text.UTF8Encoding($false)
if (!(Test-Path -LiteralPath $mysqld)) { throw "MySQL portable not found at $mysqlBaseDir. See README local setup." }

function Test-LocalMySqlReady {
    $savedErrorAction = $ErrorActionPreference
    try {
        # Connection refusal is expected while the process is stopped/starting.
        # Windows PowerShell otherwise turns native stderr into a terminating error.
        $ErrorActionPreference = 'SilentlyContinue'
        $probe = & $mysql "--defaults-file=$appClient" --batch --skip-column-names --execute='SELECT 1' 2>$null
        return ($LASTEXITCODE -eq 0 -and $probe -eq '1')
    } finally { $ErrorActionPreference = $savedErrorAction }
}

# Do not replace a configured instance or overwrite manually supplied environment files.
if (Test-Path -LiteralPath $appClient) {
    if (Test-LocalMySqlReady) { Write-Output 'Local MySQL is already available on 127.0.0.1:3307.'; return }
}
if (Get-NetTCPConnection -LocalPort 3307 -State Listen -ErrorAction SilentlyContinue) {
    throw 'Port 3307 is already in use; no existing server will be stopped or changed.'
}
New-Item -ItemType Directory -Force -Path $stateDir | Out-Null
if (!(Test-Path -LiteralPath $dataDir)) {
    if (Test-Path -LiteralPath $environmentFile) { throw '.env.local already exists. Refusing to replace its configuration.' }
    function New-LocalPassword {
        $bytes = New-Object byte[] 24
        $random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try { $random.GetBytes($bytes) } finally { $random.Dispose() }
        return ([BitConverter]::ToString($bytes)).Replace('-','')
    }
    $rootPassword = New-LocalPassword
    $appPassword = New-LocalPassword
    $testPassword = New-LocalPassword
    $initializationLog = Join-Path $stateDir 'initialization.err'
    $initializationOutput = & $mysqld --no-defaults --initialize "--basedir=$mysqlBaseDir" "--datadir=$dataDir" "--log-error=$initializationLog" 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'MySQL initialization failed. Inspect the ignored local initialization log; it may contain a temporary root password.' }
    $sql = @"
ALTER USER 'root'@'localhost' IDENTIFIED BY '$rootPassword';
CREATE DATABASE preciazo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE preciazo_shopping_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'preciazo_app'@'localhost' IDENTIFIED BY '$appPassword';
CREATE USER 'preciazo_app'@'127.0.0.1' IDENTIFIED BY '$appPassword';
GRANT ALL PRIVILEGES ON preciazo.* TO 'preciazo_app'@'localhost';
GRANT ALL PRIVILEGES ON preciazo.* TO 'preciazo_app'@'127.0.0.1';
CREATE USER 'preciazo_test'@'localhost' IDENTIFIED BY '$testPassword';
CREATE USER 'preciazo_test'@'127.0.0.1' IDENTIFIED BY '$testPassword';
GRANT ALL PRIVILEGES ON preciazo_shopping_test.* TO 'preciazo_test'@'localhost';
GRANT ALL PRIVILEGES ON preciazo_shopping_test.* TO 'preciazo_test'@'127.0.0.1';
"@
    [IO.File]::WriteAllText($bootstrap, $sql, $encoding)
    [IO.File]::WriteAllText($rootClient, "[client]`nuser=root`npassword=$rootPassword`nhost=127.0.0.1`nport=3307`nprotocol=TCP`n", $encoding)
    [IO.File]::WriteAllText($appClient, "[client]`nuser=preciazo_app`npassword=$appPassword`nhost=127.0.0.1`nport=3307`nprotocol=TCP`ndatabase=preciazo`n", $encoding)
    $environment = @"
SPRING_PROFILES_ACTIVE=dev
DATABASE_URL=127.0.0.1
DATABASE_PORT=3307
DATABASE_NAME=preciazo
DATABASE_USER=preciazo_app
DATABASE_PASSWORD=$appPassword
PRECIAZO_MYSQL_TEST_URL=jdbc:mysql://127.0.0.1:3307/preciazo_shopping_test?serverTimezone=UTC
PRECIAZO_MYSQL_TEST_USER=preciazo_test
PRECIAZO_MYSQL_TEST_PASSWORD=$testPassword
"@
    [IO.File]::WriteAllText($environmentFile, $environment, $encoding)
} elseif (!(Test-Path -LiteralPath $rootClient) -or !(Test-Path -LiteralPath $environmentFile)) {
    throw 'Existing data directory has no matching credentials. Refusing to reinitialize or alter it.'
}
$serverArguments = @('--no-defaults', "--basedir=`"$mysqlBaseDir`"", "--datadir=`"$dataDir`"", '--bind-address=127.0.0.1', '--port=3307', '--mysqlx=OFF', '--skip-log-bin', "--pid-file=`"$(Join-Path $stateDir 'server.pid')`"", "--log-error=`"$(Join-Path $stateDir 'server.err')`"")
if (Test-Path -LiteralPath $bootstrap) { $serverArguments += "--init-file=`"$bootstrap`"" }
$server = Start-Process -FilePath $mysqld -ArgumentList $serverArguments -WindowStyle Hidden -PassThru
$ready = $false
for ($attempt = 0; $attempt -lt 60; $attempt++) {
    if ($server.HasExited) { throw 'Local MySQL exited before becoming ready. Inspect the ignored server log.' }
    if (Test-LocalMySqlReady) { $ready = $true; break }
    Start-Sleep -Milliseconds 500
}
if (!$ready) { throw 'MySQL did not become ready on 127.0.0.1:3307.' }
if (Test-Path -LiteralPath $bootstrap) { Remove-Item -LiteralPath $bootstrap }
Write-Output 'MySQL ready on 127.0.0.1:3307. App and test schemas/users are isolated; credentials are stored in ignored local files.'
