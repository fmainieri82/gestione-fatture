@echo off
setlocal enabledelayedexpansion
echo Creazione eseguibile Windows con jpackage (Java 17+)...

REM Salva la directory corrente e vai nella directory dello script
REM Se viene chiamato da build-all.bat, NON mostrare pause
set SHOW_PAUSE=1
if "%CALLED_FROM_BUILD%"=="1" set SHOW_PAUSE=0

set ORIGINAL_DIR=%CD%
cd /d "%~dp0"
REM Imposta PROJECT_ROOT (directory padre di build-scripts)
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..
REM Normalizza il percorso (rimuovi eventuali \ alla fine)
set PROJECT_ROOT=%PROJECT_ROOT:"=%
if "%PROJECT_ROOT:~-1%"=="\" set PROJECT_ROOT=%PROJECT_ROOT:~0,-1%

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
    if "%SHOW_PAUSE%"=="1" pause
    cd /d "%ORIGINAL_DIR%"
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
    if "%SHOW_PAUSE%"=="1" pause
    cd /d "%ORIGINAL_DIR%"
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
if "%SHOW_PAUSE%"=="1" pause
cd /d "%ORIGINAL_DIR%"
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
if "%SHOW_PAUSE%"=="1" pause
cd /d "%ORIGINAL_DIR%"
exit /b 1

:jpackage_ok

REM Verifica che il frontend sia stato copiato nella cartella static
REM Il percorso è relativo alla directory build-scripts
set STATIC_PATH=..\backend\src\main\resources\static
set STATIC_INDEX=%STATIC_PATH%\index.html

REM Verifica se la directory esiste E se contiene index.html
if not exist "%STATIC_PATH%" (
    echo.
    echo ATTENZIONE: Directory static non trovata in %STATIC_PATH%
    goto :static_error
)

if not exist "%STATIC_INDEX%" (
    echo.
    echo ATTENZIONE: Frontend non trovato in %STATIC_PATH%
    echo La directory esiste ma non contiene index.html
    goto :static_error
)

echo Frontend trovato in %STATIC_PATH%
goto :static_ok

:static_error
echo.
echo Il frontend deve essere copiato nella cartella static del backend.
echo Esegui prima build-all.bat per compilare tutto, oppure:
echo   1. Compila il frontend: cd frontend ^&^& ng build
echo   2. Copia in static: xcopy /E /I /Y frontend\dist\frontend\browser backend\src\main\resources\static
    echo   3. Ricompila il backend: cd backend ^&^& mvn clean package
    echo.
    if "%SHOW_PAUSE%"=="1" (
        set /p continue="Vuoi continuare comunque? (S/N): "
        if /i not "!continue!"=="S" (
            cd /d "%ORIGINAL_DIR%"
            endlocal
            exit /b 1
        )
    ) else (
        echo Continuo automaticamente (chiamato da build script)...
    )

:static_ok

REM Crea directory distribuzione
if not exist "..\distribuzione" mkdir "..\distribuzione"

REM Determina il percorso corretto per la directory di destinazione
set DEST_DIR=..\distribuzione\jpackage-output

REM Rimuovi la directory jpackage-output se esiste (per evitare conflitti)
if exist "%DEST_DIR%" (
    echo Rimozione directory jpackage-output esistente...
    rmdir /s /q "%DEST_DIR%"
    if errorlevel 1 (
        echo ATTENZIONE: Impossibile rimuovere la directory esistente.
        echo La directory potrebbe essere in uso da un altro processo.
        echo.
        if "%SHOW_PAUSE%"=="1" (
            set /p continue="Vuoi continuare comunque? (S/N): "
            if /i not "!continue!"=="S" (
                cd /d "%ORIGINAL_DIR%"
                exit /b 1
            )
        )
    )
)

REM Leggi la versione dal pom.xml
echo Lettura versione dal pom.xml...
set VERSION=1.0.0

REM Trova il file pom.xml (lo script è in build-scripts, quindi risaliamo di una directory)
set POM_FILE=..\backend\pom.xml
if not exist "%POM_FILE%" (
    set POM_FILE=backend\pom.xml
    if not exist "%POM_FILE%" (
        set POM_FILE=%PROJECT_ROOT%\backend\pom.xml
    )
)

if not exist "%POM_FILE%" (
    echo ATTENZIONE: pom.xml non trovato, uso versione default 1.0.0
    goto :version_done
)

echo File pom.xml trovato: %POM_FILE%

REM Prova prima con Maven se disponibile (metodo più affidabile)
where mvn >nul 2>&1
if not errorlevel 1 (
    echo Tentativo lettura versione con Maven...
    pushd "..\backend"
    if exist "pom.xml" (
        for /f "tokens=*" %%v in ('mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2^>nul') do (
            if not "%%v"=="" (
                set VERSION=%%v
                popd
                goto :version_done
            )
        )
    )
    popd
)

