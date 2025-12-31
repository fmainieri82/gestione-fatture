@echo off
echo ========================================
echo BUILD APPLICAZIONE GESTIONE FATTURE
echo ========================================

echo.
echo [1/4] Build Frontend Angular...
cd ../frontend
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
cd ..
if not exist "frontend\dist\frontend\browser" (
    echo ERRORE: Directory frontend\dist\frontend\browser non trovata!
    pause
    exit /b 1
)
if not exist "backend\src\main\resources" mkdir "backend\src\main\resources"
if not exist "backend\src\main\resources\static" mkdir "backend\src\main\resources\static"
xcopy /E /I /Y "frontend\dist\frontend\browser\*" "backend\src\main\resources\static\"
if errorlevel 1 (
    echo ERRORE nella copia dei file!
    pause
    exit /b 1
)

echo.
echo [3/4] Build Backend Spring Boot...
cd ./backend

REM Verifica se Maven è nel PATH, altrimenti usa il percorso locale
set MAVEN_HOME=C:\apache-maven-3.9.9
set MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd
if exist "%MAVEN_CMD%" (
    call "%MAVEN_CMD%" clean package -DskipTests
) else (
    REM Prova con mvn se è nel PATH
    call mvn clean package -DskipTests
)

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
echo   2. Script semplice (.bat - piu' compatibile)
echo   3. Launch4j (originale - puo' dare falsi positivi)
echo.
set /p choice="Scelta (1/2/3): "

if "%choice%"=="1" (
    set CALLED_FROM_BUILD=1
    call build-scripts\create-exe-jpackage.bat
    set CALLED_FROM_BUILD=
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
echo Applicazione generata: distribuzione\jpackage-output\GestioneFatture
echo Eseguibile: distribuzione\jpackage-output\GestioneFatture\GestioneFatture.exe
echo.
echo PROSSIMI PASSI:
echo 1. Vai nella cartella 'distribuzione\jpackage-output\GestioneFatture'
echo 2. Doppio click su GestioneFatture.exe
echo 3. L'applicazione si aprirà nel browser
echo.
echo IMPORTANTE: Per distribuire l'applicazione, copia l'intera cartella
echo 'GestioneFatture' (contiene app, runtime e l'eseguibile).
echo.
pause
