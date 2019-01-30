package com.example.assignment30;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;


/**
 * DB --- 'DataBase': Class for holding current user information.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     10/23/2017
 * @last_edit   01/29/2019
 */
public class DB {
    public ProfileFragment profileFragment;
    public ReposFragment reposFragment;
    public FollowFragment followingFragment;
    public FollowFragment followersFragment;
    public NotificationsFragment notificationsFragment;
    public FollowFragment searchUsersFragment;
    public ReposFragment searchReposFragment;

    public DB() {}

    /**
     * Initialize the four page fragments. They will each have data loaded on them asynchronously.
     * Should be called from MainActivity.
     * @param mainActivity MainActivity
     */
    public void initFragments(MainActivity mainActivity) {
        profileFragment = new ProfileFragment();
        reposFragment = new ReposFragment();
        reposFragment.pageTitle = "Public Repos";
        followingFragment = new FollowFragment();
        followersFragment = new FollowFragment();
        followingFragment.setFields(mainActivity, "Following");
        followersFragment.setFields(mainActivity, "Followers");
        notificationsFragment = new NotificationsFragment();
        notificationsFragment.setFields(mainActivity, this);
        searchUsersFragment = new FollowFragment();
        searchUsersFragment.setFields(mainActivity, "Search Users");
        searchReposFragment = new ReposFragment();
        searchReposFragment.pageTitle = "Search Repos";
        Notification.setGetReason();
    }

    /**
     * Start async task that will interact over network with GitHub API
     * to start data extraction from GitHub API. Returns once task started.
     * @param param GithubParser param to call the async task with.
     */
    public void startDataExtraction(GithubParser.Param param) {
        if(param.mode[0]) { //If acquiring profile, acquire it individually right away for faster return
            GithubParser.Param profileParam = new GithubParser.Param(param.db, param.targetName,
                    new boolean[] {true, false, false, false, false, false}, param.mainActivity);
            param.mode[0] = false;
            new GithubParser().execute(profileParam);
        }
        //Acquire all other fields if specified.
        if(param.mode[1] || param.mode[2] || param.mode[3] || param.mode[4]) {
            if(param.mode[4]) {
                notificationsFragment.resetView();
            }
            new GithubParser().execute(param);
        }
    }

    public void newDataLoaded(GithubParser.ReturnInfo parsed) {
        if (parsed.repos != null) {
            if(!parsed.searchMode) { //acquired user's repos
                reposFragment.loadContent(parsed.repos);
            } else { //acquired searched repos
                searchReposFragment.loadContent(parsed.repos);
            }
        }
        if (parsed.following != null) {
            if(!parsed.searchMode) { //acquired Following
                followingFragment.loadContent(parsed.following);
            } else { //acquired searched users
                searchUsersFragment.loadContent(parsed.following);
            }
        }
        if (parsed.followers != null) {
            followersFragment.loadContent(parsed.followers);
        }
        if (parsed.profile != null) {
            parsed.profile.repos = parsed.repos;
            parsed.profile.following = parsed.following;
            parsed.profile.followers = parsed.followers;
            profileFragment.loadContent(parsed.profile);
        }
        if (parsed.notifications != null) {
            notificationsFragment.loadContent(parsed.notifications);
        }
    }

    /**
     * Reset views to clear them of info and display default loading layouts.
     */
    public void resetViews() {
        profileFragment.resetView();
        reposFragment.resetView();
        followingFragment.resetView();
        followersFragment.resetView();
    }

    /**
     * Create/Return TextView that corresponds to 'no items of a type exist',
     * e.g. user has 0 followers.
     * @return
     */
    public static TextView createTv_dne(Context context) {
        TextView tv_dne = new TextView(context);
        tv_dne.setText("There doesn't seem to be anything here.");
        return tv_dne;
    }
}
