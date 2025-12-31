import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { VoceFattura } from '../../models/fattura.model';

@Component({
  selector: 'app-sopralluogo-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule
  ],
  template: `
    <h2 mat-dialog-title>Scheda di Sopralluogo</h2>
    <div mat-dialog-content class="dialog-content">
      <div class="form-grid">
        <!-- Prima riga -->
        <mat-form-field appearance="outline">
          <mat-label>Cerniera</mat-label>
          <mat-select [(ngModel)]="data.cerniera">
            <mat-option value="">--</mat-option>
            <mat-option value="DX">DX</mat-option>
            <mat-option value="SX">SX</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Pompa scarico</mat-label>
          <mat-select [(ngModel)]="pompaScaricoStr">
            <mat-option value="">--</mat-option>
            <mat-option value="SI">SI</mat-option>
            <mat-option value="NO">NO</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Tensione</mat-label>
          <mat-select [(ngModel)]="data.tensione">
            <mat-option value="">--</mat-option>
            <mat-option value="220M">220M</mat-option>
            <mat-option value="220T">220T</mat-option>
            <mat-option value="380T">380T</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Allacci distanti</mat-label>
          <mat-select [(ngModel)]="allacciDistantiStr">
            <mat-option value="">--</mat-option>
            <mat-option value="SI">SI</mat-option>
            <mat-option value="NO">NO</mat-option>
          </mat-select>
        </mat-form-field>

        <!-- Seconda riga -->
        <mat-form-field appearance="outline">
          <mat-label>Ruote</mat-label>
          <mat-select [(ngModel)]="ruoteStr">
            <mat-option value="">--</mat-option>
            <mat-option value="SI">SI</mat-option>
            <mat-option value="NO">NO</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Smaltimento</mat-label>
          <mat-select [(ngModel)]="smaltimentoStr">
            <mat-option value="">--</mat-option>
            <mat-option value="SI">SI</mat-option>
            <mat-option value="NO">NO</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Necessario sopralluogo</mat-label>
          <mat-select [(ngModel)]="necessarioSopralluogoStr">
            <mat-option value="">--</mat-option>
            <mat-option value="SI">SI</mat-option>
            <mat-option value="NO">NO</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Addolcitore corrente</mat-label>
          <mat-select [(ngModel)]="data.addolcitoreCorrente">
            <mat-option value="">--</mat-option>
            <mat-option value="Automatico">Automatico</mat-option>
            <mat-option value="Normale">Normale</mat-option>
          </mat-select>
        </mat-form-field>

        <!-- Terza riga -->
        <mat-form-field appearance="outline">
          <mat-label>Passaggio (cm)</mat-label>
          <input matInput type="number" [(ngModel)]="data.passaggioCm" min="0">
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Scale</mat-label>
          <textarea matInput [(ngModel)]="data.scale" rows="2"></textarea>
        </mat-form-field>

        <!-- Quarta riga -->
        <mat-form-field appearance="outline">
          <mat-label>Macchina da smontare</mat-label>
          <mat-select [(ngModel)]="macchinaDaSmontareStr" (selectionChange)="onMacchinaDaSmontareChange()">
            <mat-option value="">--</mat-option>
            <mat-option value="SI">SI</mat-option>
            <mat-option value="NO">NO</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline" class="full-width" *ngIf="data.macchinaDaSmontare">
          <mat-label>Misure</mat-label>
          <textarea matInput [(ngModel)]="data.misure" rows="2"></textarea>
        </mat-form-field>

        <!-- Quinta riga -->
        <mat-form-field appearance="outline">
          <mat-label>Gas</mat-label>
          <mat-select [(ngModel)]="data.gas">
            <mat-option value="">--</mat-option>
            <mat-option value="Metano">Metano</mat-option>
            <mat-option value="GPL">GPL</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Gas distanza (cm)</mat-label>
          <input matInput type="number" [(ngModel)]="data.gasDistanzaCm" min="0">
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Parcheggio</mat-label>
          <mat-select [(ngModel)]="parcheggioStr">
            <mat-option value="">--</mat-option>
            <mat-option value="SI">SI</mat-option>
            <mat-option value="NO">NO</mat-option>
          </mat-select>
        </mat-form-field>

        <!-- Sesta riga -->
        <mat-form-field appearance="outline">
          <mat-label>Giorno di consegna</mat-label>
          <input matInput [matDatepicker]="picker" [(ngModel)]="giornoConsegna" placeholder="Seleziona data">
          <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
          <mat-datepicker #picker></mat-datepicker>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Ora di consegna</mat-label>
          <input matInput type="time" [(ngModel)]="oraConsegna">
        </mat-form-field>
      </div>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Annulla</button>
      <button mat-raised-button color="primary" (click)="onSave()">Salva</button>
    </div>
  `,
  styles: [`
    .dialog-content {
      min-width: 600px;
      max-height: 70vh;
      overflow-y: auto;
    }
    
    .form-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
    }
    
    .full-width {
      grid-column: 1 / -1;
    }
    
    mat-form-field {
      width: 100%;
    }
  `]
})
export class SopralluogoDialogComponent {
  pompaScaricoStr: string = '';
  allacciDistantiStr: string = '';
  ruoteStr: string = '';
  smaltimentoStr: string = '';
  necessarioSopralluogoStr: string = '';
  macchinaDaSmontareStr: string = '';
  parcheggioStr: string = '';
  giornoConsegna: Date | null = null;
  oraConsegna: string = '';

