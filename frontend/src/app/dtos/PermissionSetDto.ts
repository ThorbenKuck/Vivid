export interface PermissionSetDto {
  admin: boolean;
  environments: 'none' | 'read' | 'write';
  teams: 'none' | 'read' | 'write';
  departments: 'none' | 'read' | 'write';
  environment: EnvironmentPermissionsDto;
  resolved: boolean;
}

export interface EnvironmentPermissionsDto {
  admin: boolean;
  all: 'none' | 'read' | 'write';
  specific: { [envName: string]: 'none' | 'read' | 'write' };
}
