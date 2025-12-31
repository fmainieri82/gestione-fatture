@echo off
echo Creazione eseguibile Windows con WinRun4J...

REM WinRun4J è un'alternativa leggera a Launch4j
REM Scarica da: https://github.com/poidasmith/winrun4j/releases

set WINRUN4J_PATH=winrun4j\WinRun4J.exe
set CONFIG_FILE=winrun4j-config.ini

if not exist "%WINRUN4J_PATH%" (
    echo.
    echo ATTENZIONE: WinRun4J non trovato!
    echo.
    echo Scarica WinRun4J da:
    echo   https://github.com/poidasmith/winrun4j/releases
    echo.
    echo Estrai WinRun4J.exe in: build-scripts\winrun4j\
    echo.
    echo ALTERNATIVA: Usa create-exe-jpackage.bat (raccomandato)
    echo   oppure create-exe-simple.bat (più semplice)
    echo.
    pause
    exit /b 1
)

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

REM Crea file di configurazione WinRun4J
(
echo [MainClass]
echo org.springframework.boot.loader.JarLauncher
echo.
echo [ClassPath]
echo %JAR_PATH%
echo.
echo [JVM]
echo -Xmx512m
echo -Dfile.encoding=UTF-8
echo.
echo [WorkingDirectory]
echo .
) > "%CONFIG_FILE%"

REM Crea l'eseguibile
copy "%WINRUN4J_PATH%" "..\distribuzione\GestioneFatture.exe" >nul
copy "%JAR_PATH%" "..\distribuzione\fatture-backend-1.0.0.jar" >nul

REM Inietta la configurazione nell'EXE (richiede WinRun4J con supporto)
REM Nota: WinRun4J funziona diversamente da Launch4j
REM Potrebbe essere necessario usare il tool di WinRun4J per iniettare la config

echo.
echo ========================================
echo CONFIGURAZIONE CREATA!
echo ========================================
echo.
echo File: %CONFIG_FILE%
echo.
echo NOTA: WinRun4J richiede configurazione manuale.
echo Consulta la documentazione per completare la creazione dell'EXE.
echo.
pause

