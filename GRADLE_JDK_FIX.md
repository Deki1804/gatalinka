# 🔧 Rješavanje Gradle JDK upozorenja

## Problem
Gradle koristi različite JDK lokacije:
- **Android Studio Gradle JDK**: `C:\Program Files\Android\Android Studio\jbr`
- **JAVA_HOME**: `C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot`

## Rješenje (2 opcije)

### Opcija 1: Postavi Gradle JDK u Android Studio (PREPORUČENO)

1. **Otvorite Android Studio**
2. **Idite na**: `File` → `Settings` (ili `Ctrl + Alt + S`)
3. **U lijevom meniju**: `Build, Execution, Deployment` → `Build Tools` → `Gradle`
4. **Pronađite**: "Gradle JDK" ili "JDK location"
5. **Promijenite na**: `C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot`
6. **Kliknite**: `Apply` i `OK`
7. **Restartujte Android Studio**

### Opcija 2: Postavi JAVA_HOME na Android Studio JDK

1. **Otvorite PowerShell kao Administrator**
2. **Pokrenite**:
   ```powershell
   [System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Android\Android Studio\jbr', [System.EnvironmentVariableTarget]::User)
   ```
3. **Restartujte terminal/Android Studio**

## Provjera

Nakon promjene, provjerite:
```powershell
echo $env:JAVA_HOME
```

I u Android Studio: `File` → `Settings` → `Build Tools` → `Gradle` → provjerite "Gradle JDK"

---

**Preporuka**: Koristite **Opciju 1** jer je jednostavnija i ne mijenja sistemske varijable.

