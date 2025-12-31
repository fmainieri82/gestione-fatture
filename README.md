# 📦 PROGETTO COMPLETO GESTIONE FATTURE

## 🎯 Cosa Contiene Questo Pacchetto

Applicazione desktop completa per la gestione di fatture e preventivi con:
- ✅ **Backend Spring Boot** - API REST complete
- ✅ **Frontend Angular** - Interfaccia utente moderna
- ✅ **Database H2** - Persistenza dati su file
- ✅ **Generazione PDF** - Documenti professionali
- ✅ **Build Automatico** - Script pronti all'uso

---

## 📂 Struttura Progetto

```
progetto-completo/
├── backend/                    # Spring Boot Application
│   ├── src/
│   │   ├── main/java/com/fatture/
│   │   │   ├── FattureApplication.java
│   │   │   ├── model/          # Entity JPA (Cliente, Fattura, VoceFattura)
│   │   │   ├── repository/     # Data Access Layer
│   │   │   ├── service/        # Business Logic + PDF Generation
│   │   │   └── controller/     # REST Controllers
│   │   └── resources/
│   │       └── application.properties
│   ├── pom.xml
│   └── README.md
│
├── frontend/                   # Angular Application
│   ├── src/
│   │   ├── app/
│   │   │   ├── models/         # TypeScript interfaces
│   │   │   ├── services/       # HTTP services
│   │   │   ├── components/
│   │   │   │   ├── clienti/    # Gestione clienti
│   │   │   │   └── fatture/    # Gestione fatture
│   │   │   ├── app.component.ts
│   │   │   ├── app.routes.ts
│   │   │   └── app.config.ts
│   │   ├── index.html
│   │   └── styles.css
│   ├── package.json
│   └── angular.json
│
├── build-scripts/              # Script di build
│   ├── build-all.bat          # Build completo (Win)
│   ├── create-exe.bat         # Creazione EXE
│   └── launch4j-config.xml    # Config Launch4j
│
└── distribuzione/              # Output finale (dopo build)
    ├── GestioneFatture.exe    # Applicazione
    ├── data/                   # Database
    ├── fatture/                # PDF generati
    └── backups/                # Backup DB
```

---

## 🚀 AVVIO RAPIDO (Sviluppo)

### 1. Backend (Terminale 1)

```bash
cd backend
mvn spring-boot:run
```

Server su: http://localhost:8080

### 2. Frontend (Terminale 2)

```bash
cd frontend
npm install
npm start
```

App su: http://localhost:4200

---

## 🎯 BUILD PRODUZIONE (Un Solo Comando)

### Windows

```bash
build-scripts\build-all.bat
```

Questo script:
1. ✅ Compila Angular
2. ✅ Copia output in backend
3. ✅ Build Spring Boot JAR
4. ✅ Crea EXE con Launch4j

**Output**: `distribuzione/GestioneFatture.exe`

### Prerequisiti Build

- **Java JDK 17+**: https://adoptium.net/
- **Maven**: https://maven.apache.org/download.cgi
- **Node.js 18+**: https://nodejs.org/
- **Launch4j** (opzionale, per EXE): https://launch4j.sourceforge.net/

---

## 💻 Funzionalità Implementate

### Gestione Clienti
- ✅ Crea/Modifica/Elimina clienti
- ✅ Ricerca clienti
- ✅ Validazione P.IVA duplicata
- ✅ Campi: Ragione sociale, P.IVA, CF, Indirizzo, Email, PEC, SDI

### Gestione Fatture
- ✅ Crea Preventivi, Fatture, Ordini, DDT
- ✅ Selezione cliente
- ✅ Voci multiple con quantità e prezzo
- ✅ Calcolo automatico IVA e totali
- ✅ Stati documento (Bozza, Emesso, Accettato, ecc.)
- ✅ Numerazione automatica progressiva per anno

### Generazione PDF
- ✅ Layout professionale (identico al template fornito)
- ✅ Header con dati azienda
- ✅ Tabella voci dettagliata
- ✅ Calcolo totali automatico
- ✅ Download diretto dal browser

### Database
- ✅ H2 embedded con persistenza su file
- ✅ Backup semplice (copia cartella data/)
- ✅ Nessuna configurazione richiesta

### Dashboard
- ✅ Statistiche fatturato
- ✅ Ricerca fatture
- ✅ Filtro per anno
- ✅ Azioni rapide (PDF, Download, Modifica)

---

## 🔧 Configurazione

### Dati Azienda

Modifica in `backend/src/main/resources/application.properties`:

```properties
app.azienda.ragione-sociale=TUA AZIENDA SRL
app.azienda.partita-iva=12345678901
app.azienda.iban=IT60X0542811101000000123456
# ... ecc
```

Oppure modifica direttamente in:
`frontend/src/app/components/fatture/form-fattura.component.ts` (linee 240-247)

