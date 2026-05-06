import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-error-403',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="error-page flex-col items-center justify-center gap-lg">
      <div class="error-icon">
        <span class="material-symbols-rounded">lock</span>
      </div>
      <div class="text-center flex-col gap-sm">
        <h1 class="text-xl">403 - Access Denied</h1>
        <p class="text-secondary">You do not have the necessary permissions to view this page.</p>
      </div>
      <button class="btn btn-primary" routerLink="/">
        <span class="material-symbols-rounded">home</span>
        Back to Home
      </button>
    </div>
  `,
  styles: [`
    .error-page {
      height: 100vh;
      width: 100%;
    }
    .error-icon {
      font-size: 64px;
      color: var(--accent-danger);
      background: var(--surface-elevated);
      padding: var(--spacing-lg);
      border-radius: var(--radius-pill);
      border: var(--border-thin);
    }
    .error-icon .material-symbols-rounded {
      font-size: inherit;
    }
  `]
})
export class Error403Component {}
