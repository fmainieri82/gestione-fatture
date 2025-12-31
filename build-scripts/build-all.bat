@echo off
echo ========================================
echo BUILD APPLICAZIONE GESTIONE FATTURE
echo ========================================

echo.
echo [1/4] Build Frontend Angular...
cd ../../progetto-completo/frontend
call npm install
if errorlevel 1 (
    echo ERRORE nell'installazione dipendenze frontend!
    pause
    exit /b 1
)

call ng build --configuration production
if errorlevel 1 (
    echo ERRORE nel build del frontend!
    pause
    exit /b 1
)

echo.
echo [2/4] Copia output Angular nel backend...
xcopy /E /I /Y ../../progetto-completo/frontend/dist\frontend\browser ..\..\progetto-completo\backend\src\main\resources\static
if errorlevel 1 (
    echo ERRORE nella copia dei file!
    pause
    exit /b 1
)

echo.
echo [3/4] Build Backend Spring Boot...
cd ..\progetto-completo\backend
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERRORE nel build del backend!
    pause
    exit /b 1
)

echo.
echo [4/4] Creazione eseguibile Windows...
cd ..
echo.
echo Scegli il metodo per creare l'eseguibile:
echo   1. jpackage (raccomandato - Java 17+, nessun antivirus)
echo   2. Script semplice (.bat - più compatibile)
echo   3. Launch4j (originale - può dare falsi positivi)
echo.
set /p choice="Scelta (1/2/3): "

if "%choice%"=="1" (
    call build-scripts\create-exe-jpackage.bat
) else if "%choice%"=="2" (
    call build-scripts\create-exe-simple.bat
) else if "%choice%"=="3" (
    call build-scripts\create-exe.bat
) else (
    echo Scelta non valida, uso metodo semplice...
    call build-scripts\create-exe-simple.bat
)

if errorlevel 1 (
    echo ERRORE nella creazione dell'eseguibile!
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD COMPLETATO CON SUCCESSO!
echo ========================================
echo.
echo File generato: distribuzione\GestioneFatture.exe
echo.
echo PROSSIMI PASSI:
echo 1. Vai nella cartella 'distribuzione'
echo 2. Doppio click su GestioneFatture.exe
echo 3. L'applicazione si aprirà nel browser
echo.
pause
