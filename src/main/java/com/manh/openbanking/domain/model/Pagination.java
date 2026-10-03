package com.manh.openbanking.domain.model;

public record Pagination(int pageCount, int pageNumber, Integer nextPage, int pageSize, Long totalCount) {
}
