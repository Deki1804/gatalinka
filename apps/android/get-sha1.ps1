# PowerShell skripta za dobivanje SHA-1 fingerprinta debug keystore-a
# Pokreni ovu skriptu u PowerShell-u da dobiješ SHA-1 hash

Write-Host "Dobivanje SHA-1 fingerprinta za debug keystore..." -ForegroundColor Cyan
Write-Host ""

# Standardna lokacija debug keystore-a na Windows-u
$debugKeystorePath = "$env:USERPROFILE\.android\debug.keystore"

if (Test-Path $debugKeystorePath) {
    Write-Host "Pronađen debug keystore na: $debugKeystorePath" -ForegroundColor Green
    Write-Host ""
    
    # Pokreni keytool komandu
    $keytoolPath = $null
    
    # Pronađi keytool.exe (obično u JAVA_HOME/bin ili Android SDK)
    if ($env:JAVA_HOME) {
        $keytoolPath = Join-Path $env:JAVA_HOME "bin\keytool.exe"
    }
    
    if (-not (Test-Path $keytoolPath)) {
        # Pokušaj pronaći u Android SDK
        if ($env:ANDROID_HOME) {
            $keytoolPath = Join-Path $env:ANDROID_HOME "jre\bin\keytool.exe"
            if (-not (Test-Path $keytoolPath)) {
                $keytoolPath = Join-Path $env:ANDROID_HOME "jbr\bin\keytool.exe"
            }
        }
    }
    
    if (-not $keytoolPath -or -not (Test-Path $keytoolPath)) {
        Write-Host "Tražim keytool u sistemskim PATH-ovima..." -ForegroundColor Yellow
        $keytoolPath = (Get-Command keytool -ErrorAction SilentlyContinue).Source
    }
    
    if ($keytoolPath -and (Test-Path $keytoolPath)) {
        Write-Host "Koristim keytool iz: $keytoolPath" -ForegroundColor Green
        Write-Host ""
        Write-Host "SHA-1 fingerprint:" -ForegroundColor Cyan
        Write-Host "==================" -ForegroundColor Cyan
        
        # Izvozi SHA-1
        & $keytoolPath -list -v -keystore $debugKeystorePath -alias androiddebugkey -storepass android -keypass android | Select-String "SHA1:"
        
        Write-Host ""
        Write-Host "Kopiraj SHA-1 hash (bez 'SHA1:' prefiksa) i dodaj ga u Firebase Console!" -ForegroundColor Yellow
    } else {
        Write-Host "GREŠKA: Ne mogu pronaći keytool.exe" -ForegroundColor Red
        Write-Host ""
        Write-Host "Pokušaj ručno pokrenuti:" -ForegroundColor Yellow
        Write-Host 'keytool -list -v -keystore "%USERPROFILE%\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android'
    }
} else {
    Write-Host "GREŠKA: Debug keystore nije pronađen na: $debugKeystorePath" -ForegroundColor Red
    Write-Host ""
    Write-Host "Probaj pokrenuti aplikaciju jednom u Android Studio-u da se kreira keystore." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Pritisni bilo koji tipku za izlaz..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

