import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TranslateModule} from '@ngx-translate/core';
import {RouterLink} from '@angular/router';

@Component({
  selector: 'app-no-environments-warning',
  standalone: true,
  imports: [CommonModule, RouterLink, TranslateModule],
  template: `
    <div class="warning-card card flex-col items-center justify-center p-xl gap-md text-center">
      <div class="icon-wrapper flex items-center justify-center">
        <span class="material-symbols-rounded text-xxl" style="color: var(--accent-mint)">settings_suggest</span>
      </div>
      <div class="flex-col gap-xs">
        <h3 class="text-lg font-bold">{{ 'COMMON.NO_ENVIRONMENTS' | translate }}</h3>
        <p class="text-secondary text-sm">{{ 'COMMON.NO_ENVIRONMENTS_DESC' | translate }}</p>
      </div>
      <a routerLink="/environments" class="btn primary mt-sm">
        <span class="material-symbols-rounded">add</span>
        {{ 'COMMON.CREATE_ENVIRONMENT' | translate }}
      </a>
    </div>
  `,
  styles: [`
    .warning-card {
      min-height: 240px;
      border: 2px dashed var(--border-color);
      background: var(--surface-color);
      margin: var(--spacing-md) 0;
    }
    .icon-wrapper {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: var(--surface-elevated);
      box-shadow: var(--shadow-slim);
      margin-bottom: var(--spacing-sm);
    }
    .text-xxl {
      font-size: 32px;
    }
    .mt-sm { margin-top: var(--spacing-sm); }
    .p-xl { padding: var(--spacing-xl); }
    .text-lg { font-size: 1.125rem; }
    .font-bold { font-weight: 700; }
  `]
})
export class NoEnvironmentsWarningComponent {}
