export interface Page<T> {
    content: T[];           // Die eigentlichen Daten
    totalCount: number;     // Gesamtzahl (wichtig für Pagination-Berechnung)
    totalPages: number;     // Hilfreich: totalCount / size
    first: boolean;       // Hilfreich für UI-Buttons
    last: boolean;        // Hilfreich für UI-Buttons
    pageable: Pageable | null;
}

export interface Pageable {
    page: number;     // Aktuelle Seite (0-basiert)
    size: number;     // Einträge pro Seite
    sort: Sort[];     // Array für Multi-Sort Support
}

export interface Sort {
    key: string;
    direction: 'ASC' | 'DESC';
}

export function pageOf<T>(content: T[], pageable?: Pageable): Page<T> {
    const size = pageable?.size ?? content.length;
    const page = pageable?.page ?? 0;
    const totalCount = content.length;
    const totalPages = Math.ceil(totalCount / (size || 1));

    return {
        content: pageable
            ? content.slice(page * size, (page + 1) * size) // Lokales Slicing
            : content,
        totalCount: totalCount,
        totalPages: totalPages,
        first: page === 0,
        last: page >= totalPages - 1,
        pageable: pageable ?? null
    };
}

export interface TableColumn<T> {
    key: keyof T | string;
    label: string;
    sortable?: boolean;
}
