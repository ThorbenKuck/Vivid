import { inject, Injectable, Renderer2, RendererFactory2 } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class LanguageService {
  private translate = inject(TranslateService);
  private renderer: Renderer2;

  constructor() {
    const rendererFactory = inject(RendererFactory2);
    this.renderer = rendererFactory.createRenderer(null, null);

    this.translate.onLangChange.subscribe((event) => {
      this.renderer.setAttribute(document.documentElement, 'lang', event.lang);
    });

    if (this.translate.currentLang) {
      this.renderer.setAttribute(document.documentElement, 'lang', this.translate.currentLang);
    }
  }

  setLanguage(lang: string) {
    this.translate.use(lang).subscribe({
      next: () => {
        localStorage.setItem('vivid_lang', lang);
      },
      error: (err) => console.error('Error setting language', err)
    });
  }

  getCurrentLanguage(): string {
    return this.translate.currentLang || 'en';
  }

  getLangs(): readonly string[] {
    return this.translate.getLangs();
  }
}
