package com.manh.openbanking.domain.model;

public record Pagination(int page, int pageSize, long totalRecords, int totalPages) { }
