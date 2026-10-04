$ErrorActionPreference = 'Stop'
if ($env:GITHUB_ACTIONS -ne 'true' -or $env:RUNNER_ENVIRONMENT -ne 'github-hosted' -or $env:RUNNER_OS -ne 'Windows') {
    throw 'This installer test is restricted to a disposable hosted Windows runner.'
}
$qa = Join-Path (Get-Location) 'build/boatflix-msi-qa'
New-Item -ItemType Directory -Path $qa -Force | Out-Null
gh release download 1.32 --repo $env:GITHUB_REPOSITORY --pattern 'BOATFLIX-Windows-x64-1.32.msi' --pattern 'SHA256SUMS.txt' --dir $qa
if ($LASTEXITCODE -ne 0) { throw 'Previous published installer could not be downloaded.' }
$old = Join-Path $qa 'BOATFLIX-Windows-x64-1.32.msi'
$current = Join-Path (Get-Location) "composeApp/build/compose/release-msis/BOATFLIX-Windows-x64-$env:RELEASE_VERSION.msi"
$checksumLine = Get-Content -LiteralPath (Join-Path $qa 'SHA256SUMS.txt') | Where-Object { ($_ -split '\s+', 2)[1] -eq 'BOATFLIX-Windows-x64-1.32.msi' }
if (@($checksumLine).Count -ne 1) { throw 'Previous MSI checksum entry is missing or ambiguous.' }
$expected = ($checksumLine -split '\s+', 2)[0]
if ((Get-FileHash -LiteralPath $old -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expected) { throw 'Previous MSI checksum mismatch.' }
function Read-MsiProperties([string]$path) {
    $installer = New-Object -ComObject WindowsInstaller.Installer
    $database = $installer.OpenDatabase($path, 0)
    $view = $database.OpenView('SELECT `Property`, `Value` FROM `Property`')
    $values = @{}
    try {
        $view.Execute()
        while ($record = $view.Fetch()) { $values[$record.StringData(1)] = $record.StringData(2) }
    } finally { $view.Close() }
    return $values
}
$previous = Read-MsiProperties $old
$next = Read-MsiProperties $current
if ($previous.ProductName -ne 'BOATFLIX' -or $next.ProductName -ne 'BOATFLIX') { throw 'Unexpected application identity.' }
if ($previous.ProductVersion -ne '1.32.0' -or $next.ProductVersion -ne '1.33.0') { throw 'Unexpected MSI versions.' }
if ($previous.UpgradeCode -ne $next.UpgradeCode) { throw 'Upgrade identity changed.' }
function Install-Msi([string]$path, [string]$logName) {
    $logPath = Join-Path $qa $logName
    $process = Start-Process msiexec.exe -WindowStyle Hidden -ArgumentList @('/i', "`"$path`"", '/qn', '/norestart', '/l*v', "`"$logPath`"") -Wait -PassThru
    if ($process.ExitCode -notin @(0, 3010)) { throw "MSI failed with code $($process.ExitCode)." }
}
Install-Msi $old 'install-132.log'
$data = Join-Path $env:APPDATA 'BOATFLIX'
New-Item -ItemType Directory -Path $data -Force | Out-Null
$sentinel = Join-Path $data 'episode-upgrade-sentinel.txt'
'BOATFLIX episode upgrade QA' | Set-Content -LiteralPath $sentinel
$before = (Get-FileHash -LiteralPath $sentinel -Algorithm SHA256).Hash
Install-Msi $current 'upgrade-133.log'
if ((Get-FileHash -LiteralPath $sentinel -Algorithm SHA256).Hash -ne $before) { throw 'Application data was changed during upgrade.' }
$registered = @('HKLM:/SOFTWARE/Microsoft/Windows/CurrentVersion/Uninstall', 'HKLM:/SOFTWARE/WOW6432Node/Microsoft/Windows/CurrentVersion/Uninstall') | ForEach-Object {
    Get-ChildItem -LiteralPath $_ -ErrorAction SilentlyContinue | Get-ItemProperty | Where-Object DisplayName -eq 'BOATFLIX'
}
if (@($registered).Count -ne 1 -or $registered.DisplayVersion -ne '1.33.0') { throw 'Upgrade did not replace the registered application.' }
'PASS: BOATFLIX 1.32 -> 1.33, same UpgradeCode, one installed application, owned data preserved' | Set-Content -LiteralPath (Join-Path $qa 'result.txt')
