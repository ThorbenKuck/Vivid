import { Component, Input, Output, EventEmitter } from '@angular/core';
import {CommonModule, KeyValue} from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MetadataValue } from '../../../dtos';
import {EnvironmentOverrideDto} from "../../../dtos";

export interface ValueChange {
  key: string;
  value: MetadataValue;
}

@Component({
  selector: 'app-metadata-editor',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="metadata-editor flex-col gap-sm">
      <div *ngFor="let entry of metadata | keyvalue; trackBy: trackByKey"
           class="metadata-row flex items-center gap-sm card p-sm">
        <div class="key-display flex-1 flex items-center">
          @if (hasOverride(entry)) {
            <span class="material-symbols-rounded text-xs" style="color: var(--accent-mint)">adjust</span>
          }
          <span class="text-sm font-semibold">{{ entry.key }}</span>
          <span class="text-xs text-muted">({{ entry.value['@type'] }})</span>
          @if (hasOverride(entry)) {
            <span class="text-xs overridden-info">Overridden</span>

            <button class="remove-override-button icon-btn-slim" (click)="overrideRemoved.emit(entry.key)">
              <span class="material-symbols-rounded text-xs">undo</span>
            </button>
          }
        </div>

        <div class="value-input flex-1">
          <ng-container [ngSwitch]="entry.value['@type']">
            <input *ngSwitchCase="'Boolean'" type="checkbox" [ngModel]="entry.value.content"
                   (ngModelChange)="updateValue(entry.key, entry.value['@type'], $event)" [disabled]="disabled"/>
            <input *ngSwitchCase="'Long'" type="number" [ngModel]="entry.value.content"
                   (ngModelChange)="updateValue(entry.key, entry.value['@type'], $event)" step="1"
                   [disabled]="disabled"/>
            <input *ngSwitchCase="'Double'" type="number" [ngModel]="entry.value.content"
                   (ngModelChange)="updateValue(entry.key, entry.value['@type'], $event)" step="any"
                   [disabled]="disabled"/>
            <input *ngSwitchCase="'String'" type="text" [ngModel]="entry.value.content"
                   (ngModelChange)="updateValue(entry.key, entry.value['@type'], $event)" [disabled]="disabled"/>
            <textarea *ngSwitchCase="'Json'" [ngModel]="entry.value.content | json"
                      (ngModelChange)="updateJsonValue(entry.key, $event)" [disabled]="disabled"></textarea>
            <div *ngSwitchCase="'StringList'" class="flex-col gap-xs">
              <div *ngFor="let item of entry.value.content; let i = index; trackBy: trackByIndex"
                   class="flex items-center gap-xs">
                <input type="text" [ngModel]="item" (ngModelChange)="updateStringListItem(entry.key, i, $event)"
                       [disabled]="disabled"/>
                <button class="icon-btn danger" (click)="removeStringListItem(entry.key, i)" [disabled]="disabled">
                  <span class="material-symbols-rounded">remove</span>
                </button>
              </div>
              <button class="btn-slim" (click)="addStringListItem(entry.key)" [disabled]="disabled">Add Item</button>
            </div>
          </ng-container>
        </div>

        <button class="icon-btn-slim danger" (click)="removeEntry(entry.key)" [disabled]="disabled">
          <span class="material-symbols-rounded">delete</span>
        </button>
      </div>

      <!-- New Entry -->
      <div class="new-entry card p-sm flex items-center gap-sm" *ngIf="!disabled">
        <input type="text" placeholder="Key" [(ngModel)]="newKey" class="flex-1"/>
        <select [(ngModel)]="newType" class="flex-1">
          <option value="Boolean">Boolean</option>
          <option value="String">String</option>
          <option value="Long">Long</option>
          <option value="Double">Double</option>
          <option value="StringList">String List</option>
          <option value="Json">JSON</option>
        </select>
        <button class="btn-slim primary" (click)="addEntry()" [disabled]="!newKey">
          <span class="material-symbols-rounded">add</span>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .metadata-row {
      padding: var(--spacing-sm);
    }
    .value-input input[type="number"], .value-input input[type="text"], .value-input select, .value-input textarea {
      width: 100%;
    }
    .p-sm { padding: var(--spacing-sm); }
    .ml-sm { margin-left: var(--spacing-sm); }
    .icon-btn.danger { color: var(--accent-danger); border: none; background: transparent; }
    .remove-override-button {
      padding: 5px;
    }
    .key-display {
      gap: 5px
    }
    .overridden-info {
      color: var(--accent-mint);
    }
  `]
})
export class MetadataEditorComponent {
  @Input() metadata: { [key: string]: MetadataValue } = {};
  @Input() disabled = false;
  @Input() override?: EnvironmentOverrideDto;
  @Output() metadataChange = new EventEmitter<{ [key: string]: MetadataValue }>();
  @Output() overrideRemoved = new EventEmitter<string>();

  newKey = '';
  newType: MetadataValue['@type'] = 'String';

  hasOverride(keyValue: KeyValue<string, MetadataValue>): boolean {
    if (!this.override) {
      return false
    }
    for (let metadataKey in this.override.metadata) {
      if (metadataKey === keyValue.key) {
        console.log("Found override for key:", keyValue.key, metadataKey)
        return true
      }
    }

    return false
  }

  trackByKey(index: number, item: any) {
    return item.key;
  }

  trackByIndex(index: number) {
    return index;
  }

  addEntry() {
    if (!this.newKey || this.metadata[this.newKey]) return;
    
    const newValue: MetadataValue = this.getDefaultValueForType(this.newType);
    const updatedMetadata = { ...this.metadata, [this.newKey]: newValue };
    this.metadataChange.emit(updatedMetadata);
    this.newKey = '';
  }

  removeEntry(key: string) {
    const updatedMetadata = { ...this.metadata };
    delete updatedMetadata[key];
    this.metadataChange.emit(updatedMetadata);
  }

  updateValue(key: string, type: MetadataValue['@type'], content: any) {
    const updatedMetadata = { ...this.metadata };
    updatedMetadata[key] = { '@type': type, content } as any;
    this.metadataChange.emit(updatedMetadata);
  }

  updateJsonValue(key: string, contentStr: string) {
    try {
      const content = JSON.parse(contentStr);
      this.updateValue(key, 'Json', content);
    } catch (e) {
      // Handle parse error - maybe keep as string until it's valid?
    }
  }

  updateStringListItem(key: string, index: number, value: string) {
    const entry = this.metadata[key];
    if (entry['@type'] === 'StringList') {
      const updatedList = [...entry.content];
      updatedList[index] = value;
      this.updateValue(key, 'StringList', updatedList);
    }
  }

  addStringListItem(key: string) {
    const entry = this.metadata[key];
    if (entry['@type'] === 'StringList') {
      const updatedList = [...entry.content, ''];
      this.updateValue(key, 'StringList', updatedList);
    }
  }

  removeStringListItem(key: string, index: number) {
    const entry = this.metadata[key];
    if (entry['@type'] === 'StringList') {
      const updatedList = [...entry.content];
      updatedList.splice(index, 1);
      this.updateValue(key, 'StringList', updatedList);
    }
  }

  private getDefaultValueForType(type: MetadataValue['@type']): MetadataValue {
    switch (type) {
      case 'Boolean': return { '@type': 'Boolean', content: false };
      case 'Long': return { '@type': 'Long', content: 0 };
      case 'Double': return { '@type': 'Double', content: 0 };
      case 'String': return { '@type': 'String', content: '' };
      case 'StringList': return { '@type': 'StringList', content: [] };
      case 'Json': return { '@type': 'Json', content: {} };
    }
  }
}
