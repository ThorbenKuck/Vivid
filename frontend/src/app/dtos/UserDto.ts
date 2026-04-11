export interface UserDto {
  id: string;
  username: string;
  email?: string;
  displayRole?: string;
}

export interface UserSyncRequest {
  keycloakId: string;
  username: string;
  email?: string;
  displayRole?: string;
}
