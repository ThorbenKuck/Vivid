/// <reference types="@angular/localize" />

import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter, Routes } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { AppComponent } from './app/app.component';
import { MainContentComponent } from './app/components/main-content/main-content.component';
import { LoadingInterceptor } from './app/shared/interceptors/loading.interceptor';
import { AuthInterceptor } from './app/shared/interceptors/auth.interceptor';
import 'zone.js';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'features' },
  { path: 'login', loadComponent: () => import('./app/pages/login/login.component').then(m => m.LoginComponent) },
  { path: 'logout', loadComponent: () => import('./app/pages/logout/logout.component').then(m => m.LogoutComponent) },
  { path: 'features', component: MainContentComponent },
  { path: 'feature/:id', loadComponent: () => import('./app/pages/feature-details/feature-details.component').then(m => m.FeatureDetailsComponent) },
  { path: 'environments', loadComponent: () => import('./app/pages/environments/environments.component').then(m => m.EnvironmentsComponent) },
  { path: 'environment/:id', loadComponent: () => import('./app/pages/environment-details/environment-details.component').then(m => m.EnvironmentDetailsComponent) },
  { path: 'teams', loadComponent: () => import('./app/pages/teams/teams.component').then(m => m.TeamsComponent) },
  { path: 'team/:id', loadComponent: () => import('./app/pages/team-details/team-details.component').then(m => m.TeamDetailsComponent) }
];

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    { provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
  ]
}).catch(err => console.error(err));
