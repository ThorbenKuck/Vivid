import {inject, Injectable} from '@angular/core';
import {map, Observable} from 'rxjs';
import {VividClient} from '../dtos/VividClient';
import {HttpService} from "./http.service";
import {Page, pageOf} from "../shared/components/table/datastructure";

@Injectable({
    providedIn: 'root'
})
export class ClientRegistryService {
    private apiUrl = '/api/web/clients';
    private http = inject(HttpService);

    getAllClients(): Observable<Page<VividClient>> {
        return this.http.get<VividClient[]>(this.apiUrl).pipe(
            map((response) => pageOf(response))
        );
    }

    getClient(id: string): Observable<VividClient> {
        return this.http.get<VividClient>(`${this.apiUrl}/${id}`);
    }

    deleteClient(id: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    updateClient(id: string, clientName: string, clientToken?: string): Observable<VividClient> {
        return this.http.put<VividClient>(`${this.apiUrl}/${id}`, { clientName, clientToken });
    }

    createClient(clientName: string, clientToken?: string): Observable<VividClient> {
        return this.http.post<VividClient>(this.apiUrl, { clientName, clientToken });
    }
}
