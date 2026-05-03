import {Component, ContentChild, ElementRef, input, signal, TemplateRef} from '@angular/core';
import {HeaderComponent} from "../../generic/header.component";
import {TabHeaderComponent} from "./tab-header.component";

@Component({
  selector: 'tab',
  imports: [],
  standalone: true,
  templateUrl: './tab.component.html',
  styleUrl: './tab.component.css',
})
export class TabComponent {

  title = input<string>('');
  active = signal(false);

  @ContentChild(TabHeaderComponent) customHeader?: TabHeaderComponent;
}
