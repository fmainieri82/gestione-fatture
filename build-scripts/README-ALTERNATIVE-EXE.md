# Alternative a Launch4j per creare file .exe

Launch4j può essere segnalato come falso positivo dagli antivirus. Ecco le alternative disponibili:

## 1. jpackage (RACCOMANDATO) ⭐

**Vantaggi:**
- Incluso in Java 17+ (già installato)
- Nessun falso positivo antivirus
- Crea applicazione Windows nativa completa
- Soluzione ufficiale Oracle

**Uso:**
```batch
build-scripts\create-exe-jpackage.bat
```

**Requisiti:**
- Java 17+ (JDK, non solo JRE)
- Il JAR deve essere già compilato

**Output:**
- Cartella completa in `distribuzione\jpackage-output\GestioneFatture\`
- Eseguibile: `GestioneFatture.exe`

## 2. Script Batch Semplice (PIÙ COMPATIBILE)

**Vantaggi:**
- Nessun tool esterno necessario
- Funziona su tutti i sistemi Windows
- Nessun problema con antivirus
- Facile da distribuire

**Svantaggi:**
- File .bat invece di .exe
- Richiede Java installato sul sistema

**Uso:**
```batch
build-scripts\create-exe-simple.bat
```

**Output:**
- `GestioneFatture.bat` - Script batch
- `GestioneFatture.vbs` - Script VBS (senza console)
- `fatture-backend-1.0.0.jar` - JAR dell'applicazione

## 3. WinRun4J (ALTERNATIVA A LAUNCH4J)

**Vantaggi:**
- Alternativa leggera a Launch4j
- Meno probabilità di falsi positivi
- Open source

**Svantaggi:**
- Richiede download e setup manuale
- Configurazione più complessa

**Uso:**
1. Scarica da: https://github.com/poidasmith/winrun4j/releases
2. Estrai in `build-scripts\winrun4j\`
3. Esegui: `build-scripts\create-exe-winrun4j.bat`

## 4. Inno Setup / NSIS (PER INSTALLER)

Se vuoi creare un installer completo invece di un semplice .exe:

**Inno Setup:**
- Gratuito e open source
- Crea installer Windows professionale
- Include JRE se necessario

**NSIS:**
- Alternativa a Inno Setup
- Più flessibile ma più complesso

## Raccomandazione

Per la maggior parte dei casi, usa **jpackage** (opzione 1):
- È la soluzione ufficiale
- Nessun problema con antivirus
- Crea applicazione completa e professionale
- Già incluso in Java 17+

Se jpackage non è disponibile o hai problemi, usa lo **script batch semplice** (opzione 2).