REM Fallback: estrai la versione dal pom.xml con PowerShell (metodo più affidabile)
echo Lettura versione dal file pom.xml con PowerShell...
REM Usa percorso assoluto per PowerShell
for /f "delims=" %%v in ('powershell -NoProfile -Command "$pomPath = Resolve-Path '%POM_FILE%'; $pom = [xml](Get-Content -Path $pomPath -Raw); Write-Output $pom.project.version" 2^>nul') do (
    if not "%%v"=="" (
        set VERSION=%%v
        goto :version_done
    )
)

REM Ultimo fallback: regex semplice con PowerShell
echo Tentativo con regex...
for /f "delims=" %%v in ('powershell -NoProfile -Command "$content = Get-Content '%POM_FILE%' -Raw; if ($content -match ''<artifactId>fatture-backend</artifactId>[\s\S]*?<version>([^<]+)</version>'') { Write-Output $matches[1] }" 2^>nul') do (
    if not "%%v"=="" (
        set VERSION=%%v
        goto :version_done
    )
)

:version_done
REM Rimuovi eventuali caratteri non validi dalla versione
set VERSION=%VERSION: =%
set VERSION=%VERSION:"=%
set VERSION=%VERSION:'=%
echo Versione rilevata: %VERSION%

REM Percorso JAR (lo script è in build-scripts, quindi risaliamo di una directory)
set JAR_NAME=fatture-backend-%VERSION%.jar

echo Cerca JAR: %JAR_NAME%
echo Directory corrente: %CD%

REM Prova prima con percorso relativo (più affidabile quando lo script è in build-scripts)
set JAR_PATH=..\backend\target\%JAR_NAME%
echo Verifica percorso: %JAR_PATH%
if exist "%JAR_PATH%" (
    echo JAR trovato in: %JAR_PATH%
    goto :jar_found
)

REM Prova dalla directory corrente (se eseguito dalla root del progetto)
set JAR_PATH=backend\target\%JAR_NAME%
echo Verifica percorso: %JAR_PATH%
if exist "%JAR_PATH%" (
    echo JAR trovato in: %JAR_PATH%
    goto :jar_found
)

REM Prova con percorso assoluto basato su PROJECT_ROOT
if defined PROJECT_ROOT (
    set JAR_PATH=%PROJECT_ROOT%\backend\target\%JAR_NAME%
    echo Verifica percorso: %JAR_PATH%
    if exist "%JAR_PATH%" (
        echo JAR trovato in: %JAR_PATH%
        goto :jar_found
    )
)

REM Se non trovato, prova a cercare qualsiasi JAR nella directory target
echo Cerca qualsiasi JAR nella directory target...
if exist "..\backend\target" (
    for %%f in ("..\backend\target\fatture-backend-*.jar") do (
        echo JAR trovato: %%f
        set JAR_PATH=%%f
        goto :jar_found
    )
)

:jar_not_found
echo ERRORE: JAR non trovato!
echo Cercato in:
echo   - ..\backend\target\%JAR_NAME%
echo   - backend\target\%JAR_NAME%
if defined PROJECT_ROOT (
    echo   - %PROJECT_ROOT%\backend\target\%JAR_NAME%
)
echo.
echo Directory corrente: %CD%
echo Versione cercata: %VERSION%
echo Nome JAR: %JAR_NAME%
echo.
echo Verifica che:
echo   1. Il backend sia stato compilato (mvn clean package)
echo   2. Il JAR esista in backend\target\
echo   3. La versione nel pom.xml corrisponda
echo.
if "%SHOW_PAUSE%"=="1" pause
cd /d "%ORIGINAL_DIR%"
endlocal
exit /b 1

:jar_found
echo JAR trovato correttamente: %JAR_PATH%

REM Verifica che il JAR esista realmente
if not exist "%JAR_PATH%" (
    echo ERRORE: Il JAR non esiste nel percorso specificato: %JAR_PATH%
    echo Directory corrente: %CD%
    if "%SHOW_PAUSE%"=="1" pause
    cd /d "%ORIGINAL_DIR%"
    endlocal
    exit /b 1
)

REM Determina il percorso corretto per la directory di destinazione
REM Lo script è in build-scripts, quindi ..\distribuzione è corretto
REM DEST_DIR è già stato impostato sopra
set INPUT_DIR=..\backend\target

REM Verifica che la directory di input esista
if not exist "%INPUT_DIR%" (
    echo ERRORE: Directory di input non trovata: %INPUT_DIR%
    echo Directory corrente: %CD%
    if "%SHOW_PAUSE%"=="1" pause
    cd /d "%ORIGINAL_DIR%"
    exit /b 1
)

echo.
echo Creazione applicazione Windows con jpackage...
echo Directory corrente: %CD%
echo Input directory: %INPUT_DIR%
echo Destinazione: %DEST_DIR%
echo JAR: %JAR_PATH%
echo.

