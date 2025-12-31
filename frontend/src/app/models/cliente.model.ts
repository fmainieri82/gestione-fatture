export interface Cliente {
  id?: number;
  ragioneSociale: string;
  partitaIva: string;
  codiceFiscale?: string;
  indirizzo: string;
  cap: string;
  citta: string;
  provincia: string;
  telefono?: string;
  email?: string;
  pec?: string;
  codiceSdi?: string;
  creatoIl?: Date;
  modificatoIl?: Date;
}
