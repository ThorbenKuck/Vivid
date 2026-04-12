import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {combineLatest, of, Subject, Subscription, switchMap, tap, debounceTime, forkJoin, map, timer} from 'rxjs';
import {WebFeatureManagementService} from '../../services/web-feature-management.service';
import {EnvironmentService} from '../../services/environment.service';
import {TeamManagementService} from '../../services/team-management.service';
import {EnvironmentDto, FeatureDto, FeatureEnvironmentUpdateRequest, MetadataValue, TeamDto} from '../../dtos';
import {TranslateModule} from '@ngx-translate/core';
import {FormInputComponent} from '../../shared/components/form-input/form-input.component';
import {FeatureStateService} from '../../services/feature-state.service';
import {ChiplistComponent} from '../../shared/components/chiplist/chiplist.component';
import {MetadataEditorComponent} from '../../shared/components/metadata-editor/metadata-editor.component';
import {LoadingIndicator} from "../../shared/components/loading-indicator/loading-indicator";
import {NoEnvironmentsWarningComponent} from "../../shared/components/no-environments-warning/no-environments-warning.component";
import {PermissionService} from "../../services/permission.service";
import {HasPermissionDirective} from "../../shared/directives/has-permission.directive";
import {HasEnvPermissionDirective} from "../../shared/directives/has-env-permission.directive";

@Component({
    selector: 'app-feature-details',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule, TranslateModule, FormInputComponent, ChiplistComponent, MetadataEditorComponent, LoadingIndicator, NoEnvironmentsWarningComponent, HasPermissionDirective, HasEnvPermissionDirective],
    templateUrl: './feature-details.component.html',
    styleUrls: ['./feature-details.component.css'],
    providers: [FeatureStateService]
})
export class FeatureDetailsComponent implements OnInit, OnDestroy {
    private sub?: Subscription;
    private searchSubject = new Subject<string>();
    private teamSearchSubject = new Subject<string>();
    isSearching = false;
    isSearchingTeams = false;
    tagsOptions: string[] = [];
    featureOptions: FeatureDto[] = [];
    teamOptions: TeamDto[] = [];
    showFeatureSuggestions = false;
    showTeamSuggestions = false;
    featureSearchQuery = '';
    teamSearchQuery = '';
    waitingForBackend: boolean = false;
    selectedEnvId: string | null = null;
    environments$ = this.envs.environments$;
    collapsedCards = {
        details: true,
        teams: true,
        related: true
    };

