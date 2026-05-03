import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpService } from './http.service';
import {
  FeatureDto,
  FeatureCreateRequest,
  FeatureUpdateRequest,
  FeatureLinkCreateRequest,
  FeatureEnvironmentUpdateRequest,
} from '../dtos';
import {Page} from "../shared/components/table/datastructure";

@Injectable({
  providedIn: 'root'
})
export class WebFeatureManagementService {
  private readonly baseUrl = '/api/web/features';

  constructor(private http: HttpService) {}

  getAllFeatures(q = '', page = 0, size = 20): Observable<Page<FeatureDto>> {
    const params: any = { q, page, size };
    return this.http.get<Page<FeatureDto>>(this.baseUrl, params);
  }

  getFeatureById(id: string): Observable<FeatureDto> {
    return this.http.get<FeatureDto>(`${this.baseUrl}/${id}`);
  }

  getFeatureByRunningNumber(runningNumber: number): Observable<FeatureDto> {
    return this.http.get<FeatureDto>(`${this.baseUrl}/number/${runningNumber}`);
  }

  upsertEnvironmentState(id: string, environmentId: string, request: FeatureEnvironmentUpdateRequest): Observable<FeatureDto> {
    return this.http.put<FeatureDto>(`${this.baseUrl}/${id}/environments/${environmentId}`, request);
  }

  getAllTags(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/tags`);
  }

  createFeature(request: FeatureCreateRequest): Observable<FeatureDto> {
    return this.http.post<FeatureDto>(this.baseUrl, request);
  }

  updateFeature(id: string, request: FeatureUpdateRequest): Observable<FeatureDto> {
    return this.http.put<FeatureDto>(`${this.baseUrl}/${id}`, request);
  }

  deleteFeature(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addFeatureLink(id: string, request: FeatureLinkCreateRequest): Observable<FeatureDto> {
    return this.http.post<FeatureDto>(`${this.baseUrl}/${id}/links`, request);
  }

  removeFeatureLink(id: string, linkId: string): Observable<FeatureDto> {
    return this.http.delete<FeatureDto>(`${this.baseUrl}/${id}/links/${linkId}`);
  }

  addNote(id: string, content: string): Observable<FeatureDto> {
    return this.http.post<FeatureDto>(`${this.baseUrl}/${id}/notes`, { content });
  }
}
