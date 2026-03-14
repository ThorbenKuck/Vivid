import { Component, EventEmitter, Output, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { EnvironmentService } from '../../services/environment.service';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, TranslateModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css']
})
export class HeaderComponent {
  @Input() collapsed = false;
  @Output() toggleSidebar = new EventEmitter<void>();

  environments$ = this.envs.environments$;
  selectedEnvironmentId$ = this.envs.selectedEnvironmentId$;

  constructor(private envs: EnvironmentService, private router: Router) {}

  onToggleSidebar() {
    this.toggleSidebar.emit();
  }

  selectEnvironment(id: string | null) {
    this.envs.select(id);
  }

  addEnvironment() {
    this.router.navigate(['/environments']);
  }
}
