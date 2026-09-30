param(
    [ValidateSet('build','runClient','runGameTest','runClientGameTest','clean')]
    [string]$Task = 'build',
    [switch]$SafeJvm,
    [ValidateSet('none','jei','rei')]
    [string]$Viewer = 'none'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$candidates = @($env:JAVA_HOME)
foreach ($jdkRoot in @("$env:ProgramFiles/Eclipse Adoptium", "$env:ProgramFiles/Java", "$env:ProgramFiles/Microsoft", "$env:USERPROFILE/.jdks")) {
    if (Test-Path -LiteralPath $jdkRoot) {
        $candidates += Get-ChildItem -LiteralPath $jdkRoot -Directory | Sort-Object Name | Select-Object -ExpandProperty FullName
    }
}
$selectedJdk = $null
foreach ($candidate in $candidates) {
    if (!$candidate -or !(Test-Path -LiteralPath "$candidate/bin/javac.exe") -or !(Test-Path -LiteralPath "$candidate/release")) { continue }
    $version = Get-Content -LiteralPath "$candidate/release" | Select-String '^JAVA_VERSION="(\d+)'
    if ($version -and [int]$version.Matches[0].Groups[1].Value -ge 21) { $selectedJdk = $candidate; break }
}
if (!$selectedJdk) { throw 'Install JDK 21 or newer and set JAVA_HOME to its directory.' }
$previousJava = $env:JAVA_HOME
$previousOptions = $env:JAVA_TOOL_OPTIONS
try {
    $env:JAVA_HOME = $selectedJdk
    if ($SafeJvm) { $env:JAVA_TOOL_OPTIONS = "$previousOptions -XX:TieredStopAtLevel=1".Trim() }
    Push-Location $projectRoot
    try {
        Write-Host "Using JDK: $selectedJdk"
        $gradleArguments = @($Task, '--console=plain', "-Pviewer=$Viewer")
        if ($SafeJvm) { $gradleArguments += '-Dorg.gradle.jvmargs=-Xmx1G -XX:TieredStopAtLevel=1' }
        & './gradlew.bat' @gradleArguments
        if ($LASTEXITCODE -ne 0) { throw "Gradle $Task failed with exit code $LASTEXITCODE" }
    } finally { Pop-Location }
} finally {
    $env:JAVA_HOME = $previousJava
    $env:JAVA_TOOL_OPTIONS = $previousOptions
}
