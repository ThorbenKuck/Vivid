import { MetadataValue } from './MetadataValue';

export interface FeatureCreateRequest {
  name: string;
  description?: string;
  tags: string[];
  enabled: boolean;
  flags: { [key: string]: boolean };
  metadata: { [key: string]: MetadataValue };
}
