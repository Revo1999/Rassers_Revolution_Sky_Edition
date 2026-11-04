param([Parameter(Mandatory=$true)][string]$JdkPath)
$ErrorActionPreference = 'Stop'
if (!(Test-Path (Join-Path $JdkPath 'bin/javac.exe'))) { throw 'Not a JDK directory' }
$repo = Split-Path $PSScriptRoot
New-Item -ItemType Directory -Force "$repo/.local" | Out-Null
Set-Content "$repo/.local/java-home.txt" $JdkPath
$settings = @{
    'java.jdt.ls.java.home' = $JdkPath
    'java.import.gradle.java.home' = $JdkPath
    'java.configuration.runtimes' = @(@{name='JavaSE-21';path=$JdkPath;default=$true})
}
$settings | ConvertTo-Json -Depth 5 | Set-Content "$repo/.vscode/settings.json"