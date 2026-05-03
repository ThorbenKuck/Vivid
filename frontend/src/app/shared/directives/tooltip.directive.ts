import { Directive, ElementRef, HostListener, Input, OnDestroy, Renderer2 } from '@angular/core';

@Directive({
    selector: '[vividTooltip]',
    standalone: true
})
export class TooltipDirective implements OnDestroy {
    @Input('vividTooltip') tooltipText = '';
    private tooltipElement: HTMLElement | null = null;

    constructor(private el: ElementRef, private renderer: Renderer2) {}

    @HostListener('mouseenter') onMouseEnter() {
        if (!this.tooltipText) return;
        this.showTooltip();
    }

    @HostListener('mouseleave') onMouseLeave() {
        this.hideTooltip();
    }

    private showTooltip() {
        // 1. Element erstellen
        this.tooltipElement = this.renderer.createElement('span');
        const text = this.renderer.createText(this.tooltipText);
        this.renderer.appendChild(this.tooltipElement, text);

        // 2. Styling hinzufügen (direkt via Renderer oder über eine CSS Klasse)
        this.renderer.addClass(this.tooltipElement, 'tooltip-box');
        this.renderer.appendChild(document.body, this.tooltipElement);

        // 3. Position berechnen
        const hostPos = this.el.nativeElement.getBoundingClientRect();
        const tooltipPos = this.tooltipElement!.getBoundingClientRect();

        const top = hostPos.top - tooltipPos.height - 8; // 8px Abstand
        const left = hostPos.left + (hostPos.width - tooltipPos.width) / 2;

        this.renderer.setStyle(this.tooltipElement, 'top', `${top + window.scrollY}px`);
        this.renderer.setStyle(this.tooltipElement, 'left', `${left + window.scrollX}px`);
    }

    private hideTooltip() {
        if (this.tooltipElement) {
            this.renderer.removeChild(document.body, this.tooltipElement);
            this.tooltipElement = null;
        }
    }

    ngOnDestroy() {
        this.hideTooltip();
    }
}