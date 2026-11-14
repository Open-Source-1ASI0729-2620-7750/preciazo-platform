# Dot-source this script to load only this shell's database variables; no setx or global settings.
$environmentFile = Join-Path (Split-Path -Parent $PSScriptRoot) '.env.local'
if (!(Test-Path -LiteralPath $environmentFile)) { throw 'Run scripts/Start-LocalMySql.ps1 first, or configure your own environment.' }
$allowedKeys = @('SPRING_PROFILES_ACTIVE','DATABASE_URL','DATABASE_PORT','DATABASE_NAME','DATABASE_USER','DATABASE_PASSWORD','PRECIAZO_MYSQL_TEST_URL','PRECIAZO_MYSQL_TEST_USER','PRECIAZO_MYSQL_TEST_PASSWORD')
foreach ($line in [IO.File]::ReadAllLines($environmentFile)) {
    if ($line -match '^([A-Z_]+)=(.*)$' -and $allowedKeys -contains $Matches[1]) {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}
