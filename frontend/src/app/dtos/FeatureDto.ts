import { MetadataValue } from './MetadataValue';
import { FeatureLinkDto } from './FeatureLinkDto';
import { TeamDto } from './TeamDto';

export interface FeatureEnvironmentDto {
  environmentId: string;
  environmentName: string;
  enabled: boolean;
  flags: { [key: string]: boolean };
  metadata: { [key: string]: MetadataValue };
}

export interface FeatureDto {
  id?: string;
  runningNumber: number;
  name: string;
  description?: string;
  environments: FeatureEnvironmentDto[];
  tags: string[];
  outgoingLinks: FeatureLinkDto[];
  assignedTeams: TeamDto[];
}
