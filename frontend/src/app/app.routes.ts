import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: '/fatture', pathMatch: 'full' },
  { 
    path: 'clienti', 
    loadComponent: () => import('./components/clienti/lista-clienti.component').then(m => m.ListaClientiComponent)
  },
  { 
    path: 'fatture', 
    loadComponent: () => import('./components/fatture/lista-fatture.component').then(m => m.ListaFattureComponent)
  },
  { 
    path: 'fatture/nuova', 
    loadComponent: () => import('./components/fatture/form-fattura.component').then(m => m.FormFatturaComponent)
  },
  { 
    path: 'fatture/edit/:id', 
    loadComponent: () => import('./components/fatture/form-fattura.component').then(m => m.FormFatturaComponent)
  }
];
