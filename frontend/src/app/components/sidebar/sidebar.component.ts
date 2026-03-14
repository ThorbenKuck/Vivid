import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ThemeService } from '../../services/theme.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, FormsModule, TranslateModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  @Input() collapsed = false;

  themes = [ 'dark', 'light' ] as const;
  current = this.themeService.theme;

  constructor(private themeService: ThemeService) {}

  setTheme(t: 'dark' | 'light') {
    this.themeService.setTheme(t);
    this.current = t;
  }

  toggleTheme() {
    this.themeService.toggle();
    this.current = this.themeService.theme;
  }
}
