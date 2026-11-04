param([switch]$Resolve)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot
$lockPath = Join-Path $repo 'pack/mods.lock.json'
$agent = 'RassersRevolutionSetup/0.1 (Revo1999)'
function Get-Api($path) { Invoke-RestMethod -UserAgent $agent "https://api.modrinth.com/v2/$path" }
if ($Resolve) {
    $selected = @{}
    function Resolve-Project($projectId, $versionId) {
        if ($selected.ContainsKey($projectId)) {
            if ($versionId -and $selected[$projectId].id -ne $versionId) { throw "Conflicting pinned dependency: $projectId" }
            return
        }
        if ($versionId) { $v = Get-Api "version/$versionId" }
        else {
            $versions = Get-Api "project/$projectId/version?loaders=%5B%22neoforge%22%5D&game_versions=%5B%221.21.1%22%5D"
            $v = $versions | Where-Object version_type -eq release | Select-Object -First 1
            if (!$v) { $v = $versions | Select-Object -First 1 }
        }
        if (!$v -or '1.21.1' -notin $v.game_versions -or 'neoforge' -notin $v.loaders) { throw "No compatible NeoForge 1.21.1 version: $projectId" }
        $selected[$v.project_id] = $v
        $selected[$projectId] = $v
        foreach ($dep in $v.dependencies | Where-Object dependency_type -eq required) {
            if (!$dep.project_id -and !$dep.version_id) { throw "Unresolvable required file dependency for $projectId" }
            $depProject = $dep.project_id
            if (!$depProject) { $depProject = (Get-Api "version/$($dep.version_id)").project_id }
            Resolve-Project $depProject $dep.version_id
        }
    }
    foreach ($request in (Get-Content (Join-Path $repo 'pack/mods.requested.json') -Raw | ConvertFrom-Json)) {
        Resolve-Project $request.project $request.versionId
    }
    $entries = foreach ($v in $selected.Values | Sort-Object id -Unique) {
        $p = Get-Api "project/$($v.project_id)"
        $f = $v.files | Where-Object primary | Select-Object -First 1
        if (!$f) { $f = $v.files | Select-Object -First 1 }
        [ordered]@{project=$p.slug;projectId=$v.project_id;version=$v.version_number;versionId=$v.id;file=$f.filename;url=$f.url;sha512=$f.hashes.sha512;client=$p.client_side;server=$p.server_side}
    }
    $externalPath = Join-Path $repo 'pack/mods.external.lock.json'
    if (Test-Path $externalPath) {
        $entries = @($entries) + @(Get-Content $externalPath -Raw | ConvertFrom-Json)
    }
    [ordered]@{minecraft='1.21.1';loader='neoforge';neoForge='21.1.265';mods=@($entries)} | ConvertTo-Json -Depth 6 | Set-Content $lockPath
}
if (!(Test-Path $lockPath)) { throw 'Run with -Resolve once to create the dependency lock.' }
$lock = Get-Content $lockPath -Raw | ConvertFrom-Json
$modsDir = Join-Path $repo '.local/integration-mods'
$runModsDir = Join-Path $repo 'revolib/run/mods'
New-Item -ItemType Directory -Force $modsDir,$runModsDir | Out-Null
foreach ($mod in $lock.mods) {
    if ([IO.Path]::GetFileName($mod.file) -ne $mod.file) { throw 'Invalid artifact filename' }
    $target = Join-Path $modsDir $mod.file
    if (!(Test-Path $target) -or (Get-FileHash $target -Algorithm SHA512).Hash.ToLower() -ne $mod.sha512) {
        Write-Host "Downloading $($mod.project) $($mod.version)"
        Invoke-WebRequest -UserAgent $agent $mod.url -OutFile $target
    }
    if ((Get-FileHash $target -Algorithm SHA512).Hash.ToLower() -ne $mod.sha512) { throw "Checksum mismatch: $($mod.file)" }
    Copy-Item $target $runModsDir -Force
}
Write-Host "Installed $($lock.mods.Count) verified mod artifacts into the development run directory."
$shaderLockPath = Join-Path $repo 'pack/shader.lock.json'
if (Test-Path $shaderLockPath) {
    $shader = Get-Content $shaderLockPath -Raw | ConvertFrom-Json
    if ([IO.Path]::GetFileName($shader.file) -ne $shader.file) { throw 'Invalid shader filename' }
    $shaderDir = Join-Path $repo 'revolib/run/shaderpacks'
    New-Item -ItemType Directory -Force $shaderDir | Out-Null
    $shaderTarget = Join-Path $shaderDir $shader.file
    if (!(Test-Path $shaderTarget) -or (Get-FileHash $shaderTarget -Algorithm SHA512).Hash.ToLower() -ne $shader.sha512) {
        Invoke-WebRequest -UserAgent $agent $shader.url -OutFile $shaderTarget
    }
    if ((Get-FileHash $shaderTarget -Algorithm SHA512).Hash.ToLower() -ne $shader.sha512) { throw 'Shader checksum mismatch' }
}
