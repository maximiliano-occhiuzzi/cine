$utf8NoBom = New-Object System.Text.UTF8Encoding $false
$javaCount = 0
$configCount = 0
$xmlCount = 0

Get-ChildItem -Path $PWD -Recurse -File -Filter "*.java" | ForEach-Object {
    $file = $_
    $content = [System.IO.File]::ReadAllText($file.FullName)
    $content = $content -replace '(?s)/\*.*?\*/', ''
    $lines = $content -split "`r?\n"
    $cleanLines = @()
    foreach ($line in$lines) {
        if ($line -match '^\s*//') { continue }$cleanLine = $line -replace '(?<![:"])\s+//.*$', ''
        $cleanLines +=$cleanLine
    }
    $finalContent = ($cleanLines -join "`r`n")
    [System.IO.File]::WriteAllText($file.FullName, $finalContent, $utf8NoBom)$javaCount++
}

Get-ChildItem -Path $PWD -Recurse -File \vert{} Where-Object {$_.Extension -in '.yml', '.yaml', '.properties', '.env' -or $_.Name -like '*.env*' -or$_.Name -like 'Dockerfile*' } | ForEach-Object {
    $file =$_
    $lines = Get-Content$file.FullName
    $cleanLines = @()
    foreach ($line in$lines) {
        if ($line -match '^\s*#') { continue }
        $cleanLine = $line -replace '\s+#.*$', ''
        $cleanLines +=$cleanLine
    }
    $finalContent = ($cleanLines -join "`r`n")
    [System.IO.File]::WriteAllText($file.FullName, $finalContent, $utf8NoBom)$configCount++
}

Get-ChildItem -Path $PWD -Recurse -File -Filter "*.xml" | ForEach-Object {
    $file =$_
    $content = [System.IO.File]::ReadAllText($file.FullName)
    $content =$content -replace '(?s)<!--.*?-->', ''
    [System.IO.File]::WriteAllText($file.FullName, $content, $utf8NoBom)$xmlCount++
}

Write-Host "Limpieza completada con exito:" -ForegroundColor Green
Write-Host "- $javaCount archivos Java procesados"
Write-Host "- $configCount archivos de configuracion procesados"
Write-Host "- $xmlCount archivos XML procesados"
