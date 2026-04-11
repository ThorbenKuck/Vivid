import { MetadataValue } from './MetadataValue';
import { FeatureLinkDto } from './FeatureLinkDto';
import { TeamDto } from './TeamDto';

export interface FeatureDto {
  id?: string;
  runningNumber: number;
  name: string;
  description?: string;
  enabled: boolean;
  flags: { [key: string]: boolean };
  metadata: { [key: string]: MetadataValue };
  tags: string[];
  outgoingLinks: FeatureLinkDto[];
  assignedTeams: TeamDto[];
}
