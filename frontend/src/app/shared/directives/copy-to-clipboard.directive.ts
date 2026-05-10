import { Directive, ElementRef, HostListener, Renderer2, OnInit, signal } from '@angular/core';

@Directive({
    selector: '[copyToClipboard]',
    standalone: true
})
export class CopyToClipboardDirective implements OnInit {
    private iconElement!: HTMLElement;
    private isCopied = signal(false);

    constructor(private el: ElementRef, private renderer: Renderer2) {}

    ngOnInit() {
        // 1. Container-Styling sicherstellen
        this.renderer.setStyle(this.el.nativeElement, 'cursor', 'pointer');
        this.renderer.setStyle(this.el.nativeElement, 'display', 'inline-flex');
        this.renderer.setStyle(this.el.nativeElement, 'align-items', 'center');
        this.renderer.setStyle(this.el.nativeElement, 'gap', '8px');

        // 2. Icon Element erstellen
        this.iconElement = this.renderer.createElement('span');
        this.renderer.addClass(this.iconElement, 'material-symbols-rounded');
        this.renderer.addClass(this.iconElement, 'copy-icon');
        this.renderer.setProperty(this.iconElement, 'innerText', 'content_copy');

        // 3. Icon ans Ende des Elements hängen
        this.renderer.appendChild(this.el.nativeElement, this.iconElement);
    }

    @HostListener('click', ['$event'])
    onClick(event: MouseEvent) {
        event.stopPropagation();
        const textToCopy = this.el.nativeElement.innerText.replace('content_copy', '').replace('check', '').trim();

        navigator.clipboard.writeText(textToCopy).then(() => {
            this.setCopiedState(true);
            setTimeout(() => this.setCopiedState(false), 2000);
        });
    }

    private setCopiedState(state: boolean) {
        this.isCopied.set(state);
        this.renderer.setProperty(this.iconElement, 'innerText', state ? 'check' : 'content_copy');
        if (state) {
            this.renderer.setStyle(this.iconElement, 'color', 'var(--accent-mint, #2ecc71)');
        } else {
            this.renderer.removeStyle(this.iconElement, 'color');
        }
    }
}