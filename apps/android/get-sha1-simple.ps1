# Jednostavna PowerShell komanda za SHA-1
$keystore = "$env:USERPROFILE\.android\debug.keystore"
if (Test-Path $keystore) {
    keytool -list -v -keystore $keystore -alias androiddebugkey -storepass android -keypass android | Select-String "SHA1:" | ForEach-Object { $_.Line.Trim() }
} else {
    Write-Host "Debug keystore nije pronađen. Pokreni aplikaciju jednom u Android Studio-u."
}

