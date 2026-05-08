import {FeatureUsageDto} from "./FeatureDto";

export interface ClientPresence {
  environmentId: string;
  environmentName: string;
  lastSeen: string;
  technologies: string[];
  clientVersion?: string;
  online: boolean;
}

export interface VividClient {
  id: string;
  clientToken?: string;
  clientName: string;
  presences: ClientPresence[];
  featureUsage: FeatureUsageDto[];
}
