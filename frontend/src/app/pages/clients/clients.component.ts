import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Observable} from 'rxjs';
import {ClientRegistryService} from '../../services/client-registry.service';
import {VividClient} from '../../dtos/VividClient';
import {FormInputComponent} from '../../shared/components/form-input/form-input.component';
import {ContentHeaderComponent} from "../../shared/components/content-header/content-header.component";
import {TableColumnComponent} from "../../shared/components/table/table-column.component";
import {TableComponent} from "../../shared/components/table/table.component";
import {Page} from "../../shared/components/table/datastructure";
import {EnvStatusComponent} from "../../shared/components/env-status/env-status.component";
import {BadgeComponent} from "../../shared/components/badge/badge.component";
import {LoadingIndicator} from "../../shared/components/loading-indicator/loading-indicator";

@Component({
    selector: 'app-clients',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        FormInputComponent,
        ContentHeaderComponent,
        TableColumnComponent,
        TableComponent,
        EnvStatusComponent,
        BadgeComponent,
        LoadingIndicator,
    ],
    templateUrl: './clients.component.html',
    styleUrls: ['./clients.component.css']
})
export class ClientsComponent implements OnInit {
    clients$: Observable<Page<VividClient>>;
    q: string = '';

    constructor(private clientRegistryService: ClientRegistryService) {
        this.clients$ = this.clientRegistryService.getAllClients();
    }

    ngOnInit(): void {
    }

    filteredClients(clients: VividClient[]): VividClient[] {
        if (!this.q) return clients;
        const lowerQ = this.q.toLowerCase();
        return clients.filter(c =>
            c.clientName.toLowerCase().includes(lowerQ) ||
            (c.clientToken != null && c.clientToken.toLowerCase().includes(lowerQ)) ||
            c.environmentName.toLowerCase().includes(lowerQ)
        );
    }
}
