package com.example.assignment30;

import org.eclipse.egit.github.core.Repository;

/**
 * SearchResult_Repos --- holds data related to a GitHub API repos search.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     11/07/2017
 * @last_edit   11/07/2017
 */
public class SearchResult_Repos extends SearchResult {
    public Repository[] items;

    public SearchResult_Repos() {}
}
