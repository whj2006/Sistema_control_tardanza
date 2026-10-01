$ErrorActionPreference = 'Stop'

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)][string]$Program,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )

    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "El comando '$Program' terminó con código $LASTEXITCODE."
    }
}

$projectDirectory = $PSScriptRoot
$repositoryDirectory = (Resolve-Path (Join-Path $projectDirectory '..')).Path
$pomText = Get-Content (Join-Path $projectDirectory 'pom.xml') -Raw
$versionMatch = [regex]::Match($pomText, '(?m)^\s*<version>([^<]+)</version>')
if (-not $versionMatch.Success) {
    throw 'No se pudo leer la versión del proyecto desde pom.xml.'
}
$projectVersion = $versionMatch.Groups[1].Value.Trim()
$version = $projectVersion
if ($env:GITHUB_REF_NAME -and $env:GITHUB_REF_NAME -match '^v(\d+\.\d+\.\d+)$') {
    $version = $Matches[1]
}

Push-Location $projectDirectory
try {
    Write-Host "Compilando y probando Control de Tardanzas $version..."
    Invoke-Checked 'mvn' @('-B', 'clean', 'verify')

    $jarName = "control-tardanzas-$projectVersion-jar-with-dependencies.jar"
    $jarPath = Join-Path $projectDirectory "target/$jarName"
    if (-not (Test-Path $jarPath)) {
        throw "No se encontró el JAR ejecutable: $jarPath"
    }

    $buildDirectory = Join-Path $projectDirectory 'target/windows-package'
    $inputDirectory = Join-Path $buildDirectory 'input'
    $runtimeDirectory = Join-Path $buildDirectory 'runtime'
    $appImageDestination = Join-Path $buildDirectory 'app-image'
    $appImageDirectory = Join-Path $appImageDestination 'ControlTardanzas'
    $distDirectory = Join-Path $repositoryDirectory 'dist'

    if (Test-Path $buildDirectory) {
        Remove-Item $buildDirectory -Recurse -Force
    }
    if (Test-Path $distDirectory) {
        $previousInstaller = Join-Path $distDirectory 'ControlTardanzas-Setup.exe'
        if (Test-Path $previousInstaller) {
            Remove-Item $previousInstaller -Force
        }
    }
    New-Item -ItemType Directory -Force -Path `
        $inputDirectory, $appImageDestination, $distDirectory | Out-Null
    Copy-Item $jarPath (Join-Path $inputDirectory $jarName)

    $jlink = Join-Path $env:JAVA_HOME 'bin/jlink.exe'
    $jpackage = Join-Path $env:JAVA_HOME 'bin/jpackage.exe'
    if (-not (Test-Path $jlink) -or -not (Test-Path $jpackage)) {
        throw 'No se encontró jlink o jpackage. Se necesita un JDK 17 completo.'
    }

    Write-Host 'Preparando el runtime de Java que se incluirá en el instalador...'
    Invoke-Checked $jlink @(
        '--add-modules', 'ALL-MODULE-PATH',
        '--strip-debug',
        '--no-man-pages',
        '--no-header-files',
        '--compress=2',
        '--output', $runtimeDirectory
    )

    Write-Host 'Generando la aplicación Windows con runtime integrado...'
    Invoke-Checked $jpackage @(
        '--type', 'app-image',
        '--name', 'ControlTardanzas',
        '--app-version', $version,
        '--vendor', 'Control Tardanzas',
        '--description', 'Sistema de control de retrasos del alumnado',
        '--input', $inputDirectory,
        '--main-jar', $jarName,
        '--main-class', 'com.instituto.tardanzas.Main',
        '--runtime-image', $runtimeDirectory,
        '--dest', $appImageDestination,
        '--java-options', '-Dfile.encoding=UTF-8'
    )

    if (-not (Test-Path (Join-Path $appImageDirectory 'ControlTardanzas.exe'))) {
        throw "jpackage no generó la aplicación esperada en $appImageDirectory"
    }

    $versionInclude = Join-Path $projectDirectory 'target/installer-version.iss'
    Set-Content -Path $versionInclude -Encoding Ascii `
        -Value "#define MyAppVersion `"$version`""

    $iscc = (Get-Command 'ISCC.exe' -ErrorAction SilentlyContinue).Source
    if (-not $iscc) {
        $iscc = Join-Path ${env:ProgramFiles(x86)} 'Inno Setup 6/ISCC.exe'
    }
    if (-not (Test-Path $iscc)) {
        throw 'No se encontró Inno Setup 6 (ISCC.exe). Instálalo para generar el instalador.'
    }

    Write-Host 'Empaquetando el instalador ControlTardanzas-Setup.exe...'
    $installerScript = Join-Path $repositoryDirectory 'instalador/ControlTardanzas-Setup.iss'
    Invoke-Checked $iscc @($installerScript)

    $setupPath = Join-Path $distDirectory 'ControlTardanzas-Setup.exe'
    if (-not (Test-Path $setupPath)) {
        throw "Inno Setup no generó el instalador esperado: $setupPath"
    }
    Write-Host "Instalador listo: $setupPath"
} finally {
    Pop-Location
}
