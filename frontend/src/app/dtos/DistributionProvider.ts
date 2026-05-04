export interface DistributionProvider {
  name: string;
  type: string;
  status: string;
  details: Record<string, string>;
}
