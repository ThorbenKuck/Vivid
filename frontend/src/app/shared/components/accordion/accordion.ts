import {Component, ContentChildren, QueryList, AfterContentInit, Input, input} from '@angular/core';
import {ExpansionPanelComponent} from "./expansion-panel/expansion-panel.component";

@Component({
  selector: 'accordion',
  standalone: true,
  templateUrl: './accordion.html',
  styleUrl: './accordion.css',
})
export class AccordionComponent implements AfterContentInit {
  @ContentChildren(ExpansionPanelComponent) panels!: QueryList<ExpansionPanelComponent>;
  multi = input<boolean>(false);

  ngAfterContentInit() {
    let firstOpen = false;
    this.panels.forEach(panel => {
      // Wir abonnieren ein Event vom Panel (müssen wir gleich noch einbauen)
      if (!panel._collapsed()) {
        if (firstOpen) {
          panel._collapsed.set(true);
        }
        firstOpen = true;
      }
      panel.opened.subscribe(() => {
        if (!this.multi()) {
          this.closeOthers(panel);
        }
      });
    });
  }

  private closeOthers(currentPanel: ExpansionPanelComponent) {
    this.panels.forEach(p => {
      if (p !== currentPanel) {
        p._collapsed.set(true);
      }
    });
  }
}