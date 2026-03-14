import { MetadataValue } from './MetadataValue';
export interface FeatureEnvironmentUpdateRequest {
  enabled: boolean;
  flags: { [key: string]: boolean };
  metadata: { [key: string]: MetadataValue };
}
