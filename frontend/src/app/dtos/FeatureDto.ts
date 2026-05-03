import { MetadataValue } from './MetadataValue';
import { FeatureLinkDto } from './FeatureLinkDto';
import { NoteDto } from './NoteDto';

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
  key: string;
  description?: string;
  environments: FeatureEnvironmentDto[];
  tags: string[];
  outgoingLinks: FeatureLinkDto[];
  notes: NoteDto[];
}