  constructor(
    public dialogRef: MatDialogRef<SopralluogoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: VoceFattura
  ) {
    // Inizializza le stringhe dai boolean
    this.pompaScaricoStr = data.pompaScarico === true ? 'SI' : data.pompaScarico === false ? 'NO' : '';
    this.allacciDistantiStr = data.allacciDistanti === true ? 'SI' : data.allacciDistanti === false ? 'NO' : '';
    this.ruoteStr = data.ruote === true ? 'SI' : data.ruote === false ? 'NO' : '';
    this.smaltimentoStr = data.smaltimento === true ? 'SI' : data.smaltimento === false ? 'NO' : '';
    this.necessarioSopralluogoStr = data.necessarioSopralluogo === true ? 'SI' : data.necessarioSopralluogo === false ? 'NO' : '';
    this.macchinaDaSmontareStr = data.macchinaDaSmontare === true ? 'SI' : data.macchinaDaSmontare === false ? 'NO' : '';
    this.parcheggioStr = data.parcheggio === true ? 'SI' : data.parcheggio === false ? 'NO' : '';
    
    // Parsa giornoOraConsegna se esiste
    if (data.giornoOraConsegna) {
      // Prova a parsare il formato "dd/MM/yyyy HH:mm" o "dd/MM/yyyy ore HH:mm"
      const parts = data.giornoOraConsegna.split(/[\sore]+/);
      if (parts.length >= 1) {
        const dateStr = parts[0].trim();
        // Prova formato italiano dd/MM/yyyy
        const dateMatch = dateStr.match(/(\d{2})\/(\d{2})\/(\d{4})/);
        if (dateMatch) {
          const day = parseInt(dateMatch[1], 10);
          const month = parseInt(dateMatch[2], 10) - 1; // I mesi in JS sono 0-based
          const year = parseInt(dateMatch[3], 10);
          this.giornoConsegna = new Date(year, month, day);
        }
      }
      // Estrai l'ora se presente
      if (parts.length >= 2) {
        const timeStr = parts[parts.length - 1].trim();
        const timeMatch = timeStr.match(/(\d{1,2}):(\d{2})/);
        if (timeMatch) {
          this.oraConsegna = timeMatch[0];
        }
      }
    }
  }

  onMacchinaDaSmontareChange() {
    if (this.macchinaDaSmontareStr !== 'SI') {
      this.data.misure = '';
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    // Converti le stringhe SI/NO in boolean
    this.data.pompaScarico = this.pompaScaricoStr === 'SI' ? true : this.pompaScaricoStr === 'NO' ? false : undefined;
    this.data.allacciDistanti = this.allacciDistantiStr === 'SI' ? true : this.allacciDistantiStr === 'NO' ? false : undefined;
    this.data.ruote = this.ruoteStr === 'SI' ? true : this.ruoteStr === 'NO' ? false : undefined;
    this.data.smaltimento = this.smaltimentoStr === 'SI' ? true : this.smaltimentoStr === 'NO' ? false : undefined;
    this.data.necessarioSopralluogo = this.necessarioSopralluogoStr === 'SI' ? true : this.necessarioSopralluogoStr === 'NO' ? false : undefined;
    this.data.macchinaDaSmontare = this.macchinaDaSmontareStr === 'SI' ? true : this.macchinaDaSmontareStr === 'NO' ? false : undefined;
    this.data.parcheggio = this.parcheggioStr === 'SI' ? true : this.parcheggioStr === 'NO' ? false : undefined;
    
    // Combina data e ora in una stringa
    if (this.giornoConsegna || this.oraConsegna) {
      let giornoOraStr = '';
      if (this.giornoConsegna) {
        const day = String(this.giornoConsegna.getDate()).padStart(2, '0');
        const month = String(this.giornoConsegna.getMonth() + 1).padStart(2, '0');
        const year = this.giornoConsegna.getFullYear();
        giornoOraStr = `${day}/${month}/${year}`;
      }
      if (this.oraConsegna) {
        if (giornoOraStr) {
          giornoOraStr += ` ore ${this.oraConsegna}`;
        } else {
          giornoOraStr = `ore ${this.oraConsegna}`;
        }
      }
      this.data.giornoOraConsegna = giornoOraStr;
    } else {
      this.data.giornoOraConsegna = undefined;
    }
    
    this.dialogRef.close(this.data);
  }
}

