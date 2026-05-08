import {inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {DistributionProvider} from '../dtos/DistributionProvider';
import {HttpService} from "./http.service";
import {ApplicationSettings} from "../dtos/ApplicationSettings";

@Injectable({
    providedIn: 'root'
})
export class SettingsService {
    private apiUrl = '/api/web/settings';
    private http = inject(HttpService);

    getDistributionProviders(): Observable<DistributionProvider[]> {
        return this.http.get<DistributionProvider[]>(`${this.apiUrl}/distribution`);
    }

    getApplicationSettings(): Observable<ApplicationSettings> {
        return this.http.get<ApplicationSettings>(`${this.apiUrl}`);
    }

    save(settings: ApplicationSettings) {
        return this.http.put<ApplicationSettings>(`${this.apiUrl}`, settings)
    }
}
