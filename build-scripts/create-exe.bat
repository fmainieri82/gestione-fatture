@echo off
echo Creazione eseguibile Windows con Launch4j...

REM Percorso Launch4j (modifica se necessario)
set LAUNCH4J="C:\Program Files (x86)\Launch4j\launch4jc.exe"

if not exist %LAUNCH4J% (
    echo.
    echo ATTENZIONE: Launch4j non trovato!
    echo.
    echo Scaricalo da: https://launch4j.sourceforge.net/
    echo Installalo e riavvia questo script.
    echo.
    echo ALTERNATIVA: Puoi usare il JAR direttamente senza EXE:
    echo   java -jar backend\target\fatture-backend-1.0.0.jar
    echo.
    pause
    exit /b 1
)

REM Crea directory distribuzione
if not exist "..\distribuzione" mkdir "..\distribuzione"

REM Genera EXE
%LAUNCH4J% launch4j-config.xml

if errorlevel 1 (
    echo ERRORE nella creazione dell'EXE!
    pause
    exit /b 1
)

echo.
echo EXE creato con successo in: distribuzione\GestioneFatture.exe
echo.
