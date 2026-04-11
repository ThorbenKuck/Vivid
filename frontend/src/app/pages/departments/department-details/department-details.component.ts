import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {DepartmentService} from '../../../services/department.service';
import {TranslateModule} from '@ngx-translate/core';
import {DepartmentDto} from "../../../dtos/DepartmentDto";

@Component({
    selector: 'app-department-details',
    standalone: true,
    imports: [CommonModule, TranslateModule],
    templateUrl: './department-details.component.html',
    styleUrls: ['./department-details.component.css']
})
export class DepartmentDetailsComponent implements OnInit {
    department: (DepartmentDto & { expanded?: boolean }) | null = null;
    expandedTeams: Set<string> = new Set();

    constructor(
        private route: ActivatedRoute,
        private departmentService: DepartmentService,
        private router: Router
    ) {
    }

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
            this.departmentService.getById(id).subscribe(dept => {
                this.department = {...dept, expanded: true};
            });
        }
    }

    toggleTeam(teamId: string) {
        if (this.expandedTeams.has(teamId)) {
            this.expandedTeams.delete(teamId);
        } else {
            this.expandedTeams.add(teamId);
        }
    }

    isTeamExpanded(teamId: string): boolean {
        return this.expandedTeams.has(teamId);
    }

    back() {
        this.router.navigate(['/departments']);
    }
}
