export interface PermissionSetDto {
  admin: boolean;
  environments: 'none' | 'read' | 'write';
  environment: EnvironmentPermissionsDto;
  clients: 'none' | 'read' | 'write';
  settings: 'none' | 'read' | 'write';
  features: 'none' | 'read' | 'write';
  resolved: boolean;
}

export interface EnvironmentPermissionsDto {
  admin: boolean;
  all: 'none' | 'read' | 'write';
  specific: { [envName: string]: 'none' | 'read' | 'write' };
}
