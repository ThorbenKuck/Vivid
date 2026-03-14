import { UserDto } from './UserDto';

export interface TeamDto {
  id: string;
  name: string;
  description?: string;
  memberCount: number;
  members?: UserDto[];
}

export interface TeamCreateRequest {
  name: string;
  description?: string;
}

export interface TeamUpdateRequest {
  name?: string;
  description?: string;
}
