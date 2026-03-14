import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { Subject, Subscription, of, timer, forkJoin } from 'rxjs';
import { debounceTime, switchMap, tap, map } from 'rxjs/operators';
import { TeamManagementService } from '../../services/team-management.service';
import { TeamStateService } from '../../services/team-state.service';
import { TeamDto, UserDto } from '../../dtos';
import { FormInputComponent } from '../../shared/components/form-input/form-input.component';
import { LoadingIndicator } from '../../shared/components/loading-indicator/loading-indicator';

@Component({
  selector: 'app-team-details',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, FormInputComponent, LoadingIndicator],
  templateUrl: './team-details.component.html',
  styleUrls: ['./team-details.component.css'],
  providers: [TeamStateService]
})
export class TeamDetailsComponent implements OnInit, OnDestroy {
  private sub?: Subscription;
  private userSearchSubject = new Subject<string>();
  
  userSearchQuery = '';
  userOptions: UserDto[] = [];
  showUserSuggestions = false;
  isSearchingUsers = false;
  waitingForBackend = false;

  team$ = this.state.team$;
  isDirty$ = this.state.isDirty$;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private teams: TeamManagementService,
    public state: TeamStateService
  ) {}

  ngOnInit(): void {
    this.sub = this.route.paramMap.pipe(
      tap(() => this.waitingForBackend = true),
      switchMap(params => {
        const id = params.get('id')!;
        return this.teams.getTeamById(id);
      })
    ).subscribe({
      next: (t) => {
        this.waitingForBackend = false;
        this.state.load(t);
      },
      error: () => this.waitingForBackend = false
    });

    this.userSearchSubject.pipe(
      debounceTime(500),
      tap(() => this.isSearchingUsers = true),
      switchMap(query => {
        if (query.length < 2) {
          this.isSearchingUsers = false;
          this.showUserSuggestions = false;
          return of([]);
        }
        return this.teams.searchUsers(query).pipe(
          tap(() => this.isSearchingUsers = false)
        );
      })
    ).subscribe(users => {
      if (this.userSearchQuery.length >= 2) {
        const currentMembers = this.state.getDraft()?.members || [];
        this.userOptions = users.filter(u => !currentMembers.find(m => m.id === u.id));
        this.showUserSuggestions = true;
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.userSearchSubject.complete();
  }

  onUserSearch() {
    this.userSearchSubject.next(this.userSearchQuery);
  }

  addMember(user: UserDto) {
    const draft = this.state.getDraft();
    if (!draft) return;
    
    const members = [...(draft.members || []), user];
    this.state.updateDraft({ members, memberCount: members.length });
    
    this.userSearchQuery = '';
    this.showUserSuggestions = false;
  }

  removeMember(userId: string) {
    const draft = this.state.getDraft();
    if (!draft) return;
    
    const members = (draft.members || []).filter(m => m.id !== userId);
    this.state.updateDraft({ members, memberCount: members.length });
  }

  updateField(field: keyof TeamDto, value: any) {
    const newValue = (value && typeof value === 'object' && value.target)
      ? value.target.value
      : value;
    // @ts-ignore
    this.state.updateDraft({ [field]: newValue });
  }

  save(): void {
    const draft = this.state.getDraft();
    if (!draft || !draft.id) return;

    this.waitingForBackend = true;
    
    const original = this.state.getOriginal();
    const memberActions: any[] = [];
    
    if (original) {
      const originalIds = new Set(original.members?.map(m => m.id) || []);
      const draftIds = new Set(draft.members?.map(m => m.id) || []);
      
      // Added members
      draft.members?.forEach(m => {
        if (!originalIds.has(m.id)) {
          memberActions.push(this.teams.addMember(draft.id, m.id));
        }
      });
      
      // Removed members
      original.members?.forEach(m => {
        if (!draftIds.has(m.id)) {
          memberActions.push(this.teams.removeMember(draft.id, m.id));
        }
      });
    }

    const update$ = this.teams.updateTeam(draft.id, {
      name: draft.name,
      description: draft.description
    });

    const minWait$ = timer(500);

    forkJoin([update$, ...memberActions, minWait$]).pipe(
      map(results => results[0] as TeamDto)
    ).subscribe({
      next: (it) => {
        this.teams.getTeamById(it.id).subscribe(fullTeam => {
          this.state.load(fullTeam);
          this.waitingForBackend = false;
        });
      },
      error: (err) => {
        this.waitingForBackend = false;
        console.error(err);
      }
    });
  }

  revert(): void {
    this.state.revert();
  }

  backToTeams(): void {
    this.router.navigate(['/teams']);
  }

  getInitials(name: string): string {
    if (!name) return '??';
    return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
  }

  trackByUserId(index: number, user: UserDto): string {
    return user.id;
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
