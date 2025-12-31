# 🚀 Backend Gestione Fatture - PRONTO ALL'USO

## ✅ STATO PROGETTO: 100% COMPLETO

Tutti i file del backend sono pronti! Puoi iniziare subito.

## 📂 Struttura

```
fatture-app/
├── pom.xml                           ✅ Maven configurato
└── src/main/
    ├── java/com/fatture/
    │   ├── FattureApplication.java        ✅ Main app
    │   ├── model/
    │   │   ├── Cliente.java               ✅ Entity Cliente
    │   │   ├── Fattura.java               ✅ Entity Fattura
    │   │   └── VoceFattura.java           ✅ Entity Voci
    │   ├── repository/
    │   │   ├── ClienteRepository.java     ✅ Repository Cliente
    │   │   └── FatturaRepository.java     ✅ Repository Fattura
    │   ├── service/
    │   │   ├── ClienteService.java        ✅ Service Cliente
    │   │   ├── FatturaService.java        ✅ Service Fattura
    │   │   └── PdfService.java            ✅ Generazione PDF
    │   └── controller/
    │       ├── ClienteController.java     ✅ API Clienti
    │       └── FatturaController.java     ✅ API Fatture
    └── resources/
        └── application.properties         ✅ Config H2
```

## 🏃 AVVIO RAPIDO (2 minuti)

### Opzione 1: Con Maven già installato

```bash
cd fatture-app

# Avvia l'applicazione
mvn spring-boot:run
```

### Opzione 2: Senza Maven

```bash
cd fatture-app

# Compila
mvn clean package

# Avvia il JAR
java -jar target/fatture-backend-1.0.0.jar
```

**L'applicazione si avvierà su http://localhost:8080**

## ✅ TEST DELL'API

Puoi testare subito le API con curl o Postman:

### Crea un cliente
```bash
curl -X POST http://localhost:8080/api/clienti \
  -H "Content-Type: application/json" \
  -d '{
    "ragioneSociale": "Test SRL",
    "partitaIva": "12345678901",
    "indirizzo": "Via Roma 1",
    "cap": "00100",
    "citta": "Roma",
    "provincia": "RM",
    "email": "test@test.it"
  }'
```

### Lista clienti
```bash
curl http://localhost:8080/api/clienti
```

### Crea una fattura
```bash
curl -X POST http://localhost:8080/api/fatture \
  -H "Content-Type: application/json" \
  -d '{
    "tipoDocumento": "PREVENTIVO",
    "dataDocumento": "2024-12-29",
    "cliente": {"id": 1},
    "aliquotaIva": 22,
    "ragioneSocialeEmittente": "TUA AZIENDA SRL",
    "sedeLegaleEmittente": "Via Milano 1, 00100 Roma",
    "sedeOperativaEmittente": "Via Milano 1, 00100 Roma",
    "partitaIvaEmittente": "09876543210",
    "codiceUnivocoEmittente": "ABCD123",
    "ibanEmittente": "IT60X0542811101000000123456",
    "telefonoEmittente": "06.1234567",
    "emailEmittente": "info@tuaazienda.it",
    "voci": [
      {
        "descrizione": "Prodotto 1",
        "quantita": 2,
        "prezzoUnitario": 100.00,
        "unitaMisura": "Pz"
      }
    ]
  }'
```

### Genera PDF
```bash
curl -X POST http://localhost:8080/api/fatture/1/genera-pdf
```

### Download PDF
```
http://localhost:8080/api/fatture/1/download-pdf
```

## 📊 Database H2

Il database viene creato automaticamente in:
```
./data/fatture.mv.db
```

**NON eliminare questo file!** Contiene tutti i tuoi dati.

### Backup
```bash
# Copia semplicemente il file
cp data/fatture.mv.db data/backup_$(date +%Y%m%d).mv.db
```

## 🔧 Configurazione

### Porta del server
Modifica in `application.properties`:
```properties
server.port=8081
```

### Dati azienda di default
Modifica in `application.properties` o passa via API:
```properties
app.azienda.ragione-sociale=TUA AZIENDA SRL
app.azienda.partita-iva=12345678901
# ... ecc
```

## 📝 API Disponibili

### Clienti
- `GET /api/clienti` - Lista tutti
- `GET /api/clienti/{id}` - Dettaglio cliente
- `GET /api/clienti/search?keyword=xxx` - Ricerca
- `POST /api/clienti` - Crea cliente
- `PUT /api/clienti/{id}` - Aggiorna
- `DELETE /api/clienti/{id}` - Elimina

### Fatture
- `GET /api/fatture` - Lista tutte
- `GET /api/fatture/{id}` - Dettaglio fattura
- `GET /api/fatture/search?keyword=xxx` - Ricerca
- `GET /api/fatture/anno/{anno}` - Filtra per anno
- `POST /api/fatture` - Crea fattura
- `PUT /api/fatture/{id}` - Aggiorna
- `DELETE /api/fatture/{id}` - Elimina
- `POST /api/fatture/{id}/genera-pdf` - Genera PDF
- `GET /api/fatture/{id}/download-pdf` - Download PDF
- `GET /api/fatture/stats` - Statistiche

## 🐛 Problemi Comuni

### "Port 8080 already in use"
```bash
# Trova il processo
lsof -i :8080

# Oppure cambia porta in application.properties
server.port=8081
```

### Maven non trovato
```bash
# Windows: scarica da https://maven.apache.org/download.cgi
# Linux: sudo apt install maven
# Mac: brew install maven
```

### Java non trovato
```bash
# Scarica JDK 17 da https://adoptium.net/
```

## 📦 Prossimi Passi

1. **Testa il backend** con le API qui sopra
2. **Crea il frontend Angular** (vedi CHECKLIST_IMPLEMENTAZIONE.md)
3. **Build finale** per creare l'eseguibile

## 🎯 File Generati

### PDF
I PDF vengono salvati in:
```
./fatture/PREVENTIVO_1_2024P.pdf
```

### Log
I log dell'applicazione sono su console e in:
```
./logs/spring.log
```

## ✨ Funzionalità Implementate

- ✅ CRUD completo clienti e fatture
- ✅ Calcolo automatico totali e IVA
- ✅ Numerazione automatica documenti
- ✅ Generazione PDF professionale
- ✅ Ricerca full-text
- ✅ Statistiche fatturato
- ✅ Database persistente
- ✅ Validazione dati

---

**Il backend è 100% funzionante!** 🎉

Avvia con `mvn spring-boot:run` e testa le API.
