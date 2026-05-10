import {Component, inject, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {Observable} from 'rxjs';
import {ClientRegistryService} from '../../services/client-registry.service';
import {VividClient} from '../../dtos/VividClient';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {FormInputComponent} from '../../shared/components/form-input/form-input.component';
import {ContentHeaderComponent} from "../../shared/components/content-header/content-header.component";
import {TableColumnComponent} from "../../shared/components/table/table-column.component";
import {TableComponent} from "../../shared/components/table/table.component";
import {Page} from "../../shared/components/table/datastructure";
import {EnvStatusComponent} from "../../shared/components/env-status/env-status.component";
import {BadgeComponent} from "../../shared/components/badge/badge.component";
import {LoadingIndicator} from "../../shared/components/loading-indicator/loading-indicator";
import {RouterLink} from "@angular/router";
import {ModalService} from "../../services/modal.service";
import {HasPermissionDirective} from "../../shared/directives/has-permission.directive";
import {CopyToClipboardDirective} from "../../shared/directives/copy-to-clipboard.directive";

@Component({
    selector: 'app-clients',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        TranslateModule,
        FormInputComponent,
        ContentHeaderComponent,
        TableColumnComponent,
        TableComponent,
        EnvStatusComponent,
        BadgeComponent,
        LoadingIndicator,
        RouterLink,
        HasPermissionDirective,
        CopyToClipboardDirective,
    ],
    templateUrl: './clients.component.html',
    styleUrls: ['./clients.component.css']
})
export class ClientsComponent implements OnInit {
    clients$: Observable<Page<VividClient>>;
    q: string = '';
    private clientRegistryService = inject(ClientRegistryService);
    private modalService = inject(ModalService);
    private translate = inject(TranslateService);

    constructor() {
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
            c.presences.some(p => p.environmentName.toLowerCase().includes(lowerQ))
        );
    }

    deleteClient(client: VividClient) {
        this.translate.get(['CLIENTS.DELETE_TITLE', 'CLIENTS.DELETE_CONFIRM'], {name: client.clientName}).subscribe(t => {
            this.modalService.confirm(t['CLIENTS.DELETE_TITLE'], t['CLIENTS.DELETE_CONFIRM']).subscribe(confirmed => {
                if (confirmed) {
                    this.clientRegistryService.deleteClient(client.id).subscribe(() => {
                        this.clients$ = this.clientRegistryService.getAllClients();
                    });
                }
            });
        });
    }

    createClient() {
        this.translate.get(['CLIENTS.CREATE_TITLE', 'CLIENTS.CREATE_PROMPT']).subscribe(t => {
            this.modalService.prompt(t['CLIENTS.CREATE_TITLE'], t['CLIENTS.CREATE_PROMPT']).subscribe(name => {
                if (name) {
                    this.clientRegistryService.createClient(name).subscribe(() => {
                        this.clients$ = this.clientRegistryService.getAllClients();
                    });
                }
            });
        });
    }

    copied = signal(false);

    copyToClipboard(value: string) {
        // Browser API zum Kopieren
        navigator.clipboard.writeText(value).then(() => {
            this.copied.set(true);

            // Nach 2 Sekunden das Icon zurücksetzen
            setTimeout(() => {
                this.copied.set(false);
            }, 2000);
        });
    }
}
