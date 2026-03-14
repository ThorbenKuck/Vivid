export interface FeatureLinkDto {
  id?: string;
  sourceFeatureId?: string;
  targetFeatureId: string;
  targetFeatureName?: string;
  type?: string;
}
