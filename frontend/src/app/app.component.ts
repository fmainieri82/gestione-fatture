import { Component } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  template: `
    <div class="app-container">
      <nav class="navbar">
        <div class="nav-brand">
          <h1>📄 Gestione Fatture</h1>
        </div>
        <div class="nav-links">
          <a routerLink="/fatture" routerLinkActive="active">Fatture</a>
          <a routerLink="/clienti" routerLinkActive="active">Clienti</a>
        </div>
      </nav>
      
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      background-color: #f5f5f5;
    }
    
    .navbar {
      background-color: #3f51b5;
      color: white;
      padding: 1rem 2rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .nav-brand h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }
    
    .nav-links {
      display: flex;
      gap: 2rem;
    }
    
    .nav-links a {
      color: white;
      text-decoration: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: background-color 0.3s;
    }
    
    .nav-links a:hover {
      background-color: rgba(255,255,255,0.1);
    }
    
    .nav-links a.active {
      background-color: rgba(255,255,255,0.2);
      font-weight: 500;
    }
    
    .main-content {
      padding: 2rem;
      max-width: 1400px;
      margin: 0 auto;
    }
  `]
})
export class AppComponent {
  title = 'Gestione Fatture';
}
