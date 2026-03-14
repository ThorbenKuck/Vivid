import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { TeamDto, TeamCreateRequest, TeamUpdateRequest, UserDto, Page } from '../dtos';
import { HttpService } from './http.service';

@Injectable({
  providedIn: 'root'
})
export class TeamManagementService {
  private apiUrl = '/api/web/teams';
  private userApiUrl = '/api/web/users';

  constructor(private http: HttpService) {}

  getTeams(q?: string, page: number = 0, size: number = 10): Observable<Page<TeamDto>> {
    const params: any = { page, size };
    if (q) {
      params.q = q;
    }
    return this.http.get<Page<TeamDto>>(this.apiUrl, params);
  }

  getTeamById(id: string): Observable<TeamDto> {
    return this.http.get<TeamDto>(`${this.apiUrl}/${id}`);
  }

  createTeam(request: TeamCreateRequest): Observable<TeamDto> {
    return this.http.post<TeamDto>(this.apiUrl, request);
  }

  updateTeam(id: string, request: TeamUpdateRequest): Observable<TeamDto> {
    return this.http.put<TeamDto>(`${this.apiUrl}/${id}`, request);
  }

  deleteTeam(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  addMember(teamId: string, userId: string): Observable<TeamDto> {
    return this.http.post<TeamDto>(`${this.apiUrl}/${teamId}/members/${userId}`, {});
  }

  removeMember(teamId: string, userId: string): Observable<TeamDto> {
    return this.http.delete<TeamDto>(`${this.apiUrl}/${teamId}/members/${userId}`);
  }

  searchUsers(query: string): Observable<UserDto[]> {
    return this.http.get<UserDto[]>(`${this.userApiUrl}/search`, { q: query });
  }
}
