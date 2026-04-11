import { MetadataValue } from './MetadataValue';
export interface FeatureEnvironmentUpdateRequest {
  environmentId: string;
  enabled: boolean;
  flags: { [key: string]: boolean };
  metadata: { [key: string]: MetadataValue };
}
