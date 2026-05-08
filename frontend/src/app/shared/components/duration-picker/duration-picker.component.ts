import {Component, EventEmitter, Input, Output} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';

@Component({
    selector: 'duration-picker',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './duration-picker.component.html',
    styleUrls: ['./duration-picker.component.css']
})
export class DurationPickerComponent {
    private _duration: string = 'PT0S';

    @Input() set duration(value: string | undefined | null) {
        const val = value || 'PT0S';
        if (val !== this._duration) {
            this._duration = val;
            this.parseDuration(val);
        }
    }

    @Input() disabled: boolean = false;

    get duration(): string {
        return this._duration;
    }

    @Output() durationChange = new EventEmitter<string>();

    minutes: number = 0;
    seconds: number = 0;

    private parseDuration(value: string) {
        const regex = /PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/;
        const matches = value.match(regex);
        if (matches) {
            const hours = matches[1] ? parseInt(matches[1], 10) : 0;
            const minutes = matches[2] ? parseInt(matches[2], 10) : 0;
            const seconds = matches[3] ? parseInt(matches[3], 10) : 0;

            this.minutes = hours * 60 + minutes;
            this.seconds = seconds;
        } else {
            this.minutes = 0;
            this.seconds = 0;
        }
    }

    update() {
        if (this.minutes === null || this.minutes < 0) this.minutes = 0;
        if (this.seconds === null || this.seconds < 0) this.seconds = 0;

        let result = 'PT';
        if (this.minutes > 0) result += `${this.minutes}M`;
        if (this.seconds > 0 || this.minutes === 0) result += `${this.seconds}S`;

        this._duration = result;
        this.durationChange.emit(result);
    }
}
