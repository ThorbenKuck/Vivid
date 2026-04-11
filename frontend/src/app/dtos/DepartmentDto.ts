import { TeamDto } from './TeamDto';

export interface DepartmentDto {
  id: string;
  name: string;
  description?: string;
  teams?: TeamDto[];
}
