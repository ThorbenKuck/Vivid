import {Component, OnInit, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import {LoadingIndicator} from "../../shared/components/loading-indicator/loading-indicator";
import {TranslateModule, TranslateService} from "@ngx-translate/core";
import {AuthConfigDto} from "../../dtos/AuthConfigDto";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, LoadingIndicator, TranslateModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  authConfig = signal<AuthConfigDto | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private translate: TranslateService
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      const state = params['state'];

      if (code && state) {
        this.handleCallback(code, state);
      } else {
        this.loadConfig();
      }
    });
  }

  loadConfig() {
    this.authService.getAuthConfig().subscribe({
      next: (config) => {
        this.authConfig.set(config);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Failed to load auth config', err);
        this.loading.set(false);
        this.translate.get('LOGIN.CONFIG_FAILED').subscribe(msg => this.error.set(msg));
      }
    });
  }

  onLogin() {
    const issuer = this.authConfig()?.issuer
    if (issuer) {
      this.authService.initiateLogin(issuer);
    }
  }

  private handleCallback(code: string, state: string) {
    console.log('Handling callback with code', code, 'and state', state);
    this.loading.set(true);
    this.authService.getAuthConfig().subscribe(config => {
      if (config.issuer) {
        this.authService.handleCallback(code, state, config.issuer).subscribe({
          next: () => {
            this.router.navigate(['/']);
          },
          error: (err) => {
            console.error('Login callback failed', err);
            this.translate.get('LOGIN.CALLBACK_FAILED').subscribe(msg => this.error.set(msg));
            this.loading.set(false);
            this.router.navigate(['/']);
          }
        });
      } else {
        this.translate.get('LOGIN.CONFIG_MISSING').subscribe(msg => this.error.set(msg));
        this.loading.set(false);
      }
    });
  }
}
