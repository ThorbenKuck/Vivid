import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {debounceTime, forkJoin, map, Observable, of, Subject, Subscription, switchMap, tap, timer, combineLatest} from 'rxjs';
import {WebFeatureManagementService} from '../../../services/web-feature-management.service';
import {ClientRegistryService} from '../../../services/client-registry.service';
import {VividClient} from '../../../dtos/VividClient';
import {EnvironmentOverrideDto, FeatureDto, OverrideStrategy, MetadataValue, EnvironmentOverrideUpdateRequest} from '../../../dtos';
import {TranslateModule} from '@ngx-translate/core';
import {FormInputComponent} from '../../../shared/components/form-input/form-input.component';
import {FeatureStateService} from '../../../services/feature-state.service';
import {ChiplistComponent} from '../../../shared/components/chiplist/chiplist.component';
import {MetadataEditorComponent} from '../../../shared/components/metadata-editor/metadata-editor.component';
import {LoadingIndicator} from "../../../shared/components/loading-indicator/loading-indicator";
import {
    NoEnvironmentsWarningComponent
} from "../../../shared/components/no-environments-warning/no-environments-warning.component";
import {PermissionService} from "../../../services/permission.service";
import {HasPermissionDirective} from "../../../shared/directives/has-permission.directive";
import {HasEnvPermissionDirective} from "../../../shared/directives/has-env-permission.directive";
import {OverlayModule} from "@angular/cdk/overlay";
import {TimeAgoPipe} from "../../../shared/pipes/time-ago.pipe";
import {ExpansionPanelComponent} from "../../../shared/components/accordion/expansion-panel/expansion-panel.component";
import {ContentComponent} from "../../../shared/components/generic/content.component";
import {HeaderComponent} from "../../../shared/components/generic/header.component";
import {AccordionComponent} from "../../../shared/components/accordion/accordion";
import {TabsComponent} from "../../../shared/components/tabs/tabs.component";
import {TabComponent} from "../../../shared/components/tabs/tab/tab.component";
import {EnvStatusComponent} from "../../../shared/components/env-status/env-status.component";
import {TabHeaderComponent} from "../../../shared/components/tabs/tab/tab-header.component";
import {ToastService} from "../../../services/toast.service";
import {BadgeComponent} from "../../../shared/components/badge/badge.component";
import {TooltipDirective} from "../../../shared/directives/tooltip.directive";
import {VividButtonToggleComponent} from "../../../shared/components/button-toggle/button-toggle.component";
import {Page, pageOf} from "../../../shared/components/table/datastructure";
import {SlideToggleComponent} from "../../../shared/components/slide-toggle/slide-toggle.component";
import {TableComponent} from "../../../shared/components/table/table.component";
import {TableColumnComponent} from "../../../shared/components/table/table-column.component";
import {RulesEngineService, RuleViolation} from "../../../services/rules-engine.service";
import {EnvironmentService} from "../../../services/environment.service";

@Component({
    selector: 'app-feature-details',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        FormsModule,
        TranslateModule,
        FormInputComponent,
        ChiplistComponent,
        MetadataEditorComponent,
        LoadingIndicator,
        NoEnvironmentsWarningComponent,
        HasPermissionDirective,
        HasEnvPermissionDirective,
        OverlayModule,
        TimeAgoPipe,
        ExpansionPanelComponent,
        ContentComponent,
        HeaderComponent,
        AccordionComponent,
        TabsComponent,
        TabComponent,
        EnvStatusComponent,
        TabHeaderComponent,
        TabHeaderComponent,
        BadgeComponent,
        TooltipDirective,
        VividButtonToggleComponent,
        SlideToggleComponent,
        TableComponent,
        TableColumnComponent
    ],
    templateUrl: './feature-details.component.html',
    styleUrls: ['./feature-details.component.css'],
    providers: [FeatureStateService]
})
export class FeatureDetailsComponent implements OnInit, OnDestroy {
    private sub?: Subscription;
    private searchSubject = new Subject<string>();
    isSearching = false;
    tagsOptions: string[] = [];
    featureOptions: FeatureDto[] = [];
    showFeatureSuggestions = false;
    featureSearchQuery = '';
    waitingForBackend: boolean = false;
    selectedEnvId: string | null = null;
    expandNoteArea: boolean = false;
    collapsedCards: { [key: string]: boolean } = {
        details: false,
        notes: false,
        related: false,
        flags: false,
        metadata: false,
        copyFrom: true
    };

