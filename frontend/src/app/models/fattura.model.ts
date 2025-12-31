import { Cliente } from './cliente.model';

export interface Fattura {
  id?: number;
  numeroDocumento: string;
  dataDocumento: string;
  tipoDocumento: TipoDocumento;
  cliente: Cliente;
  voci: VoceFattura[];
  
  // Dati emittente
  ragioneSocialeEmittente?: string;
  sedeLegaleEmittente?: string;
  sedeOperativaEmittente?: string;
  partitaIvaEmittente?: string;
  codiceUnivocoEmittente?: string;
  ibanEmittente?: string;
  telefonoEmittente?: string;
  emailEmittente?: string;
  
  // Sede consegna
  sedeConsegnaIndirizzo?: string;
  sedeConsegnaCap?: string;
  sedeConsegnaCitta?: string;
  sedeConsegnaProvincia?: string;
  
  // Pagamento
  valuta?: string;
  agente?: string;
  modalitaPagamento?: string;
  scadenzaPagamento?: string;
  riferimentoCliente?: string;
  
  // Banca
  bancaCliente?: string;
  bancaEmittente?: string;
  bicEmittente?: string;
  
  // Totali
  totaleRighe?: number;
  scontiMaggiori?: number;
  imponibile?: number;
  aliquotaIva: number;
  importoIva?: number;
  speseTrasporto?: number;
  totaleDocumento?: number;
  accontoVersato?: number;
  speseIncasso?: number;
  speseImballo?: number;
  bollo?: number;
  ritenuta?: number;
  
  // Spedizione
  modalitaSpedizione?: string;
  porto?: string;
  condizioneConsegna?: string;
  
  // Note
  note?: string;
  noteInterne?: string;
  
  // File
  fileDocxPath?: string;
  filePdfPath?: string;
  
  stato: StatoDocumento;
  creatoIl?: Date;
  modificatoIl?: Date;
}

export interface VoceFattura {
  id?: number;
  codiceMerce?: string;
  descrizione: string;
  dettagliTecnici?: string;
  note?: string;
  unitaMisura: string;
  quantita: number;
  prezzoUnitario: number;
  importo?: number;
  codiceIva?: number;
  dataEvasione?: string;
  ordine?: number;
  // Scheda di sopralluogo
  cerniera?: string; // DX, SX
  pompaScarico?: boolean; // true = SI, false = NO
  tensione?: string; // 220M, 220T, 380T
  allacciDistanti?: boolean;
  ruote?: boolean;
  smaltimento?: boolean;
  necessarioSopralluogo?: boolean;
  addolcitoreCorrente?: string; // Automatico, Normale
  passaggioCm?: number;
  scale?: string; // testo libero
  macchinaDaSmontare?: boolean;
  misure?: string; // testo libero (se macchina da smontare = SI)
  gas?: string; // Metano, GPL
  gasDistanzaCm?: number;
  parcheggio?: boolean;
  giornoOraConsegna?: string; // data e ora
}

export enum TipoDocumento {
  PREVENTIVO = 'PREVENTIVO',
  FATTURA = 'FATTURA',
  ORDINE = 'ORDINE',
  DDT = 'DDT'
}

export enum StatoDocumento {
  BOZZA = 'BOZZA',
  EMESSA = 'EMESSA',
  PAGATA = 'PAGATA',
  ANNULLATA = 'ANNULLATA'
}