    feature$ = this.state.feature$;
    isDirty$ = this.state.isDirty$;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private api: WebFeatureManagementService,
        private envs: EnvironmentService,
        private teams: TeamManagementService,
        public state: FeatureStateService,
        public permissions: PermissionService
    ) {
    }

    ngOnInit(): void {
        this.api.getAllTags().subscribe(tags => this.tagsOptions = tags);

        // Bootstrap state from navigation if provided (instant draft editing after create)
        const navFeature = (history.state && (history.state as any).feature) as FeatureDto | undefined;
        if (navFeature) {
            console.debug('Bootstrap state from navigation', navFeature);
            this.state.load(navFeature);
            if (navFeature.environments?.length > 0) {
                this.selectedEnvId = navFeature.environments[0].environmentId;
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
                if (f.environments?.length > 0 && !this.selectedEnvId) {
                    this.selectedEnvId = f.environments[0].environmentId;
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
                    return of({ content: [] });
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

        this.teamSearchSubject.pipe(
            debounceTime(500),
            tap(() => this.isSearchingTeams = true),
            switchMap(query => {
                if (query.length < 2) {
                    this.isSearchingTeams = false;
                    this.showTeamSuggestions = false;
                    return of({ content: [] });
                }
                return this.teams.getTeams(query).pipe(
                    tap(() => this.isSearchingTeams = false)
                );
            })
        ).subscribe(page => {
            if (this.teamSearchQuery.length >= 2) {
                const currentTeams = this.state.getDraft()?.assignedTeams || [];
                this.teamOptions = page.content.filter(t => !currentTeams.find(ct => ct.id === t.id));
                this.showTeamSuggestions = true;
            }
        });
    }

    ngOnDestroy(): void {
        this.sub?.unsubscribe();
        this.searchSubject.complete();
        this.teamSearchSubject.complete();
    }

    onFeatureSearch() {
        this.searchSubject.next(this.featureSearchQuery);
    }

    onTeamSearch() {
        this.teamSearchSubject.next(this.teamSearchQuery);
    }

    assignTeam(team: TeamDto) {
        const draft = this.state.getDraft();
        if (!draft) return;
        const assignedTeams = [...(draft.assignedTeams || []), team];
        this.state.updateDraft({ assignedTeams });
        this.teamSearchQuery = '';
        this.showTeamSuggestions = false;
    }

    removeTeam(teamId: string) {
        const draft = this.state.getDraft();
        if (!draft) return;
        const assignedTeams = (draft.assignedTeams || []).filter(t => t.id !== teamId);
        this.state.updateDraft({ assignedTeams });
    }

    trackByTeamId(index: number, team: TeamDto): string {
        return team.id;
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
                    environments: currentDraft.environments,
                    assignedTeams: currentDraft.assignedTeams
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
                    environments: currentDraft.environments,
                    assignedTeams: currentDraft.assignedTeams
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

    updateMetadata(envId: string, metadata: { [key: string]: MetadataValue }) {
        this.state.updateEnvState(envId, {metadata});
    }

    updateFlags(envId: string, flags: { [key: string]: boolean }) {
        this.state.updateEnvState(envId, {flags});
    }

    toggleFlag(envId: string, flags: { [key: string]: boolean }, key: string, value: boolean) {
        const updatedFlags = { ...flags, [key]: value };
        this.updateFlags(envId, updatedFlags);
    }

    addFlag(envId: string, flags: { [key: string]: boolean }, key: string) {
        if (!key || flags[key] !== undefined) return;
        const updatedFlags = { ...flags, [key]: false };
        this.updateFlags(envId, updatedFlags);
    }

    removeFlag(envId: string, flags: { [key: string]: boolean }, key: string) {
        const updatedFlags = { ...flags };
        delete updatedFlags[key];
        this.updateFlags(envId, updatedFlags);
    }

    updateEnvField(envId: string, field: 'enabled', value: any) {
        this.state.updateEnvState(envId, {[field]: value});
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
        const sourceEnv = draft.environments.find(e => e.environmentId === sourceEnvId);
        if (!sourceEnv) return;
        this.state.updateEnvState(targetEnvId, {
            enabled: sourceEnv.enabled,
            flags: JSON.parse(JSON.stringify(sourceEnv.flags)),
            metadata: JSON.parse(JSON.stringify(sourceEnv.metadata))
        });
    }

    save(): void {
        const draft = this.state.getDraft();
        if (!draft || !draft.id) return;

        const id = draft.id;

        this.waitingForBackend = true;

        const environments: FeatureEnvironmentUpdateRequest[] = draft.environments.map(env => ({
            environmentId: env.environmentId,
            enabled: env.enabled,
            flags: env.flags,
            metadata: env.metadata
        }));

        // 1. Der API Call
        const update$ = this.api.updateFeature(id, {
            name: draft.name,
            description: draft.description,
            tags: draft.tags,
            environments: environments,
            assignedTeamIds: draft.assignedTeams.map(t => t.id)
        });

        // 2. Der Mindest-Timer
        const minWait$ = timer(500);

        // Wir führen beide gleichzeitig aus und warten auf das Ende von beiden
        forkJoin([update$, minWait$]).pipe(
            map(([apiResult, _]) => apiResult) // Wir interessieren uns nur für das Ergebnis der API
        ).subscribe({
            next: (it) => {
                this.state.load(it);
            },
            complete: () => {
                this.waitingForBackend = false;
            },
            error: (err) => {
                // Wichtig: Auch im Fehlerfall das Loading beenden
                this.waitingForBackend = false;
                console.error(err);
            }
        });
    }

    revert(): void {
        this.state.revert();
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
}
