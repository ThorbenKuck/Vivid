import {MetadataValue} from './MetadataValue';
import {EnvironmentOverrideUpdateRequest} from './EnvironmentOverrideUpdateRequest';

export interface FeatureUpdateRequest {
  name?: string;
  description?: string;
  tags?: string[];
  enabled?: boolean;
  flags?: { [key: string]: boolean };
  metadata?: { [key: string]: MetadataValue };
  overrides?: EnvironmentOverrideUpdateRequest[];
}