### Porta Server

Default: 8080

Per cambiare, modifica `application.properties`:
```properties
server.port=8081
```

E aggiorna anche in `frontend/src/app/services/*.service.ts`:
```typescript
private apiUrl = 'http://localhost:8081/api/...';
```

---

## 📖 Guida Uso Applicazione

### 1. Primo Avvio
- Doppio click su `GestioneFatture.exe`
- Il browser si apre automaticamente
- Nessuna configurazione necessaria

### 2. Crea un Cliente
- Vai su "Clienti"
- Click "+ Nuovo Cliente"
- Compila i dati
- Salva

### 3. Crea una Fattura
- Vai su "Fatture"  
- Click "+ Nuova Fattura"
- Seleziona cliente
- Aggiungi voci (descrizione, quantità, prezzo)
- I totali si calcolano automaticamente
- Salva

### 4. Genera PDF
- Nella lista fatture, click "📄 PDF"
- Il PDF viene generato
- Click "⬇️" per scaricare

---

## 🗄️ Backup e Ripristino

### Backup Manuale
```bash
# Copia la cartella data
xcopy /E /I data backup_29_12_2024
```

### Ripristino
```bash
# Sostituisci la cartella data
xcopy /E /I backup_29_12_2024 data
```

### Portabilità
Copia l'intera cartella `distribuzione/` su:
- Chiavetta USB
- Cloud (Google Drive, Dropbox)
- Altro PC

---

## 🐛 Risoluzione Problemi

### "Port 8080 already in use"
Un'altra app usa la porta 8080.
```bash
# Windows: trova il processo
netstat -ano | findstr :8080

# Oppure cambia porta in application.properties
```

### "Java not found"
Installa JDK 17: https://adoptium.net/

### "ng command not found"
```bash
npm install -g @angular/cli
```

### Database corrotto
```bash
# Elimina e ricrea
rmdir /S data
# Al prossimo avvio si ricrea vuoto
```

### Frontend non si connette al backend
Verifica che:
1. Backend sia avviato (http://localhost:8080)
2. URL in services sia corretto
3. CORS sia abilitato nel backend

---

## 📈 Miglioramenti Futuri

Idee per estendere l'applicazione:

- [ ] Invio email fatture
- [ ] Scadenzario pagamenti
- [ ] Statistiche avanzate con grafici
- [ ] Esportazione Excel
- [ ] Multi-azienda
- [ ] Login e autenticazione
- [ ] Modelli di fattura personalizzabili
- [ ] Integrazione fatturazione elettronica
- [ ] App mobile (Ionic)

---

## 📞 Supporto

Per domande o problemi:

1. Verifica la documentazione inclusa
2. Controlla i log del backend (console)
3. Controlla la console browser (F12) per errori frontend

---

## 🚀 GitHub e Release Automatiche

Il progetto è configurato per generare automaticamente una release su GitHub ad ogni push sul branch `main` o `master`.

### Configurazione Iniziale

1. **Crea un nuovo repository su GitHub**
   - Vai su https://github.com/new
   - Scegli un nome per il repository (es: `gestione-fatture`)
   - **NON** inizializzare con README, .gitignore o licenza (già presenti)

2. **Inizializza Git nel progetto** (se non già fatto)
   ```bash
   cd progetto-completo
   git init
   git add .
   git commit -m "Initial commit"
   ```

3. **Collega il repository locale a GitHub**
   ```bash
   git remote add origin https://github.com/TUO_USERNAME/nome-repository.git
   git branch -M main
   git push -u origin main
   ```

### Come Funziona

Ad ogni push sul branch `main` o `master`, GitHub Actions:
1. ✅ Compila il frontend Angular
2. ✅ Copia i file statici nel backend
3. ✅ Compila il backend Spring Boot
4. ✅ Crea automaticamente una release con il JAR compilato

### Visualizzare le Release

- Vai nella sezione **Releases** del tuo repository GitHub
- Ogni release contiene:
  - Il JAR compilato pronto all'uso
  - Note di release con informazioni sul commit
  - Tag automatico con versione e commit SHA

### Eseguire il Workflow Manualmente

Puoi anche eseguire il workflow manualmente:
1. Vai su **Actions** nel tuo repository GitHub
2. Seleziona **Build and Release**
3. Clicca **Run workflow**

### Personalizzazione

Per modificare il comportamento delle release, modifica il file:
`.github/workflows/build-and-release.yml`

---

## 📄 Licenza

Questo progetto è fornito "as-is" per uso personale/aziendale.

---

## ✨ Credits

Sviluppato con:
- Spring Boot 3.2
- Angular 17
- H2 Database
- iText PDF
- Apache POI

---

**Versione**: 1.0.0  
**Data**: Dicembre 2024
