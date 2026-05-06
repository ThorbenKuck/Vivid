import { Component, Input, Output, EventEmitter, ElementRef, ViewChild, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-chiplist',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  template: `
    <div class="chiplist-wrapper" [class.is-disabled]="disabled">
      <div class="chiplist-container flex flex-wrap gap-xs">
        <div *ngFor="let tag of tags; let i = index" class="chip flex items-center gap-xs">
          <span class="tag-text text-xs">{{ tag }}</span>
          <button class="remove-btn" (click)="removeTag(i)" *ngIf="!disabled">
            <span class="material-symbols-rounded">close</span>
          </button>
        </div>
        <input 
          #tagInput
          *ngIf="!disabled"
          type="text" 
          class="tag-input text-xs" 
          [placeholder]="placeholder" 
          [(ngModel)]="newTag" 
          (input)="onInputChange()"
          (keydown)="onKeyDown($event)"
          (blur)="onBlur()"
          (focus)="onFocus()"
        />
      </div>
      
      <ul *ngIf="showSuggestions && filteredOptions.length > 0" class="suggestions-dropdown card shadow-slim">
        <li *ngFor="let option of filteredOptions; let i = index" 
            [class.active]="i === selectedIndex"
            (mousedown)="selectOption(option)"
            class="suggestion-item text-xs p-xs">
          {{ option }}
        </li>
      </ul>
    </div>
  `,
  styles: [`
    .chiplist-wrapper {
      position: relative;
      width: 100%;
    }
    .chiplist-container {
      padding: var(--spacing-sm);
      background: var(--surface-elevated);
      border-radius: var(--radius-md);
      border: var(--border-thin);
      min-height: 36px;
      transition: border-color 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .chiplist-container:focus-within {
      border-color: var(--accent-mint);
    }
    .chip {
      background: var(--surface-color);
      border: var(--border-thin);
      padding: 2px 8px;
      border-radius: var(--radius-pill);
      color: var(--text-primary);
      will-change: transform, opacity;
      transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .remove-btn {
      background: transparent;
      border: none;
      padding: 0;
      color: var(--text-muted);
      cursor: pointer;
      display: flex;
    }
    .remove-btn .material-symbols-rounded {
      font-size: 14px;
    }
    .tag-input {
      border: none;
      background: transparent;
      outline: none;
      flex: 1;
      min-width: 80px;
      padding: 0;
      color: var(--text-primary);
    }
    .suggestions-dropdown {
      position: absolute;
      top: 100%;
      left: 0;
      right: 0;
      z-index: 100;
      margin-top: 4px;
      background: var(--surface-color);
      max-height: 200px;
      overflow-y: auto;
      list-style: none;
      padding: 4px;
      border-radius: var(--radius-md);
      border: var(--border-thin);
    }
    .suggestion-item {
      cursor: pointer;
      border-radius: var(--radius-sm);
      transition: background 0.2s cubic-bezier(0.4, 0, 0.2, 1);
    }
    .suggestion-item:hover, .suggestion-item.active {
      background: var(--surface-hover);
      color: var(--accent-mint);
    }
  `]
})
export class ChiplistComponent {
  @Input() tags: string[] = [];
  @Input() options: string[] = [];
  @Input() placeholder = 'Add tag...';
  @Input() disabled = false;
  @Output() tagsChange = new EventEmitter<string[]>();

  @ViewChild('tagInput') tagInput!: ElementRef<HTMLInputElement>;

  newTag = '';
  filteredOptions: string[] = [];
  showSuggestions = false;
  selectedIndex = -1;

  onInputChange() {
    this.showSuggestions = true;
    this.filteredOptions = this.options
      .filter(opt => opt.toLowerCase().includes(this.newTag.toLowerCase()) && !this.tags.includes(opt));
    this.selectedIndex = -1;
  }

  onKeyDown(event: KeyboardEvent) {
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.selectedIndex = (this.selectedIndex + 1) % this.filteredOptions.length;
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.selectedIndex = (this.selectedIndex - 1 + this.filteredOptions.length) % this.filteredOptions.length;
    } else if (event.key === 'Enter') {
      event.preventDefault();
      if (this.selectedIndex >= 0) {
        this.selectOption(this.filteredOptions[this.selectedIndex]);
      } else {
        this.addTag();
      }
    } else if (event.key === 'Escape') {
      this.showSuggestions = false;
    }
  }

  onBlur() {
    // Delay hiding suggestions to allow mousedown on items
    setTimeout(() => this.showSuggestions = false, 200);
  }

  onFocus() {
    if (this.newTag) {
      this.onInputChange();
    }
  }

  selectOption(option: string) {
    this.newTag = option;
    this.addTag();
    this.showSuggestions = false;
  }

  addTag() {
    const tag = this.newTag.trim();
    if (tag && !this.tags.includes(tag)) {
      const updatedTags = [...this.tags, tag];
      this.tagsChange.emit(updatedTags);
      this.newTag = '';
      this.filteredOptions = [];
      this.showSuggestions = false;
    }
  }

  removeTag(index: number) {
    const updatedTags = [...this.tags];
    updatedTags.splice(index, 1);
    this.tagsChange.emit(updatedTags);
  }
}
