import {AfterContentInit, Component, ContentChildren, QueryList, signal} from '@angular/core';
import {TabComponent} from "./tab/tab.component";
import {NgTemplateOutlet} from "@angular/common";

@Component({
  selector: 'tabs',
  imports: [
    NgTemplateOutlet
  ],
  standalone: true,
  templateUrl: './tabs.component.html',
  styleUrl: './tabs.component.css',
})
export class TabsComponent implements AfterContentInit {
  @ContentChildren(TabComponent) tabs!: QueryList<TabComponent>;

  activeTabIndex = signal<number>(0); // Wir speichern den Index
  inkBarWidth = signal<number>(0);
  inkBarLeft = signal<number>(0);

  selectTab(tab: TabComponent, element: HTMLElement, index: number) {
    this.activeTabIndex.set(index);

    // Slider-Header-Logik
    this.inkBarLeft.set(element.offsetLeft);
    this.inkBarWidth.set(element.offsetWidth);

    // Wir setzen alle Tabs auf aktiv (oder lassen sie einfach im DOM),
    // da wir sie jetzt nebeneinander rendern müssen.
    this.tabs.forEach((t, i) => t.active.set(true));
  }

  ngAfterContentInit() {
    setTimeout(() => {
      const firstButton = document.querySelector('.tabs-nav .tab-selector') as HTMLElement;
      if (this.tabs.first && firstButton) {
        this.selectTab(this.tabs.first, firstButton, 0);
      }
    }, 0);
  }
}