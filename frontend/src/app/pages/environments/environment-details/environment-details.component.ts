import {Component, OnInit, signal, computed} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { EnvironmentService } from '../../../services/environment.service';
import { EnvironmentDto, EnvironmentRuleDto } from '../../../dtos';
import {CardComponent} from "../../../shared/components/card/card.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {BadgeComponent} from "../../../shared/components/badge/badge.component";
import {LoadingIndicator} from "../../../shared/components/loading-indicator/loading-indicator";

@Component({
  selector: 'app-environment-details',
  standalone: true,
    imports: [CommonModule, TranslateModule, CardComponent, FormsModule, ReactiveFormsModule, BadgeComponent, LoadingIndicator],
  templateUrl: './environment-details.component.html',
  styleUrls: ['./environment-details.component.css']
})
export class EnvironmentDetailsComponent implements OnInit {
  env = signal<EnvironmentDto | null>(null);
  allEnvironments = signal<EnvironmentDto[]>([]);
  
  availableEnvironments = computed(() => {
    const current = this.env();
    return this.allEnvironments().filter(e => e.id !== current?.id);
  });

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private envs: EnvironmentService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.envs.getById(id).subscribe(e => this.env.set(e));
    }
    this.envs.loadAll().subscribe(envs => this.allEnvironments.set(envs));
  }

  addRule(type: string) {
    const current = this.env();
    if (!current) return;
    
    const newRule: EnvironmentRuleDto = {
      type: type,
      config: type === 'MATCH_ENVIRONMENT' ? { sourceEnvironmentId: '' } : {}
    };
    
    this.env.set({
      ...current,
      rules: [...current.rules, newRule]
    });
  }

  removeRule(index: number) {
    const current = this.env();
    if (!current) return;
    
    const rules = [...current.rules];
    rules.splice(index, 1);
    
    this.env.set({
      ...current,
      rules: rules
    });
  }

  updateRuleConfig(index: number, key: string, value: any) {
    const current = this.env();
    if (!current) return;
    
    const rules = JSON.parse(JSON.stringify(current.rules));
    rules[index].config[key] = value;
    
    this.env.set({
      ...current,
      rules: rules
    });
  }

  save() {
    const current = this.env();
    if (!current) return;
    
    this.envs.update(current.id, current).subscribe(updated => {
      this.env.set(updated);
    });
  }

  back(): void {
    this.router.navigate(['/environments']);
  }
}
