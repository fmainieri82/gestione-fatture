import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-note-dialog',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title>Note</h2>
    <mat-dialog-content>
      <mat-form-field appearance="outline" class="note-field">
        <mat-label>Inserisci note per questa voce</mat-label>
        <textarea matInput [(ngModel)]="note" rows="6" placeholder="Note aggiuntive..."></textarea>
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Annulla</button>
      <button mat-raised-button color="primary" (click)="onSave()">Salva</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .note-field {
      width: 100%;
      margin-top: 16px;
    }
    
    .note-field ::ng-deep .mat-mdc-form-field {
      width: 100%;
    }
    
    .note-field ::ng-deep textarea {
      min-height: 120px;
      font-size: 14px;
      line-height: 1.5;
    }
    
    mat-dialog-content {
      min-width: 450px;
    }
  `]
})
export class NoteDialogComponent {
  note: string = '';

  constructor(
    public dialogRef: MatDialogRef<NoteDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { note: string }
  ) {
    this.note = data.note || '';
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    this.dialogRef.close(this.note);
  }
}

