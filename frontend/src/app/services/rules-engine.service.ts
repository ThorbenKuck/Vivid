import {inject, Injectable} from '@angular/core';
import {EnvironmentDto, FeatureDto, OverrideStrategy} from '../dtos';

export interface RuleViolation {
  type: string;
  message: string;
  severity: 'warning' | 'error';
}

export interface RuleEvaluator {
  type: string;
  evaluate(feature: FeatureDto, environment: EnvironmentDto, sourceEnvironments: EnvironmentDto[]): RuleViolation[];
}

@Injectable({providedIn: 'root'})
export class RulesEngineService {
  private evaluators: RuleEvaluator[] = [];

  constructor() {
    this.registerEvaluator(new MatchEnvironmentEvaluator());
  }

  registerEvaluator(evaluator: RuleEvaluator) {
    this.evaluators.push(evaluator);
  }

  evaluate(feature: FeatureDto, environment: EnvironmentDto, allEnvironments: EnvironmentDto[]): RuleViolation[] {
    const violations: RuleViolation[] = [];
    
    for (const rule of environment.rules) {
      const evaluator = this.evaluators.find(e => e.type === rule.type);
      if (evaluator) {
        violations.push(...evaluator.evaluate(feature, environment, allEnvironments));
      }
    }

    return violations;
  }
}

class MatchEnvironmentEvaluator implements RuleEvaluator {
  type = 'MATCH_ENVIRONMENT';

  evaluate(feature: FeatureDto, environment: EnvironmentDto, sourceEnvironments: EnvironmentDto[]): RuleViolation[] {
    const sourceEnvId = environment.rules.find(r => r.type === this.type)?.config?.sourceEnvironmentId;
    if (!sourceEnvId) return [];

    const sourceEnv = sourceEnvironments.find(e => e.id === sourceEnvId);
    if (!sourceEnv) return [];

    const currentResolved = this.resolve(feature, environment.id);
    const sourceResolved = this.resolve(feature, sourceEnv.id);

    const match = this.deepCompare(currentResolved, sourceResolved);

    if (!match) {
      return [{
        type: this.type,
        message: `Integrity Check failed: Does not match ${sourceEnv.name}`,
        severity: 'warning'
      }];
    }

    return [];
  }

  private resolve(f: FeatureDto, envId: string) {
    const override = f.overrides.find(o => o.environmentId === envId);
    if (!override) return { enabled: f.enabled, flags: f.flags, metadata: f.metadata };
    
    if (override.strategy === OverrideStrategy.OVERRIDE) {
      return {
        enabled: override.enabled ?? f.enabled,
        flags: override.flags,
        metadata: override.metadata
      };
    } else {
      const flags = { ...f.flags, ...override.flags };
      const metadata = { ...f.metadata };
      Object.entries(override.metadata).forEach(([key, value]) => {
        const existing = metadata[key];
        if (existing && existing['@type'] === 'StringList' && value['@type'] === 'StringList') {
             metadata[key] = { ...existing, content: [...existing.content, ...value.content] } as any;
        } else {
            metadata[key] = value;
        }
      });
      return {
        enabled: override.enabled ?? f.enabled,
        flags,
        metadata
      };
    }
  }

  private deepCompare(a: any, b: any): boolean {
    return JSON.stringify(a) === JSON.stringify(b);
  }
}