    newNoteContent = '';
    isPostingNote = false;

    feature$ = this.state.feature$;
    isDirty$ = this.state.isDirty$;

    usagePage$ = this.feature$.pipe(
        map(f => {
            if (!f || !f.usage) return pageOf([]);

            const matrix: { [appName: string]: any } = {};
            const appNames = new Set<string>();

            f.usage.forEach(u => {
                appNames.add(u.clientName);
                if (!matrix[u.clientName]) {
                    matrix[u.clientName] = { appName: u.clientName };
                }
                matrix[u.clientName][u.environmentId] = u.lastSeen;
            });

            const content = Array.from(appNames).sort().map(appName => matrix[appName]);
            return pageOf(content)
        })
    );

    allClients$: Observable<Page<VividClient>>;
    violations$ = combineLatest([
        this.feature$,
        this.environmentService.environments$
    ]).pipe(
        map(([feature, environments]) => {
            if (!feature || !environments) return {} as { [envId: string]: RuleViolation[] };
            const result: { [envId: string]: RuleViolation[] } = {};
            environments.forEach(env => {
                result[env.id] = this.rulesEngine.evaluate(feature, env, environments);
            });
            return result;
        })
    );

    hasAnyViolation$ = this.violations$.pipe(
        map(violations => Object.values(violations).some(v => v.length > 0))
    );

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private api: WebFeatureManagementService,
        private clientRegistry: ClientRegistryService,
        public state: FeatureStateService,
        public permissions: PermissionService,
        private toastService: ToastService,
        private rulesEngine: RulesEngineService,
        private environmentService: EnvironmentService,
    ) {
        this.allClients$ = this.clientRegistry.getAllClients();
        this.environmentService.loadAll();
    }

    get canWriteFeatures(): boolean {
        return this.permissions.hasPermission('features', 'write');
    }

    ngOnInit(): void {
        this.api.getAllTags().subscribe(tags => this.tagsOptions = tags);

        // Bootstrap state from navigation if provided (instant draft editing after create)
        const navFeature = (history.state && (history.state as any).feature) as FeatureDto | undefined;
        if (navFeature) {
            console.debug('Bootstrap state from navigation', navFeature);
            this.state.load(navFeature);
            if (navFeature.overrides?.length > 0) {
                this.selectedEnvId = navFeature.overrides[0].environmentId;
            }
        }

        this.sub = this.route.paramMap.pipe(
            tap(() => this.waitingForBackend = true),
            switchMap(params => {
                const runningNumberStr = params.get('runningNumber');
                if (runningNumberStr) {
                    return this.api.getFeatureByRunningNumber(parseInt(runningNumberStr));
                }
                const id = params.get('id')!;
                return this.api.getFeatureById(id);
            })
        ).subscribe({
            next: (f) => {
                this.waitingForBackend = false;
                this.state.load(f);
                if (f.overrides?.length > 0 && !this.selectedEnvId) {
                    this.selectedEnvId = f.overrides[0].environmentId;
                }
            }
        });

        this.searchSubject.pipe(
            debounceTime(1000),
            tap(() => this.isSearching = true),
            switchMap(query => {
                if (query.length < 2) {
                    this.isSearching = false;
                    this.showFeatureSuggestions = false;
                    return of({content: []});
                }
                return this.api.getAllFeatures(query).pipe(
                    tap(() => this.isSearching = false)
                );
            })
        ).subscribe(page => {
            if (this.featureSearchQuery.length >= 2) {
                this.featureOptions = page.content.filter(f => f.id !== this.state.getDraft()?.id);
                this.showFeatureSuggestions = true;
            }
        });
    }

    ngOnDestroy(): void {
        this.sub?.unsubscribe();
        this.searchSubject.complete();
    }

    onFeatureSearch() {
        this.searchSubject.next(this.featureSearchQuery);
    }

    addNote() {
        const draft = this.state.getDraft();
        if (!draft || !draft.id || !this.newNoteContent.trim()) return;

        this.isPostingNote = true;
        this.api.addNote(draft.id, this.newNoteContent).subscribe({
            next: (updatedFeature) => {
                const currentDraft = this.state.getDraft();
                this.state.load(updatedFeature);
                if (currentDraft) {
                    this.state.updateDraft({
                        name: currentDraft.name,
                        description: currentDraft.description,
                        tags: currentDraft.tags,
                    });
                }
                this.newNoteContent = '';
                this.isPostingNote = false;
            },
            error: () => this.isPostingNote = false
        });
    }

    trackByFlagKey(index: number, item: any): string {
        return item.key;
    }

    trackByLinkId(index: number, link: any): string {
        return link.id;
    }

    addLink(targetFeature: FeatureDto) {
        const draft = this.state.getDraft();
        if (!draft || !draft.id || !targetFeature.id) return;
        this.toastService.success("Linked " + targetFeature.name + " with " + draft.name)

        this.api.addFeatureLink(draft.id, {
            targetFeatureId: targetFeature.id,
            type: 'RELATED'
        }).subscribe(updatedFeature => {
            const currentDraft = this.state.getDraft();
            this.state.load(updatedFeature);
            if (currentDraft) {
                this.state.updateDraft({
                    name: currentDraft.name,
                    description: currentDraft.description,
                    tags: currentDraft.tags,
                });
            }
            this.featureSearchQuery = '';
            this.showFeatureSuggestions = false;
        });
    }

    removeLink(linkId: string) {
        const draft = this.state.getDraft();
        if (!draft || !draft.id) return;

        this.api.removeFeatureLink(draft.id, linkId).subscribe(updatedFeature => {
            const currentDraft = this.state.getDraft();
            this.state.load(updatedFeature);
            if (currentDraft) {
                this.state.updateDraft({
                    name: currentDraft.name,
                    description: currentDraft.description,
                    tags: currentDraft.tags,
                });
            }
        });
    }

    updateField(field: keyof FeatureDto, value: any) {
        const newValue = (value && typeof value === 'object' && value.target)
            ? value.target.value
            : value;
        this.state.updateDraft({[field]: newValue});
    }

    updateMetadata(envId: string | null, metadata: { [key: string]: MetadataValue }) {
        if (envId === null) {
            this.state.updateDraft({metadata});
        } else {
            this.state.updateOverride(envId, {metadata});
        }
    }

    updateFlags(envId: string | null, flags: { [key: string]: boolean }) {
        if (envId === null) {
            this.state.updateDraft({flags});
        } else {
            this.state.updateOverride(envId, {flags});
        }
    }

    toggleFlag(envId: string | null, flags: { [key: string]: boolean }, key: string, value: boolean) {
        const updatedFlags = {...flags, [key]: value};
        this.updateFlags(envId, updatedFlags);
    }

    addFlag(envId: string | null, flags: { [key: string]: boolean }, key: string) {
        if (!key || flags[key] !== undefined) return;
        const updatedFlags = {...flags, [key]: false};
        this.updateFlags(envId, updatedFlags);
    }

    removeFlag(envId: string | null, flags: { [key: string]: boolean }, key: string) {
        const updatedFlags = {...flags};
        delete updatedFlags[key];
        this.updateFlags(envId, updatedFlags);
    }

    updateOverrideField(envId: string, field: keyof EnvironmentOverrideDto, value: any) {
        this.state.updateOverride(envId, {[field]: value});
    }

    resolvePreview(f: FeatureDto, envId: string) {
        const override = f.overrides.find(o => o.environmentId === envId);
        if (!override) return { enabled: f.enabled, flags: f.flags, metadata: f.metadata };
        
        if (override.strategy === OverrideStrategy.OVERRIDE) {
            return {
                enabled: override.enabled ?? f.enabled,
                flags: override.flags,
                metadata: override.metadata
            };
        } else {
            const flags = { ...f.flags, ...override.flags };
            const metadata = { ...f.metadata };
            Object.entries(override.metadata).forEach(([key, value]) => {
                const existing = metadata[key];
                if (existing && existing['@type'] === 'StringList' && value['@type'] === 'StringList') {
                     metadata[key] = { ...existing, content: [...existing.content, ...value.content] } as any;
                } else {
                    metadata[key] = value;
                }
            });
            return {
                enabled: override.enabled ?? f.enabled,
                flags,
                metadata
            };
        }
    }
    
    isFlagOverridden(f: FeatureDto, envId: string, key: string): boolean {
        const override = f.overrides.find(o => o.environmentId === envId);
        return !!override && override.flags[key] !== undefined;
    }

    isMetadataOverridden(f: FeatureDto, envId: string, key: string): boolean {
        const override = f.overrides.find(o => o.environmentId === envId);
        return !!override && override.metadata[key] !== undefined;
    }

    removeOverride(envId: string, type: 'enabled' | 'flag' | 'metadata', key?: string) {
        const draft = this.state.getDraft();
        if (!draft) return;
        const override = draft.overrides.find(o => o.environmentId === envId);
        if (!override) return;

        if (type === 'enabled') {
            this.state.updateOverride(envId, { enabled: undefined });
        } else if (type === 'flag' && key) {
            const flags = { ...override.flags };
            delete flags[key];
            this.state.updateOverride(envId, { flags });
        } else if (type === 'metadata' && key) {
            const metadata = { ...override.metadata };
            delete metadata[key];
            this.state.updateOverride(envId, { metadata });
        }
    }

    selectEnv(envId: string) {
        this.selectedEnvId = envId;
    }

    toggleCard(card: keyof typeof this.collapsedCards) {
        this.collapsedCards[card] = !this.collapsedCards[card];
    }

    copyFrom(targetEnvId: string, sourceEnvId: string) {
        const draft = this.state.getDraft();
        if (!draft) return;
        const sourceOvr = draft.overrides.find(o => o.environmentId === sourceEnvId);
        if (!sourceOvr) return;
        this.state.updateOverride(targetEnvId, {
            enabled: sourceOvr.enabled,
            flags: JSON.parse(JSON.stringify(sourceOvr.flags)),
            metadata: JSON.parse(JSON.stringify(sourceOvr.metadata)),
            strategy: sourceOvr.strategy
        });
    }

    save(): void {
        const draft = this.state.getDraft();
        if (!draft || !draft.id) return;

        const id = draft.id;

        this.waitingForBackend = true;

        const overrides: EnvironmentOverrideUpdateRequest[] = draft.overrides.map(ovr => ({
            environmentId: ovr.environmentId,
            enabled: ovr.enabled,
            flags: ovr.flags,
            metadata: ovr.metadata,
            strategy: ovr.strategy
        }));

        // 1. Der API Call
        const update$ = this.api.updateFeature(id, {
            name: draft.name,
            description: draft.description,
            tags: draft.tags,
            enabled: draft.enabled,
            flags: draft.flags,
            metadata: draft.metadata,
            overrides: overrides
        });

        // 2. Der Mindest-Timer
        const minWait$ = timer(500);

        // Wir führen beide gleichzeitig aus und warten auf das Ende von beiden
        forkJoin([update$, minWait$]).pipe(
            map(([apiResult, _]) => apiResult) // Wir interessieren uns nur für das Ergebnis der API
        ).subscribe({
            next: (it) => {
                this.state.load(it);
                this.toastService.success('Update successful!', 5000);
            },
            complete: () => {
                this.waitingForBackend = false;
            },
            error: (err) => {
                // Wichtig: Auch im Fehlerfall das Loading beenden
                this.toastService.error('Update failed', 5000);
                this.waitingForBackend = false;
                console.error(err);
            }
        });
    }

    revert(): void {
        this.state.revert();
        this.toastService.success('Changes reverted', 5000);
    }

    backToFeatures(): void {
        this.router.navigate(['/features']);
    }

    startDragging(event: MouseEvent, element: HTMLElement) {
        event.preventDefault();
        const startX = event.clientX - element.getBoundingClientRect().left;
        const startY = event.clientY - element.getBoundingClientRect().top;

        const onMouseMove = (moveEvent: MouseEvent) => {
            element.style.position = 'fixed';
            element.style.left = `${moveEvent.clientX - startX}px`;
            element.style.top = `${moveEvent.clientY - startY}px`;
            element.style.bottom = 'auto';
            element.style.right = 'auto';
            element.style.transform = 'none';
        };

        const onMouseUp = () => {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
        };

        document.addEventListener('mousemove', onMouseMove);
        document.addEventListener('mouseup', onMouseUp);
    }

    protected readonly JSON = JSON;
}
