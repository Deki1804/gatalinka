#!/bin/bash
# Bash skripta za dobivanje SHA-1 fingerprinta debug keystore-a (Linux/Mac)

echo "Dobivanje SHA-1 fingerprinta za debug keystore..."
echo ""

# Standardna lokacija debug keystore-a
DEBUG_KEYSTORE="$HOME/.android/debug.keystore"

if [ -f "$DEBUG_KEYSTORE" ]; then
    echo "Pronađen debug keystore na: $DEBUG_KEYSTORE"
    echo ""
    echo "SHA-1 fingerprint:"
    echo "=================="
    
    # Izvozi SHA-1
    keytool -list -v -keystore "$DEBUG_KEYSTORE" -alias androiddebugkey -storepass android -keypass android | grep SHA1
    
    echo ""
    echo "Kopiraj SHA-1 hash (bez 'SHA1:' prefiksa) i dodaj ga u Firebase Console!"
else
    echo "GREŠKA: Debug keystore nije pronađen na: $DEBUG_KEYSTORE"
    echo ""
    echo "Probaj pokrenuti aplikaciju jednom u Android Studio-u da se kreira keystore."
fi

