@echo off
echo Creazione eseguibile Windows semplice (script batch)...

REM Crea directory distribuzione
if not exist "..\distribuzione" mkdir "..\distribuzione"

REM Percorso JAR
set JAR_PATH=backend\target\fatture-backend-1.0.0.jar

if not exist "%JAR_PATH%" (
    echo ERRORE: JAR non trovato in %JAR_PATH%
    echo Esegui prima il build del backend!
    pause
    exit /b 1
)

REM Crea uno script batch che avvia l'applicazione
(
echo @echo off
echo REM Avvia Gestione Fatture
echo cd /d "%%~dp0"
echo if not exist "java.exe" ^(
echo     echo Java non trovato! Assicurati che Java 17+ sia installato.
echo     pause
echo     exit /b 1
echo ^)
echo start "" "java" -jar "%%~dp0fatture-backend-1.0.0.jar"
) > "..\distribuzione\GestioneFatture.bat"

REM Copia il JAR nella cartella distribuzione
copy "%JAR_PATH%" "..\distribuzione\fatture-backend-1.0.0.jar" >nul

REM Crea un file VBS per nascondere la finestra console (opzionale)
(
echo Set WshShell = CreateObject^("WScript.Shell"^)
echo WshShell.Run "java -jar """ ^& CreateObject^("Scripting.FileSystemObject"^).GetParentFolderName^(WScript.ScriptFullName^) ^& "\fatture-backend-1.0.0.jar", 0, False
) > "..\distribuzione\GestioneFatture.vbs"

echo.
echo ========================================
echo FILE CREATI CON SUCCESSO!
echo ========================================
echo.
echo File creati in distribuzione\:
echo   - GestioneFatture.bat (doppio click per avviare)
echo   - GestioneFatture.vbs (avvia senza finestra console)
echo   - fatture-backend-1.0.0.jar
echo.
echo NOTA: L'utente deve avere Java 17+ installato.
echo.
pause

