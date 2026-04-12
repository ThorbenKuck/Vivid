/// <reference types="@angular/localize" />

import {bootstrapApplication} from '@angular/platform-browser';
import {provideRouter, Routes} from '@angular/router';
import {HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi} from '@angular/common/http';
import {APP_INITIALIZER} from '@angular/core';
import {AppComponent} from './app/app.component';
import {MainContentComponent} from './app/components/main-content/main-content.component';
import {LoadingInterceptor} from './app/shared/interceptors/loading.interceptor';
import {AuthInterceptor} from './app/shared/interceptors/auth.interceptor';
import 'zone.js';
import {FeatureDetailsComponent} from "./app/pages/feature-details/feature-details.component";
import {EnvironmentsComponent} from "./app/pages/environments/environments.component";
import {EnvironmentDetailsComponent} from "./app/pages/environment-details/environment-details.component";
import {TeamsComponent} from "./app/pages/teams/teams.component";
import {TeamDetailsComponent} from "./app/pages/team-details/team-details.component";
import {DepartmentDetailsComponent} from "./app/pages/departments/department-details/department-details.component";
import {DepartmentOverviewComponent} from "./app/pages/departments/department-overview/department-overview.component";
import {PermissionService} from "./app/services/permission.service";
import {catchError, of} from "rxjs";

const routes: Routes = [
    {path: '', pathMatch: 'full', redirectTo: 'features'},
    {
        path: 'login',
        loadComponent: () => import('./app/pages/login/login.component').then(m => m.LoginComponent),
        data: {showShell: false}
    },
    {
        path: 'logout',
        loadComponent: () => import('./app/pages/logout/logout.component').then(m => m.LogoutComponent),
        data: {showShell: false}
    },
    {
        path: 'features',
        component: MainContentComponent,
    },
    {
        path: 'feature/:runningNumber',
        component: FeatureDetailsComponent,
    },
    {
        path: 'environments',
        component: EnvironmentsComponent,
    },
    {
        path: 'environment/:id',
        component: EnvironmentDetailsComponent,
    },
    {
        path: 'teams',
        component: TeamsComponent,
    },
    {
        path: 'team/:id',
        component: TeamDetailsComponent,
    },
    {
        path: 'departments',
        component: DepartmentOverviewComponent,
    },
    {
        path: 'department/:id',
        component: DepartmentDetailsComponent,
    }
];

bootstrapApplication(AppComponent, {
    providers: [
        provideRouter(routes),
        provideHttpClient(withInterceptorsFromDi()),
        {provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true},
        {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
        {
            provide: APP_INITIALIZER,
            useFactory: (permissionService: PermissionService) => () => permissionService.fetchPermissions().pipe(
                catchError(() => of(null))
            ),
            deps: [PermissionService],
            multi: true
        }
    ]
}).catch(err => console.error(err));
