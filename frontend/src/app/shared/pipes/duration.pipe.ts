import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'duration',
    standalone: true
})
export class DurationPipe implements PipeTransform {
    transform(value: string | null | undefined): string {
        if (!value) return '-';

        // Match PT[n]H[n]M[n]S (Hours, Minutes, Seconds)
        const regex = /PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/;
        const matches = value.match(regex);

        if (!matches) return value;

        const hours = matches[1] ? parseInt(matches[1], 10) : 0;
        const minutes = matches[2] ? parseInt(matches[2], 10) : 0;
        const seconds = matches[3] ? parseInt(matches[3], 10) : 0;

        const parts = [];
        if (hours > 0) parts.push(`${hours}h`);
        if (minutes > 0) parts.push(`${minutes}m`);
        if (seconds > 0) parts.push(`${seconds}s`);

        if (parts.length === 0) {
            // Check for zero duration
            if (value === 'PT0S' || value === 'PT0M' || value === 'PT0H') return '0s';
            return '0s';
        }

        return parts.join(' ');
    }
}
