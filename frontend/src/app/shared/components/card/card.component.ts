import {Component, input, signal} from '@angular/core';

@Component({
  selector: 'card',
  standalone: true,
  imports: [],
  templateUrl: './card.component.html',
  styleUrl: './card.component.css',
})
export class CardComponent {

  hoverEffect = input<boolean>(false);
  collapsable = input<boolean>(false);
  collapsed = input<boolean>(false);
  _collapsed = signal<boolean>(this.collapsed());

  toggleCollapsed() {
    if (this.collapsable()) {
      this._collapsed.set(!this._collapsed());
    }
  }
}
