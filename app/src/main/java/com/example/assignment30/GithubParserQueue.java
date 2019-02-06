package com.example.assignment30;

import android.util.Log;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * GithubParserQueue --- Main access point to interact with GithubParser.
 *                       Holds all ongoing in-progress GitHub requests in a queue,
 *                       and controls cancelling irrelevant ongoing requests when the user
 *                       decides to load something else while data is not finished loading.
 * @author      Scott Wolfskill, wolfski2
 * @created     02/05/2017
 * @last_edit   02/05/2019
 */
public class GithubParserQueue {
    protected LinkedList<GithubParser> queue;

    public GithubParserQueue() {
        queue = new LinkedList<>();
    }

    /**
     * Creates a new GithubParser to add to the queue, and starts it.
     * @param toAdd Param of new GithubParser to create and add to the queue.
     * @return true if a GithubParser was added to the queue successfully, false if duplicate request.
     */
    public boolean addToQueue(GithubParser.Param toAdd) {
        boolean isSupplementary = false;
        for(GithubParser githubParser : queue) {
            if(githubParser.isEquivalentTo(toAdd)) {
                return false;
            }
            if(githubParser.isSupplementaryTo(toAdd)) {
                isSupplementary = true;
            }
        }
        if(!isSupplementary) {
            /* Cancels all other requests if none is supplementary.
             * ex1: if we're fetching repos of user1 and toAdd.targetName is user2,
                    cancel all other requests because user1 data is no longer relevant.
             * ex2: if we're fetching repos of user1 and new request is to fetch followers of user1,
                    don't cancel other requests because the new request is supplementary data of user1. */
            Log.d("GithubParserQueue", "Adding non-supplementary request for target '" + toAdd.targetName
                    + "' (cancelling " + queue.size() + ")");
            cancelAll();
        } else {
            Log.d("GithubParserQueue", "Adding supplementary request for target '"
                    + toAdd.targetName + "'. (" + String.valueOf(queue.size() + 1) + ")");
        }
        GithubParser newRequest = new GithubParser(toAdd);
        queue.addLast(newRequest);
        newRequest.execute(toAdd);
        return true;
    }

    /**
     * Removes a GithubParser from the queue (does not cancel it; call only when finished).
     * @param finishedParam Original Param of the finished GithubParser.
     */
    public void githubParserFinished(GithubParser.Param finishedParam) {
        if(finishedParam == null) {
            throw new IllegalArgumentException("githubParserFinished: finishedParam was null!");
        }
        Iterator<GithubParser> iterator = queue.iterator();
        while (iterator.hasNext()) {
            GithubParser githubParser = iterator.next();
            if(githubParser.isEquivalentTo(finishedParam)) {
                iterator.remove();
                Log.d("GithubParserQueue", "Removing finished request for target '"
                        + finishedParam.targetName + "'. (" + queue.size() + ")");
                return;
            }
        }
        throw new NoSuchElementException("githubParserFinished: No GithubParser with specified param for target '"
                                         + finishedParam.targetName + "' exists in the queue.");
    }

    /**
     * Cancel (interrupt) and remove all GithubParsers from the queue.
     */
    public void cancelAll() {
        Iterator<GithubParser> iterator = queue.iterator();
        while (iterator.hasNext()) {
            GithubParser githubParser = iterator.next();
            githubParser.cancel(true);
            iterator.remove();
        }
    }
}
