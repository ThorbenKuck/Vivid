import {Component, ContentChild, TemplateRef} from '@angular/core';

@Component({
    selector: 'app-header', // Ich empfehle ein Präfix wie 'app-'
    standalone: true,
    template: `<ng-content></ng-content>`, // Reicht den Inhalt einfach durch
    styles: [`:host { display: block; }`]
})
export class HeaderComponent {
    // Wir suchen nach einem ng-template innerhalb des Headers
    @ContentChild(TemplateRef) templateRef?: TemplateRef<any>;
}
