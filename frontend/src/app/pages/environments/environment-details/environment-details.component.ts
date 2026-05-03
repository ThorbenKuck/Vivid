import {Component, OnInit, signal} from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { EnvironmentService } from '../../../services/environment.service';
import { EnvironmentDto } from '../../../dtos';
import {CardComponent} from "../../../shared/components/card/card.component";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

@Component({
  selector: 'app-environment-details',
  standalone: true,
  imports: [CommonModule, TranslateModule, CardComponent, FormsModule, ReactiveFormsModule],
  templateUrl: './environment-details.component.html',
  styleUrls: ['./environment-details.component.css']
})
export class EnvironmentDetailsComponent implements OnInit {
  env = signal<EnvironmentDto | null>(null);

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
  }

  back(): void {
    this.router.navigate(['/environments']);
  }
}
