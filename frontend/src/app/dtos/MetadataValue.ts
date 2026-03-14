export type MetadataValue =
  | BooleanMetadataValue
  | LongMetadataValue
  | DoubleMetadataValue
  | StringMetadataValue
  | JsonMetadataValue
  | StringListMetadataValue;

export interface BooleanMetadataValue {
  '@type': 'Boolean';
  content: boolean;
}

export interface LongMetadataValue {
  '@type': 'Long';
  content: number;
}

export interface DoubleMetadataValue {
  '@type': 'Double';
  content: number;
}

export interface StringMetadataValue {
  '@type': 'String';
  content: string;
}

export interface JsonMetadataValue {
  '@type': 'Json';
  content: any;
}

export interface StringListMetadataValue {
  '@type': 'StringList';
  content: string[];
}
