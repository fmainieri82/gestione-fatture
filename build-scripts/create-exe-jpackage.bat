@echo off
echo Creazione eseguibile Windows con jpackage (Java 17+)...

REM Forza Java 21 (modifica il percorso se necessario)
set FORCED_JAVA_HOME=C:\Program Files\Java\jdk-21.0.4

REM Verifica se il JDK forzato esiste
if exist "%FORCED_JAVA_HOME%\bin\java.exe" (
    set JAVA_HOME=%FORCED_JAVA_HOME%
    set JAVA_CMD=%JAVA_HOME%\bin\java.exe
    set JPACKAGE_CMD=%JAVA_HOME%\bin\jpackage.exe
    echo Usando JDK forzato: %JAVA_HOME%
    goto :check_java_version
)

REM Se il JDK forzato non esiste, verifica JAVA_HOME esistente
if defined JAVA_HOME (
    set JAVA_CMD=%JAVA_HOME%\bin\java.exe
    set JPACKAGE_CMD=%JAVA_HOME%\bin\jpackage.exe
    if exist "%JPACKAGE_CMD%" (
        echo Trovato JDK in JAVA_HOME: %JAVA_HOME%
        goto :check_java_version
    )
)

REM Verifica che Java sia installato nel PATH
where java >nul 2>&1
if errorlevel 1 (
    echo ERRORE: Java non trovato nel PATH!
    echo.
    echo Assicurati che Java 17+ (JDK) sia installato.
    echo Imposta JAVA_HOME se hai più versioni di Java installate.
    echo.
    pause
    exit /b 1
)

set JAVA_CMD=java
set JPACKAGE_CMD=jpackage

:check_java_version
REM Verifica versione Java (deve essere 17+)
echo Verifica versione Java...
"%JAVA_CMD%" -version >nul 2>&1
if errorlevel 1 (
    echo ERRORE: Impossibile eseguire Java!
    pause
    exit /b 1
)

REM Ottieni versione completa
for /f "tokens=*" %%v in ('"%JAVA_CMD%" -version 2^>^&1') do (
    set JAVA_VERSION_LINE=%%v
    echo %%v | findstr /i "version" >nul
    if not errorlevel 1 (
        REM Estrai numero di versione (formato: "1.8.0_201" o "17.0.1" o "openjdk version "17.0.1"")
        for /f "tokens=3" %%a in ("%%v") do (
            set JAVA_VERSION=%%a
            goto :parse_version
        )
    )
)

:parse_version
REM Rimuovi virgolette
set JAVA_VERSION=%JAVA_VERSION:"=%
echo Versione Java rilevata: %JAVA_VERSION%

REM Estrai numero di versione principale (prima parte prima del punto)
for /f "tokens=1 delims=." %%a in ("%JAVA_VERSION%") do set MAJOR_PART=%%a
for /f "tokens=1 delims=_" %%a in ("%MAJOR_PART%") do set MAJOR_PART=%%a

REM Se inizia con "1." (es. 1.8, 1.9), prendi la seconda parte
echo %JAVA_VERSION% | findstr /i "^1\." >nul
if not errorlevel 1 (
    REM È formato 1.x, prendi la seconda parte
    for /f "tokens=2 delims=." %%a in ("%JAVA_VERSION%") do (
        for /f "tokens=1 delims=_" %%b in ("%%a") do set MAJOR_VERSION=%%b
    )
) else (
    REM È formato 17+ o openjdk, prendi la prima parte
    set MAJOR_VERSION=%MAJOR_PART%
)

REM Rimuovi caratteri non numerici
set MAJOR_VERSION=%MAJOR_VERSION:"=%
set MAJOR_VERSION=%MAJOR_VERSION: =%

echo Versione principale: %MAJOR_VERSION%

REM Verifica che la versione sia valida
if "%MAJOR_VERSION%"=="" (
    echo ATTENZIONE: Impossibile determinare la versione Java
    echo Procedo comunque...
    goto :check_jpackage
)

REM Confronto semplificato: verifica direttamente se è Java 17 o superiore
if "%MAJOR_VERSION%"=="17" goto check_jpackage
if "%MAJOR_VERSION%"=="18" goto check_jpackage
if "%MAJOR_VERSION%"=="19" goto check_jpackage
if "%MAJOR_VERSION%"=="20" goto check_jpackage
if "%MAJOR_VERSION%"=="21" goto check_jpackage
if "%MAJOR_VERSION%"=="22" goto check_jpackage
if "%MAJOR_VERSION%"=="23" goto check_jpackage
if "%MAJOR_VERSION%"=="24" goto check_jpackage
if "%MAJOR_VERSION%"=="25" goto check_jpackage

REM Se arriva qui, la versione è < 17 o non riconosciuta
goto version_error

