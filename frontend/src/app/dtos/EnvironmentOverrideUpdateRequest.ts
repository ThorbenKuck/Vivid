import { MetadataValue } from './MetadataValue';
import { OverrideStrategy } from './FeatureDto';

export interface EnvironmentOverrideUpdateRequest {
  environmentId: string;
  enabled?: boolean;
  flags?: { [key: string]: boolean };
  metadata?: { [key: string]: MetadataValue };
  strategy?: OverrideStrategy;
}
