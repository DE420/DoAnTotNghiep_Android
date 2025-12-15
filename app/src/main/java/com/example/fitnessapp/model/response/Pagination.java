package com.example.fitnessapp.model.response;

public class Pagination {
    private int page;
    private int pageSize;
    private int totalPages;
    private long total;
    private boolean hasMore;

    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
    public long getTotal() { return total; }
    public boolean isHasMore() { return hasMore; }
}