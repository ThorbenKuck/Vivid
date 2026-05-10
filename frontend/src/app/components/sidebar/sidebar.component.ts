import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../services/theme.service';
import { LanguageService } from '../../services/language.service';
import { TranslateModule } from '@ngx-translate/core';
import { HasPermissionDirective } from '../../shared/directives/has-permission.directive';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule, TranslateModule, HasPermissionDirective],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() collapsed = true;

  themes = [ 'dark', 'light' ] as const;
  current = this.themeService.theme;
  currentLang = this.languageService.getCurrentLanguage();
  languages = this.languageService.getLangs();

  constructor(
      private themeService: ThemeService,
      private languageService: LanguageService
  ) {}

  setTheme(t: 'dark' | 'light') {
    this.themeService.setTheme(t);
    this.current = t;
  }

  setLanguage(lang: string) {
    this.languageService.setLanguage(lang);
    this.currentLang = lang;
  }

  toggleTheme() {
    this.themeService.toggle();
    this.current = this.themeService.theme;
  }
}
