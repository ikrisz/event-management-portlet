package com.pfiks.intelligus.events.model;

public class Pagination {

    private final int totalPages;
    private final int currentPage;
    private int rangeLow;
    private int rangeHigh;
    private final int totalResultsFound;

    public Pagination(final int currentResultsPage, final int totalSearchResults, final int maxResults) {
	totalResultsFound = totalSearchResults;
	currentPage = currentResultsPage;
	totalPages = (int) ((double) totalSearchResults / maxResults + 0.9);
	rangeLow = currentPage - 4;
	if (rangeLow < 1) {
	    rangeLow = 1;
	}
	rangeHigh = currentPage + 5;
	if (rangeHigh < 10) {
	    rangeHigh = 10;
	}
	if (rangeHigh > totalPages) {
	    rangeHigh = totalPages;
	}
	if (totalPages > 9 && rangeHigh - rangeLow < 9) {
	    rangeLow = rangeHigh - 9;
	}
    }

    public int getTotalPages() {
	return totalPages;
    }

    public int getCurrentPage() {
	return currentPage;
    }

    public int getRangeLow() {
	return rangeLow;
    }

    public int getRangeHigh() {
	return rangeHigh;
    }

    public int getTotalResultsFound() {
	return totalResultsFound;
    }

}
