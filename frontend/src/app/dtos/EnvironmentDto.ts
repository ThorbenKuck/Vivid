export interface EnvironmentRuleDto {
  type: string;
  config: any;
}

export interface EnvironmentDto {
  id: string;
  name: string;
  key: string;
  description?: string;
  sortOrder: number;
  rules: EnvironmentRuleDto[];
}
