import {Component} from "@angular/core";

@Component({
    selector: 'content-header',
    standalone: true,
    imports: [],
    template: `
        <div class="page-header">
            <ng-content></ng-content>
        </div>
    `,
    styles: [`
        :host {
            display: flex;
            margin-bottom: var(--spacing-xl);
        }
        .page-header {
            flex-grow: 1;
            display: flex;
        }
  `]
})
export class ContentHeaderComponent {

}
