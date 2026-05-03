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
        path: 'features',
        component: FeaturesComponent,
    },
    {
        path: 'features/:runningNumber',
        component: FeatureDetailsComponent,
    },
    {
        path: 'environments',
        component: EnvironmentsComponent,
    },
    {
        path: 'environments/:id',
        component: EnvironmentDetailsComponent,
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
