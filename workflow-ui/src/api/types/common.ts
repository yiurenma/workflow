export interface ApiError {
    code: string;
    message: string;
    status: number;
    details?: Record<string, unknown>;
}

export interface PaginationParams {
    page?: number;
    pageSize?: number;
}

export interface SortParams {
    sortBy?: string;
    sortOrder?: 'asc' | 'desc';
}

export interface ApiResponse<T> {
    data: T;
    message?: string;
} 