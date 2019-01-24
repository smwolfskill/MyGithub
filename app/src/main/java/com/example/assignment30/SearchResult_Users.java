package com.example.assignment30;

import org.eclipse.egit.github.core.User;

/**
 * SearchResult_Users --- holds data related to a GitHub API users search.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     11/07/2017
 * @last_edit   11/07/2017
 */
public class SearchResult_Users extends SearchResult {
    public User[] items;

    public SearchResult_Users() {}
}
