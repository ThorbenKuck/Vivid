import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import {LoadingIndicator} from "../../shared/components/loading-indicator/loading-indicator";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, LoadingIndicator],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  issuer: string | null = null;
  loading = true;
  error: string | null = null;

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
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
        this.issuer = config.issuer || null;
        this.loading = false;
      },
      error: (err) => {
        console.error('Failed to load auth config', err);
        this.loading = false;
        this.error = 'Failed to load authentication configuration.';
      }
    });
  }

  onLogin() {
    if (this.issuer) {
      this.authService.initiateLogin(this.issuer);
    }
  }

  private handleCallback(code: string, state: string) {
    this.loading = true;
    this.authService.getAuthConfig().subscribe(config => {
      if (config.issuer) {
        this.authService.handleCallback(code, state, config.issuer).subscribe({
          next: () => {
            this.router.navigate(['/']);
          },
          error: (err) => {
            console.error('Login callback failed', err);
            this.error = 'Login failed. Please try again.';
            this.loading = false;
            this.router.navigate(['/']);
          }
        });
      } else {
        this.error = 'Auth config missing during callback.';
        this.loading = false;
      }
    });
  }
}
