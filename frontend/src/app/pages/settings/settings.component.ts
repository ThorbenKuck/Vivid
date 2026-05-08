import {Component, OnInit, signal} from '@angular/core';
import {CommonModule} from '@angular/common';
import {Observable} from 'rxjs';
import {SettingsService} from '../../services/settings.service';
import {DistributionProvider} from '../../dtos/DistributionProvider';
import {LoadingIndicator} from "../../shared/components/loading-indicator/loading-indicator";
import {ContentHeaderComponent} from "../../shared/components/content-header/content-header.component";
import {BadgeComponent} from "../../shared/components/badge/badge.component";
import {ApplicationSettings} from "../../dtos/ApplicationSettings";
import {CardComponent} from "../../shared/components/card/card.component";
import {TooltipDirective} from "../../shared/directives/tooltip.directive";
import {SlideToggleComponent} from "../../shared/components/slide-toggle/slide-toggle.component";
import {ToastService} from "../../services/toast.service";
import {TranslateModule, TranslateService} from "@ngx-translate/core";

import {DurationPickerComponent} from "../../shared/components/duration-picker/duration-picker.component";
import {DurationPipe} from "../../shared/pipes/duration.pipe";
import {PermissionService} from "../../services/permission.service";
import {HasPermissionDirective} from "../../shared/directives/has-permission.directive";

@Component({
    selector: 'app-settings',
    standalone: true,
    imports: [CommonModule, LoadingIndicator, ContentHeaderComponent, BadgeComponent, CardComponent, TooltipDirective, SlideToggleComponent, DurationPickerComponent, DurationPipe, HasPermissionDirective, TranslateModule],
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
    providers$: Observable<DistributionProvider[]>;
    settings = signal<ApplicationSettings | null>(null);

    constructor(
        private settingsService: SettingsService,
        private toastService: ToastService,
        public permissionService: PermissionService,
        private translate: TranslateService
    ) {
        this.providers$ = this.settingsService.getDistributionProviders();
        this.settingsService.getApplicationSettings().subscribe({
            next: (settings) => {
                this.settings.set(settings)
            }
        });
    }

    ngOnInit(): void {
    }

    saveSettings() {
        const targetSettings = this.settings();
        if (!targetSettings) {
            return;
        }
        this.settings.set(null)
        this.settingsService.save(targetSettings).subscribe({
            next: (settings) => {
                this.translate.get('SETTINGS.SAVED').subscribe(msg => this.toastService.success(msg));
                this.settings.set(settings);
            },
            error: err => {
                this.translate.get('SETTINGS.SAVE_FAILED', {error: err.message}).subscribe(msg => this.toastService.error(msg));
            }
        })
    }
}
