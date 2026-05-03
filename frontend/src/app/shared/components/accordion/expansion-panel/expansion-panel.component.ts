import {Component, input, OnInit, output, signal} from '@angular/core';

@Component({
  selector: 'expansion-panel',
  standalone: true,
  imports: [],
  templateUrl: './expansion-panel.component.html',
  styleUrl: './expansion-panel.component.css',
})
export class ExpansionPanelComponent implements OnInit {

  showToggle = input<boolean>(true);
  // Wir geben einen Standardwert von 'true' an, falls nichts übergeben wird
  collapsed = input<boolean>(true);

  _collapsed = signal(this.collapsed());
  opened = output<void>();
  closed = output<void>();

  toggle() {
    this._collapsed.set(!this._collapsed());
    if (this._collapsed()) {
      this.closed.emit();
    } else {
      this.opened.emit();
    }
  }

  ngOnInit() {
    // Hier sind die Inputs garantiert verfügbar.
    // Wir setzen unser internes Signal auf den Wert des Inputs.
    this._collapsed.set(this.collapsed() ?? true);
  }
}
