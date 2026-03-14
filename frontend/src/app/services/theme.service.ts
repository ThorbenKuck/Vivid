import { Injectable } from '@angular/core';

export type Theme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private current: Theme = 'dark';

  constructor() {
    this.applyTheme(this.current);
  }

  get theme(): Theme {
    return this.current;
  }

  toggle(): void {
    this.setTheme(this.current === 'dark' ? 'light' : 'dark');
  }

  setTheme(theme: Theme): void {
    this.current = theme;
    this.applyTheme(theme);
  }

  private applyTheme(theme: Theme): void {
    const html = document.documentElement;
    html.setAttribute('data-theme', theme);
  }
}
