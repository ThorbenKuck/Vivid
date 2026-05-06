/// <reference types="@angular/localize" />

import {bootstrapApplication} from '@angular/platform-browser';
import {provideRouter, Routes, withInMemoryScrolling} from '@angular/router';
import {HTTP_INTERCEPTORS, provideHttpClient, withInterceptors, withInterceptorsFromDi} from '@angular/common/http';
import {APP_INITIALIZER} from '@angular/core';
import {AppComponent} from './app/app.component';
import {FeaturesComponent} from './app/pages/features/features.component';
import {loadingInterceptor} from './app/shared/interceptors/loading.interceptor';
// import {AuthInterceptor} from './app/shared/interceptors/auth.interceptor';
import 'zone.js';
import {FeatureDetailsComponent} from "./app/pages/features/feature-details/feature-details.component";
import {EnvironmentsComponent} from "./app/pages/environments/environments.component";
import {EnvironmentDetailsComponent} from "./app/pages/environments/environment-details/environment-details.component";
import {PermissionService} from "./app/services/permission.service";
import {catchError, of} from "rxjs";
import {authInterceptor} from "./app/shared/interceptors/auth.interceptor";
import {ClientsComponent} from "./app/pages/clients/clients.component";
import {ClientDetailsComponent} from "./app/pages/clients/client-details/client-details.component";
import {SettingsComponent} from "./app/pages/settings/settings.component";
import {Error403Component} from "./app/pages/error-403/error-403.component";
import {permissionGuard} from "./app/shared/guards/permission.guard";

const routes: Routes = [
    {path: '', pathMatch: 'full', redirectTo: 'features'},
    {
        path: 'login',
        loadComponent: () => import('./app/pages/login/login.component').then(m => m.LoginComponent),
        data: {hideShell: true}
    },
    {
        path: 'logout',
        loadComponent: () => import('./app/pages/logout/logout.component').then(m => m.LogoutComponent),
        data: {hideShell: true}
    },
    {
        path: '403',
        component: Error403Component,
        data: {hideShell: true}
    },
    {
        path: 'features',
        component: FeaturesComponent,
        canActivate: [permissionGuard],
        data: {resource: 'features', action: 'read'}
    },
    {
        path: 'features/:runningNumber',
        component: FeatureDetailsComponent,
        canActivate: [permissionGuard],
        data: {resource: 'features', action: 'read'}
    },
    {
        path: 'environments',
        component: EnvironmentsComponent,
        canActivate: [permissionGuard],
        data: {resource: 'environments', action: 'read'}
    },
    {
        path: 'environments/:id',
        component: EnvironmentDetailsComponent,
        canActivate: [permissionGuard],
        data: {resource: 'environments', action: 'read'}
    },
    {
        path: 'clients',
        component: ClientsComponent,
        canActivate: [permissionGuard],
        data: {resource: 'clients', action: 'read'}
    },
    {
        path: 'clients/:id',
        component: ClientDetailsComponent,
        canActivate: [permissionGuard],
        data: {resource: 'clients', action: 'read'}
    },
    {
        path: 'settings',
        component: SettingsComponent,
        canActivate: [permissionGuard],
        data: {resource: 'settings', action: 'read'}
    }
];

bootstrapApplication(AppComponent, {
    providers: [
        provideRouter(routes, withInMemoryScrolling({
            scrollPositionRestoration: 'enabled',
        })),
        provideHttpClient(withInterceptorsFromDi(), withInterceptors([loadingInterceptor, authInterceptor])),
        // {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
        // {
        //     provide: APP_INITIALIZER,
        //     useFactory: (permissionService: PermissionService) => () => permissionService.fetchPermissions().pipe(
        //         catchError(() => of(null))
        //     ),
        //     deps: [PermissionService],
        //     multi: true
        // }
    ]
}).catch(err => console.error(err));
