import { Component } from '@angular/core';

@Component({
    selector: 'app-content',
    standalone: true,
    template: `<ng-content></ng-content>`,
    styles: [`:host { display: block; }`]
})
export class ContentComponent {}