:version_error
echo.
echo ========================================
echo ERRORE: Java 17+ richiesto!
echo ========================================
echo Versione trovata: Java %JAVA_VERSION% (principale: %MAJOR_VERSION%)
echo.
echo Soluzioni:
echo 1. Installa JDK 17 o superiore da:
echo    https://adoptium.net/ o https://www.oracle.com/java/
echo.
echo 2. Imposta JAVA_HOME per puntare al JDK 17+:
echo    set JAVA_HOME=C:\Program Files\Java\jdk-17
echo    set PATH=%%JAVA_HOME%%\bin;%%PATH%%
echo.
echo 3. Usa create-exe-simple.bat come alternativa
echo    (non richiede Java 17+, funziona con qualsiasi Java)
echo.
pause
exit /b 1

:check_jpackage
echo Versione Java OK, procedo con jpackage...

REM Verifica che jpackage sia disponibile
if exist "%JPACKAGE_CMD%" (
    goto jpackage_ok
)
echo.
echo ERRORE: jpackage non trovato in: %JPACKAGE_CMD%
echo.
echo jpackage è incluso solo nel JDK (non JRE).
echo Assicurati di avere il JDK 17+ installato, non solo il JRE.
echo.
echo Imposta JAVA_HOME per puntare al JDK:
echo   set JAVA_HOME=C:\Program Files\Java\jdk-17
echo.
pause
exit /b 1

:jpackage_ok

REM Verifica che il frontend sia stato copiato nella cartella static
set STATIC_PATH=..\backend\src\main\resources\static
if not exist "%STATIC_PATH%" (
    echo.
    echo ATTENZIONE: Frontend non trovato in %STATIC_PATH%
    echo.
    echo Il frontend deve essere copiato nella cartella static del backend.
    echo Esegui prima build-all.bat per compilare tutto, oppure:
    echo   1. Compila il frontend: cd frontend ^&^& ng build
    echo   2. Copia in static: xcopy /E /I /Y frontend\dist\frontend\browser backend\src\main\resources\static
    echo   3. Ricompila il backend: cd backend ^&^& mvn clean package
    echo.
    set /p continue="Vuoi continuare comunque? (S/N): "
    if /i not "%continue%"=="S" (
        pause
        exit /b 1
    )
)

REM Crea directory distribuzione
if not exist "..\distribuzione" mkdir "..\distribuzione"
if not exist "..\distribuzione\jpackage-output" rmdir /s /q "..\distribuzione\jpackage-output" 2>nul

REM Percorso JAR (lo script è in build-scripts, quindi risaliamo di una directory)
set JAR_PATH=..\backend\target\fatture-backend-1.0.0.jar

if not exist "%JAR_PATH%" (
    echo ERRORE: JAR non trovato in %JAR_PATH%
    echo Esegui prima il build del backend!
    pause
    exit /b 1
)

echo.
echo Creazione applicazione Windows con jpackage...
echo.

REM Crea applicazione Windows con jpackage
REM Per Spring Boot, non serve specificare main-class se il JAR è eseguibile
"%JPACKAGE_CMD%" ^
    --input ..\backend\target ^
    --name "GestioneFatture" ^
    --main-jar fatture-backend-1.0.0.jar ^
    --type app-image ^
    --dest ..\distribuzione\jpackage-output ^
    --java-options "-Xmx512m" ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --java-options "-Dspring.profiles.active=prod" ^
    --app-version "1.0.0" ^
    --description "Gestione Fatture - Applicazione Desktop" ^
    --vendor "Gestione Fatture" ^
    --copyright "2024" ^
    --win-console

if errorlevel 1 (
    echo.
    echo ERRORE nella creazione dell'applicazione con jpackage!
    echo.
    echo Verifica che:
    echo 1. Java 17+ sia installato
    echo 2. Il JAR sia stato creato correttamente
    echo 3. jpackage sia disponibile (incluso in JDK 17+)
    echo.
    pause
    exit /b 1
)

REM Copia l'eseguibile nella cartella distribuzione principale
if exist "..\distribuzione\jpackage-output\GestioneFatture\GestioneFatture.exe" (
    copy "..\distribuzione\jpackage-output\GestioneFatture\GestioneFatture.exe" "..\distribuzione\GestioneFatture.exe" >nul
    echo.
    echo ========================================
    echo APPLICAZIONE CREATA CON SUCCESSO!
    echo ========================================
    echo.
    echo File eseguibile: distribuzione\GestioneFatture.exe
    echo.
    echo NOTA: L'applicazione completa si trova in:
    echo   distribuzione\jpackage-output\GestioneFatture\
    echo.
    echo Per distribuire, copia l'intera cartella GestioneFatture.
    echo.
) else (
    echo ERRORE: Eseguibile non trovato dopo la creazione!
    pause
    exit /b 1
)

pause

