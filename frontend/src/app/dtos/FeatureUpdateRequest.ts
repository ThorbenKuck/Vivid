import {MetadataValue} from './MetadataValue';

export interface FeatureUpdateRequest {
  name?: string;
  description?: string;
  tags?: string[];
  environmentId?: string;
  enabled?: boolean;
  flags?: { [key: string]: boolean };
  metadata?: { [key: string]: MetadataValue };
  assignedTeamIds?: string[];
}
