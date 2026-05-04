import {Component, inject, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {ClientRegistryService} from '../../../services/client-registry.service';
import {VividClient} from '../../../dtos/VividClient';
import {Observable, switchMap} from 'rxjs';
import {ContentHeaderComponent} from "../../../shared/components/content-header/content-header.component";
import {CardComponent} from "../../../shared/components/card/card.component";
import {TableComponent} from "../../../shared/components/table/table.component";
import {TableColumnComponent} from "../../../shared/components/table/table-column.component";
import {EnvStatusComponent} from "../../../shared/components/env-status/env-status.component";
import {DurationPipe} from "../../../shared/pipes/duration.pipe";
import {BadgeComponent} from "../../../shared/components/badge/badge.component";
import {FormsModule} from "@angular/forms";
import {FormInputComponent} from "../../../shared/components/form-input/form-input.component";
import {ModalService} from "../../../services/modal.service";
import {Page, pageOf} from "../../../shared/components/table/datastructure";
import {LoadingIndicator} from "../../../shared/components/loading-indicator/loading-indicator";

@Component({
  selector: 'app-client-details',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ContentHeaderComponent,
    CardComponent,
    TableComponent,
    TableColumnComponent,
    EnvStatusComponent,
    DurationPipe,
    BadgeComponent,
    FormsModule,
    FormInputComponent,
    LoadingIndicator,
  ],
  templateUrl: './client-details.component.html',
  styleUrls: ['./client-details.component.css']
})
export class ClientDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private clientRegistryService = inject(ClientRegistryService);
  private modalService = inject(ModalService);

  client$!: Observable<VividClient>;

  constructor() {}

  ngOnInit(): void {
    this.client$ = this.route.params.pipe(
      switchMap(params => this.clientRegistryService.getClient(params['id']))
    );
  }

  toPage<T>(data: T[]): Page<T> {
    return pageOf(data || []);
  }

  updateClient(client: VividClient, newName: string, newToken?: string) {
    if (newName === client.clientName && newToken === client.clientToken) return;

    this.modalService.prompt(
      "Confirm Changes",
      `Changing the name or token will break existing SDK integrations. Please type the application name "${client.clientName}" to confirm:`
    ).subscribe(confirmation => {
      if (confirmation === client.clientName) {
        this.clientRegistryService.updateClient(client.id, newName, newToken).subscribe(() => {
          this.router.navigate(['/clients']);
        });
      }
    });
  }

  deleteClient(client: VividClient) {
    this.modalService.confirm("Delete Client", `Are you sure you want to delete the client "${client.clientName}"?`).subscribe(confirmed => {
      if (confirmed) {
        this.clientRegistryService.deleteClient(client.id).subscribe(() => {
          this.router.navigate(['/clients']);
        });
      }
    });
  }
}
