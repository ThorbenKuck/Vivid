import {MetadataValue} from './MetadataValue';
import {FeatureEnvironmentUpdateRequest} from './FeatureEnvironmentUpdateRequest';

export interface FeatureUpdateRequest {
  name?: string;
  description?: string;
  tags?: string[];
  environments?: FeatureEnvironmentUpdateRequest[];
  assignedTeamIds?: string[];
}
