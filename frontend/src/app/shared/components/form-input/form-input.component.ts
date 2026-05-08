import {Component, forwardRef, input, Input} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR} from '@angular/forms';

@Component({
    selector: 'app-form-input',
    standalone: true,
    imports: [CommonModule, FormsModule],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => FormInputComponent),
            multi: true
        }
    ],
    template: `
        <div class="input-container" [class.has-icon]="icon">
            @if (icon(); as icon) {
                <span class="material-symbols-rounded input-icon">{{ icon }}</span>
            }
            <input
                    [type]="type()"
                    [placeholder]="placeholder()"
                    [disabled]="disabled()"
                    [(ngModel)]="value"
                    (ngModelChange)="onModelChange($event)"
                    (blur)="onBlur()"
                    class="slim-input"
                    [class.icon-padding]="icon()"
            />
        </div>
    `,
    styles: [`
        input:disabled {
            color: var(--text-muted);
            cursor: not-allowed;
        }
        .input-container {
            position: relative;
            display: flex;
            align-items: center;
            width: 100%;
        }

        .input-icon {
            position: absolute;
            left: 12px;
            color: var(--text-muted);
            pointer-events: none;
            z-index: 1;
            font-size: 18px;
        }

        .slim-input {
            height: 36px;
            padding: 8px 12px;
        }

        .icon-padding {
            padding-left: 38px;
        }
    `]
})
export class FormInputComponent implements ControlValueAccessor {
    icon = input<string>('');
    type = input<string>('text');
    placeholder = input<string>('');
    @Input() value: string = '';
    disabled = input<boolean>(false);

    onChange: any = () => {
    };
    onTouched: any = () => {
    };

    writeValue(value: any): void {
        this.value = value;
    }

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    onModelChange(v: any) {
        this.value = v;
        this.onChange(v);
    }

    onBlur() {
        this.onTouched();
    }
}
