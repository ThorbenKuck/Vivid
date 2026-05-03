import { MetadataValue } from './MetadataValue';
import { FeatureLinkDto } from './FeatureLinkDto';
import { NoteDto } from './NoteDto';

export enum OverrideStrategy {
  OVERRIDE = 'OVERRIDE',
  EXTEND = 'EXTEND'
}

export interface EnvironmentOverrideDto {
  environmentId: string;
  environmentName: string;
  enabled?: boolean;
  flags: { [key: string]: boolean };
  metadata: { [key: string]: MetadataValue };
  strategy: OverrideStrategy;
}

export interface FeatureDto {
  id?: string;
  runningNumber: number;
  name: string;
  key: string;
  description?: string;
  enabled: boolean;
  flags: { [key: string]: boolean };
  metadata: { [key: string]: MetadataValue };
  overrides: EnvironmentOverrideDto[];
  tags: string[];
  outgoingLinks: FeatureLinkDto[];
  notes: NoteDto[];
}
