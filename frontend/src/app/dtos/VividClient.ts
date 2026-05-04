export interface VividClient {
  id: string;
  clientToken?: string;
  clientName: string;
  environmentId: string;
  environmentName: string;
  lastSeen?: string;
  technologies: string[];
  clientVersion?: string;
  online: boolean;
}
