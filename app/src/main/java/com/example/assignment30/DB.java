package com.example.assignment30;

import android.content.Context;
import android.widget.TextView;


/**
 * DB --- 'DataBase': Class for holding current user information.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     10/23/2017
 * @last_edit   02/05/2019
 */
public class DB {
    public ProfileFragment profileFragment;
    public ReposFragment reposFragment;
    public FollowFragment followingFragment;
    public FollowFragment followersFragment;
    public NotificationsFragment notificationsFragment;
    public FollowFragment searchUsersFragment;
    public ReposFragment searchReposFragment;

    public int profileImage_width;
    public int profileImage_height;

    private GithubParserQueue githubParserQueue;

    public DB(int profileImage_width, int profileImage_height) {
        this.profileImage_width = profileImage_width;
        this.profileImage_height = profileImage_height;
        this.githubParserQueue = new GithubParserQueue();
    }

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
     * @param extractSeparately If true, will extract all data in separate requests (one request for each type of data requested in param.mode).
     */
    public void startDataExtraction(GithubParser.Param param, boolean extractSeparately) {
        if(extractSeparately) {
            //Create a separate GitHubParser for each type of data requested in param.mode for faster returns
            for (int i = 0; i <= 4; i++) {
                if (param.mode[i]) {
                    GithubParser.Param separateParam = new GithubParser.Param(param);
                    separateParam.setMode_IndexOnly(i); //mode[5] (search mode true/false) unchanged
                    //new GithubParser().execute(separateParam);
                    githubParserQueue.addToQueue(separateParam);
                }
            }
        } else {
            //Fetch all data in one GithubParser request
            //new GithubParser().execute(param);
            githubParserQueue.addToQueue(param);
        }
    }

    public void newDataLoaded(GithubParser.ReturnInfo parsed) {
        githubParserQueue.githubParserFinished(parsed.originalParam); //remove finished GithubParser from queue
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
        //tv_dne.setText("There doesn't seem to be anything here.");
        tv_dne.setText(R.string.noDataFound);
        return tv_dne;
    }

    public void ExtractLoginProfile_full(boolean getNotifications, boolean extractSeparately, MainActivity mainActivity) {
        String user = GithubParser.GetLoginUser();
        String loadedUser = profileFragment.GetProfileUsername();
        if(loadedUser == null || !loadedUser.equals(user)) { //don't query API if already loaded the data
            //profileFragment.resetView();
            // Only attempt to get notifications if specified and login token is set
            boolean notifications = getNotifications && GithubParser.LoginTokenSet();
            boolean[] mode = new boolean[]{true, true, true, true, notifications, false}; //get all 4: profile, repos, following, followers
            GithubParser.Param param = new GithubParser.Param(this, user, mode, profileImage_width, profileImage_height, mainActivity);
            startDataExtraction(param, extractSeparately);
        }
    }

    public void ExtractLoginRepos(MainActivity mainActivity) {
        String user = GithubParser.GetLoginUser();
        String loadedReposOwner = reposFragment.GetReposOwner();
        if(loadedReposOwner == null || !loadedReposOwner.equals(user)) { //don't query API if already loaded the data
            //reposFragment.resetView();
            GithubParser.Param param = new GithubParser.Param(this, user, null, profileImage_width, profileImage_height, mainActivity);
            param.setMode_ReposOnly();
            startDataExtraction(param, false);
        }
    }

    public void ExtractLoginFollowing(MainActivity mainActivity) {
        String user = GithubParser.GetLoginUser();
        //followingFragment.resetView();
        GithubParser.Param param = new GithubParser.Param(this, user, null, profileImage_width, profileImage_height, mainActivity);
        param.setMode_FollowingOnly();
        startDataExtraction(param, false);
    }

    public void ExtractLoginFollowers(MainActivity mainActivity) {
        String user = GithubParser.GetLoginUser();
        //followersFragment.resetView();
        GithubParser.Param param = new GithubParser.Param(this, user, null, profileImage_width, profileImage_height, mainActivity);
        param.setMode_FollowersOnly();
        startDataExtraction(param, false);
    }
}