REM Crea applicazione Windows con jpackage
REM Per Spring Boot, non serve specificare main-class se il JAR è eseguibile
echo Esecuzione jpackage...
echo JAR: %JAR_NAME%
echo Versione: %VERSION%
"%JPACKAGE_CMD%" ^
    --input "%INPUT_DIR%" ^
    --name "GestioneFatture" ^
    --main-jar %JAR_NAME% ^
    --type app-image ^
    --dest "%DEST_DIR%" ^
    --java-options "-Xmx512m" ^
    --java-options "-Dfile.encoding=UTF-8" ^
    --java-options "-Dspring.profiles.active=prod" ^
    --app-version "%VERSION%" ^
    --description "Gestione Fatture - Applicazione Desktop" ^
    --vendor "Gestione Fatture" ^
    --copyright "2024" ^
    --win-console

REM Attendi un momento per assicurarsi che jpackage completi
timeout /t 1 /nobreak >nul

REM Verifica se l'eseguibile è stato creato (questo è il modo più affidabile)
REM invece di controllare solo ERRORLEVEL, verifichiamo il risultato
set EXE_PATH=%DEST_DIR%\GestioneFatture\GestioneFatture.exe

echo Verifica risultato jpackage...
if exist "%EXE_PATH%" (
    echo jpackage completato con successo!
    echo Eseguibile trovato: %EXE_PATH%
    goto :jpackage_success
)

REM Se l'eseguibile non esiste, controlla ERRORLEVEL come fallback
if errorlevel 1 (
    echo.
    echo ========================================
    echo ERRORE nella creazione dell'applicazione con jpackage!
    echo ========================================
    echo Codice di errore: %ERRORLEVEL%
    echo.
    echo Directory corrente: %CD%
    echo Input: %INPUT_DIR%
    echo Destinazione: %DEST_DIR%
    echo JAR: %JAR_PATH%
    echo Percorso eseguibile cercato: %EXE_PATH%
    echo.
    echo Verifica la directory di output:
    if exist "%DEST_DIR%" (
        echo Directory %DEST_DIR% esiste. Contenuto:
        dir "%DEST_DIR%"
    ) else (
        echo Directory %DEST_DIR% non esiste!
    )
    echo.
    echo Verifica che:
    echo 1. Java 17+ sia installato
    echo 2. Il JAR sia stato creato correttamente
    echo 3. jpackage sia disponibile (incluso in JDK 17+)
    echo 4. La directory di destinazione non sia bloccata da altri processi
    echo.
    if "%SHOW_PAUSE%"=="1" pause
    cd /d "%ORIGINAL_DIR%"
    exit /b 1
) else (
    REM jpackage non ha restituito errore ma l'eseguibile non esiste
    echo.
    echo ATTENZIONE: jpackage non ha restituito errori ma l'eseguibile non è stato trovato!
    echo Percorso cercato: %EXE_PATH%
    echo.
    echo Verifica la directory di output:
    if exist "%DEST_DIR%" (
        echo Directory %DEST_DIR% esiste. Contenuto:
        dir "%DEST_DIR%"
    ) else (
        echo Directory %DEST_DIR% non esiste!
    )
    echo.
    if "%SHOW_PAUSE%"=="1" pause
    cd /d "%ORIGINAL_DIR%"
    exit /b 1
)

:jpackage_success

REM L'applicazione completa è in jpackage-output/GestioneFatture
REM Non serve copiare l'exe esterno, l'applicazione completa è nella cartella
set APP_DIR=%DEST_DIR%\GestioneFatture

if exist "%EXE_PATH%" (
    echo.
    echo ========================================
    echo APPLICAZIONE CREATA CON SUCCESSO!
    echo ========================================
    echo.
    echo Applicazione completa: distribuzione\jpackage-output\GestioneFatture
    echo Eseguibile: distribuzione\jpackage-output\GestioneFatture\GestioneFatture.exe
    echo.
    echo L'applicazione include:
    echo   - GestioneFatture.exe (eseguibile principale)
    echo   - Cartella app (contenente il JAR e le risorse)
    echo   - Cartella runtime (Java runtime incluso)
    echo   - Tutte le dipendenze necessarie
    echo.
    echo IMPORTANTE: Per distribuire, copia l'intera cartella GestioneFatture.
    echo L'eseguibile da solo non funziona senza le cartelle app e runtime.
    echo.
    REM Ripristina directory originale
    cd /d "%ORIGINAL_DIR%"
    REM Esci con successo (0 = successo)
    endlocal
    exit /b 0
) else (
    echo ERRORE: Eseguibile non trovato dopo la creazione!
    echo Percorso cercato: %EXE_PATH%
    echo.
    echo Verifica che jpackage abbia completato correttamente.
    REM Ripristina directory originale
    cd /d "%ORIGINAL_DIR%"
    endlocal
    exit /b 1
)

