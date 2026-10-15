# Builds Windows x64 Release DLLs and packages them as JNA classpath resources.
param(
    [string]$ClionHome = 'C:/Program Files/JetBrains/CLion 2026.2.3.1'
)

$ErrorActionPreference = 'Stop'
$repoRoot = Split-Path $PSScriptRoot -Parent
$cmake = Join-Path $ClionHome 'bin/cmake/win/x64/bin/cmake.exe'
$ninja = Join-Path $ClionHome 'bin/ninja/win/x64/ninja.exe'
$compilerBin = Join-Path $ClionHome 'bin/mingw/bin'
foreach ($tool in @($cmake, $ninja, (Join-Path $compilerBin 'gcc.exe'))) {
    if (-not (Test-Path -LiteralPath $tool)) { throw "Missing build tool: $tool" }
}

$previousPath = $env:PATH
try {
    $env:PATH = "$compilerBin;$previousPath"
    foreach ($module in @('risk', 'fx')) {
        $buildDir = Join-Path $repoRoot "build/native-$module"
        & $cmake -S (Join-Path $PSScriptRoot $module) -B $buildDir -G Ninja "-DCMAKE_MAKE_PROGRAM=$ninja" -DCMAKE_BUILD_TYPE=Release -DCURL_ZLIB=OFF -DCURL_USE_OPENSSL=OFF
        if ($LASTEXITCODE -ne 0) { throw "$module configuration failed" }
        & $cmake --build $buildDir
        if ($LASTEXITCODE -ne 0) { throw "$module build failed" }
    }

    $destination = Join-Path $repoRoot 'backend/AvanzaPortal/src/main/resources/win32-x86-64'
    New-Item -ItemType Directory -Force -Path $destination | Out-Null
    foreach ($module in @('risk', 'fx')) {
        Copy-Item -LiteralPath (Join-Path $repoRoot "build/native-$module/bin/$module.dll") -Destination (Join-Path $destination "$module.dll")
    }
} finally {
    $env:PATH = $previousPath
}
