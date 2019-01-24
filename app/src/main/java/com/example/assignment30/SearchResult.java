package com.example.assignment30;

/**
 * SearchResult --- Abstract class representing base data (other than array) returned from GitHub API search.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     11/07/2017
 * @last_edit   11/07/2017
 */
public abstract class SearchResult {
    public String total_count;
    public boolean incomplete_results;
}
