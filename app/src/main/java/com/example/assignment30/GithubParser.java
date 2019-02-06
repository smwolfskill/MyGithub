package com.example.assignment30;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GsonUtils;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;


/**
 * GithubParser --- Runnable class (intended to be run in worker thread)
 *                  for obtaining GitHub info via their API and parsing it.
 * @author      Scott Wolfskill, wolfski2
 * @created     10/23/2017
 * @last_edit   02/05/2019
 */
public class GithubParser extends AsyncTask<GithubParser.Param, Void, GithubParser.ReturnInfo> {
    public static final String login_user_default = "smwolfskill"; //default user if no other login info specified
    public static String login_filename = "/login.txt"; //filename containing login_user and login_token
    public static String login_user = null;   //GitHub username
    private static String login_token = null; //login_token to access read-only info for login_user
    private static String login_user_dummy = "GitHub username";
    private static String login_token_dummy = "personal access token";

    /**
     * Param --- Passed as parameter into this async task.
     */
    public static class Param {
        public DB db;
        public String targetName;
        public boolean[] mode;
        public int profileImage_width;
        public int profileImage_height;
        public MainActivity mainActivity; //for showing popup messages if exceptions

        /**
         * Create a new Param to pass into this parser.
         * @param db DB to link data to after parsing.
         * @param targetName Username of target to acquire data about.
         * @param mode 6-length boolean array.
         *             mode[0] true => acquire profile of (targetName).
         *             mode[1] true => acquire repos of (targetName).
         *             mode[2] true => acquire following of (targetName).
         *             mode[3] true => acquire followers of (targetName).
         *             mode[4] true => acquire notifications of login_user.
         *             mode[5] true => search mode.
         * @param profileImage_width minimum width of compressed profile image to obtain (if any)
         * @param profileImage_height minimum height of compressed profile image to obtain (if any)
         * @param mainActivity Non-null MainActivity to send messages to if exceptions occur.
         */
        public Param(DB db, String targetName, boolean[] mode,
                     int profileImage_width, int profileImage_height, MainActivity mainActivity) {
            this.db = db;
            this.targetName = targetName;
            if(mode.length != 6) throw new IllegalArgumentException("mode must be length 6!");
            this.mode = mode;
            this.profileImage_width = profileImage_width;
            this.profileImage_height = profileImage_height;
            this.mainActivity = mainActivity;
        }
    }

    /**
     * ReturnInfo --- Inner class containing information parsed from GitHub.
     */
    public static class ReturnInfo {
        public DB db;
        public Profile profile;
        public Repository[] repos;
        public Profile[] following;
        public Profile[] followers;
        public Notification[] notifications;
        public boolean searchMode; //if true, put searched repos in repos and searched users in following.
        public ReturnInfo(DB db, boolean searchMode) {
            this.db = db;
            profile = null;
            repos = null;
            following = null;
            followers = null;
            notifications = null;
            this.searchMode = searchMode;
        }
    }

    /**
     * Return user-specified loaded username if possible, otherwise default user.
     * @return login (or default) username.
     */
    public static String GetLoginUser() {
        if(GithubParser.login_user != null) {
            return GithubParser.login_user;
        } else {
            return GithubParser.login_user_default;
        }
    }

    /**
     * Attempt to load and set login info from login_filename.
     * @param mainActivity Reference to MainActivity for displaying a popup message on failure, if desired.
     *                     Set to null if no popup desired.
     * @return true if both login_user and login_token have been loaded and set successfully, false otherwise.
     */
    public static boolean LoadLoginInfo(MainActivity mainActivity) {
        boolean success = false;
        try {
            InputStream stream = GithubParser.class.getResourceAsStream(login_filename);
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader rd = new BufferedReader(streamReader);
            String line;
            line = rd.readLine();
            if(line != null && !line.equals(login_user_dummy) && !line.trim().equals("")) {
                login_user = line;
                line = rd.readLine();
                success = true; //mark success even without necessarily having a token
                if(line != null && !line.equals(login_token_dummy) && !line.trim().equals("")) {
                    login_token = line;
                } else {
                    String noTokenMessage = "No personal access token specified for login user " + login_user;
                    Log.d("LoadLoginInfo", noTokenMessage);
                    if(mainActivity != null) {
                        mainActivity.ShowPopup(noTokenMessage, 1000);
                    }
                }
            }
            rd.close();
        } catch (Exception e) {
            Log.e("LoadLoginInfo", "FAILED w/ exception '" + e.getMessage() + "'");
        }
        if(success) {
            Log.d("LoadLoginInfo", login_user + " login info loaded successfully from " + login_filename);
        } else {
            Log.d("LoadLoginInfo", "login info could not be loaded from " + login_filename);

        }
        return success;
    }

    /**
     * Return true if login_token is set.
     * @return true if login_token is not null, false if login_token is null.
     */
    public static boolean LoginTokenSet() {
        return (login_token != null);
    }

    /**
     * Parse required GitHub fields for user with login_token and update the DB with them.
     * @param param String array {"target name", mode} where mode is in format:
     *                      includes GET_PROFILE : get Profile of "target name"
     *                      includes GET_REPOS : get Repos of "target name"
     *                      includes GET_FOLLOWING : get users "target name" Following as Profile array.
     *                      includes GET_FOLLOWERS : get followers of "target name" as Profile array.
     */
    protected ReturnInfo doInBackground(Param... param) {
        if (param.length != 1) {
            throw new IllegalArgumentException("GithubParser: expected len. 1 Param array! " +
                    "(was " + param.length + ")");
        }
        LinkedList<String> errMessages = new LinkedList<String>(); //list of unique exception messages
        ReturnInfo parsed = new ReturnInfo(param[0].db, param[0].mode[5]);
        if (param[0].mode[0]) { //need to acquire profile w/ username 'targetName'
            try {
                parsed.profile = getProfile(param[0].targetName,
                                            param[0].profileImage_width, param[0].profileImage_height);
            } catch (Exception e) {
                errMessages.push(e.getMessage());
            }
        }
        if (param[0].mode[1]) {
            try {
                if (!param[0].mode[5]) { //get repos of user at targetName
                    parsed.repos = getRepos(param[0].targetName);
                } else { //search mode: get repos search results w/ repo name close to targetName
                    parsed.repos = getRepos_search(param[0].targetName);
                }
            } catch (IOException e) {
                if(!errMessages.contains(e.getMessage())) { //add to message if different
                    errMessages.push(e.getMessage());
                }
            }
        }
        if (param[0].mode[2]) {
            try {
                if (!param[0].mode[5]) { // get following of user at targetName
                    parsed.following = getFollowing(param[0].targetName,
                                                    param[0].profileImage_width, param[0].profileImage_height);
                } else { //search mode: get users search results w/ username close to targetName
                    parsed.following = getUsers_search(param[0].targetName,
                                                       param[0].profileImage_width, param[0].profileImage_height);
                }
            } catch (Exception e) {
                if(!errMessages.contains(e.getMessage())) { //add to message if different
                    errMessages.push(e.getMessage());
                }
            }
        }
        if (param[0].mode[3]) {
            try {
                parsed.followers = getFollowers(param[0].targetName,
                                                param[0].profileImage_width, param[0].profileImage_height);
            }  catch (Exception e) {
                if(!errMessages.contains(e.getMessage())) { //add to message if different
                    errMessages.push(e.getMessage());
                }
            }
        }
        if (param[0].mode[4]) {
            try {
                parsed.notifications = getNotifications();
            }  catch (Exception e) {
                if(!errMessages.contains(e.getMessage())) { //add to message if different
                    errMessages.push(e.getMessage());
                }
            }
        }
        //Show error message if exception encountered, and return parsed info
        sendErrorMessages(errMessages, param[0].mainActivity);
        return parsed;
    }

    protected void onPostExecute(ReturnInfo parsed) {
        parsed.db.newDataLoaded(parsed);
    }

    /**
     * Send MainActivity a Message containing a Bundle of all exception messages encountered, if any.
     * @param errorMessages Linked List containing exception messages.
     * @param mainActivity Reference to MainActivity to send message to via its handler
     */
    protected void sendErrorMessages(LinkedList<String> errorMessages, MainActivity mainActivity) {
        if(errorMessages.size() > 0) {
            Log.d("MainActivityHandler", String.valueOf(errorMessages.size()) + " exceptions!");
            String exceptionMessage_prefix = "Exception: ";
            int delayMillis = 500; //time in ms until first popup should be shown
            Bundle errorBundle = new Bundle();
            int count = 0;
            while(errorMessages.size() > 0) {
                String msg = exceptionMessage_prefix + errorMessages.removeFirst();
                errorBundle.putCharArray(String.valueOf(count), msg.toCharArray());
                count++;
            }
            if(count > 0) {
                Message message = mainActivity.handler.obtainMessage(MainActivity.HANDLER_POPUP);
                message.arg1 = delayMillis;
                message.setData(errorBundle);
                message.sendToTarget();
            }
        }
    }

    /**
     * Get and parse Profile information for the user, and return the info as a new Profile.
     * @param targetName GitHub username of target to obtain Profile of. If null, does login_user
     * @param profileImage_width Minimum width to compress targetName's profile picture to.
     * @param profileImage_height Minimum height to compress targetName's profile picture to.
     * @return Profile of user with targetName or login_token.
    */
    public Profile getProfile(String targetName, int profileImage_width, int profileImage_height) throws Exception {
        Log.d("getProfile", "Starting Profile extraction for " + targetName + "...");
        Profile profile = null;

        try {
            UserService service;
            User usr;
            if(login_token != null && targetName.equals(login_user)) { //authenticate for loaded user
                GitHubClient client = new GitHubClient();
                client.setOAuth2Token(login_token);
                service = new UserService(client);
                usr = service.getUser(); //authenticated request
            } else {
                service = new UserService();
                usr = service.getUser(targetName); //unauthenticated request
            }

            //Get profile pic:
            Drawable profilePic = ImageParser.getProfileImage(usr, profileImage_width, profileImage_height);

            //Create profile.
            profile = new Profile(usr, profilePic);
            Log.d("getProfile", "Parsed Profile:");
            profile.logData("getProfile");
            Log.d("getProfile", "Profile extraction finished successfully.");
        } catch (Exception e) {
            Log.e("getProfile", "API FAILED w/ Exception msg. '" + e.getMessage() + "'");
            throw e;
        }

        return profile;
    }

    /**
     * Get and parse Repositories that the user is in.
     * @param targetName GitHub username of target to obtain Repos of. If null, does login_token user.
     * @return Repositories Array of targetName or login_token.
     */
    public Repository[] getRepos(String targetName) throws IOException {
        Log.d("getRepos", "Starting repos extraction for " + targetName + "...");
        Repository[] repos = null;

        try {
            RepositoryService service;
            if(login_token != null && targetName.equals(login_user)) { //authenticate for loaded user
                GitHubClient client = new GitHubClient();
                client.setOAuth2Token(login_token);
                service = new RepositoryService(client);
            } else {
                service = new RepositoryService();
            }
            List<Repository> reposList = service.getRepositories(targetName);
            repos = reposList.toArray(new Repository[reposList.size()]);
            Log.d("getRepos", "Parsed " + repos.length + " Repositories.");
            /*for(int i = 0; i < repos.length; i++) {
                String tab = "     ";
                Log.d("getRepos", "Repo #" + i + ":");
                Log.d("getRepos", tab + "name = '" + repos[i].getName() + "'");
                Log.d("getRepos", tab + "owner = " + repos[i].getOwner().getLogin());
                Log.d("getRepos", tab + "desc. = '" + repos[i].getDescription() + "'");
            }*/

            Log.d("getRepos", "Repos extraction finished successfully.");
        } catch (IOException e) {
            Log.e("getRepos", "API FAILED w/ IOException msg. '" + e.getMessage() + "'");
            throw e;
        }

        return repos;
    }

    /**
     * Get and parse list of users (profile) is following.
     * @param targetName GitHub username of target to obtain Following of.
     * @param profileImage_width Minimum width to compress targetName's profile picture to.
     * @param profileImage_height Minimum height to compress targetName's profile picture to.
     */
    public Profile[] getFollowing(String targetName, int profileImage_width, int profileImage_height) throws Exception {
        Log.d("getFollow", "Starting following extraction on target '" + targetName + "'...");
        return getFollow(targetName, profileImage_width, profileImage_height, true);
    }

    /**
     * Get and parse list of users who follow (profile).
     * @param targetName GitHub username of target to obtain Followers of.
     * @param profileImage_width Minimum width to compress targetName's profile picture to.
     * @param profileImage_height Minimum height to compress targetName's profile picture to.
     */
    public Profile[] getFollowers(String targetName, int profileImage_width, int profileImage_height) throws Exception {
        Log.d("getFollow", "Starting followers extraction on target '" + targetName + "'...");
        return getFollow(targetName, profileImage_width, profileImage_height, false);
    }

    /**
     * Gets either the following or followers of a GitHub user and returns them.
     * @param targetName GitHub username of target to obtain following or followers of.
     * @param profileImage_width Minimum width to compress targetName's profile picture to.
     * @param profileImage_height Minimum height to compress targetName's profile picture to.
     * @param following If true, gets following of targetName. IF false, gets followers of targetName.
     * @return Array of Profiles that are either followers or following targetName.
     */
    private Profile[] getFollow(String targetName, int profileImage_width, int profileImage_height, boolean following) throws Exception {
        Profile[] follows = null;
        String follow = "/";
        if(following) {
            follow += "following";
        } else {
            follow += "followers";
        }

        try {
            UserService service;
            if(login_token != null && targetName.equals(login_user)) { //authenticate for loaded user
                GitHubClient client = new GitHubClient();
                client.setOAuth2Token(login_token);
                service = new UserService(client);
            } else {
                service = new UserService();
            }
            List<User> follow_usersList = null;
            if(following) {
                follow_usersList = service.getFollowing(targetName);
            } else {
                follow_usersList = service.getFollowers(targetName);
            }
            User[] follow_users = follow_usersList.toArray(new User[follow_usersList.size()]);
            /* Convert users into skeleton Profiles.
             * (missing repos, following, followers which will optionally be loaded later) */
            follows = new Profile[follow_users.length];
            for(int i = 0; i < follow_users.length; i++) {
                Drawable profileImage = ImageParser.getProfileImage(follow_users[i], profileImage_width, profileImage_height);
                follows[i] = new Profile(follow_users[i], profileImage);
                //Log.d("getFollow", follow + " parsed Profile #" + i + ":");
                //follows[i].logData("getFollow");
            }

            Log.d("getFollow", follow + " extraction finished successfully.");
        } catch (Exception e) {
            Log.e("getFollow", follow + " API FAILED w/ Exception msg. '" + e.getMessage() + "'");
            throw e;
        }

        return follows;
    }

    /**
     * Get all notifications for the login user. Requires authentication token.
     * @return Array of all notifications for the login user//, or null if not authenticated.
     */
    public Notification[] getNotifications() throws Exception {
        Log.d("getNotifications", "Starting Notifications extraction...");
        Notification[] notifications = null;
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(login_token);
        GitHubRequest request = new GitHubRequest();
        request.setUri("/notifications");
        HashMap<String, String> params = new HashMap<>(1);
        params.put("all", "true"); //show all notifications, incl. 'read' ones
        request.setParams(params);
        InputStream stream = null;
        try {
            stream = client.getStream(request);
            //TEMP FOR TESTING: Instead of API req, load Notifications from file:
            //stream = GithubParser.class.getResourceAsStream("/Notifications_json.txt");
            InputStreamReader streamReader = new InputStreamReader(stream);

            notifications = GsonUtils.fromJson(streamReader, Notification[].class);
            Log.d("getNotifications", "parsed " + notifications.length + " notifications.");
            for(int i = 0; i < notifications.length; i++) {
                Log.d("getNotifications", "Notification #" + i);
                Log.d("getNotifications", notifications[i].toString());
            }

        } catch (Exception e) {
            Log.e("getNotifications", "API FAILED w/ Exception msg. '" + e.getMessage() + "'");
            throw e;
        }

        return notifications;
    }

    /**
     * Get searched users as Profiles.
     * @param usernameCloseTo Username/login to search for.
     * @param profileImage_width Minimum width to compress all found profile pictures to.
     * @param profileImage_height Minimum height to compress all found profile pictures to.
     * @return Any close/matching Profiles found.
     */
    public Profile[] getUsers_search(String usernameCloseTo, int profileImage_width, int profileImage_height) throws Exception {
        Log.d("getUsers_search", "Starting search users extraction...");
        Profile[] profiles = null;

        GitHubClient client = new GitHubClient();
        if(login_token != null) {
            client.setOAuth2Token(login_token); //better rate limit for authenticated requests
        }
        GitHubRequest request = new GitHubRequest();
        request.setUri("/search/users");
        HashMap<String, String> params = new HashMap<>(1);
        params.put("q", usernameCloseTo); //search for usernameCloseTo
        request.setParams(params);
        InputStream stream = null;
        try {
            stream = client.getStream(request);
            InputStreamReader streamReader = new InputStreamReader(stream);
            SearchResult_Users searched = GsonUtils.fromJson(streamReader, SearchResult_Users.class);
            /* Convert users into skeleton Profiles.
             * (missing repos, following, followers which will optionally be loaded later) */
            profiles = new Profile[searched.items.length];
            for(int i = 0; i < searched.items.length; i++) {
                Drawable profileImage = ImageParser.getProfileImage(searched.items[i], profileImage_width, profileImage_height);
                profiles[i] = new Profile(searched.items[i], profileImage);
                //Log.d("getUsers_search", " parsed Profile #" + i + ":");
                //profiles[i].logData("getUsers_search");
            }

            Log.d("getUsers_search", "extraction finished successfully.");
        } catch (Exception e) {
            Log.e("getUsers_search", "API FAILED w/ Exception msg. '" + e.getMessage() + "'");
            throw e;
        }
        return profiles;
    }

    /**
     * Get searched repositories.
     * @param nameCloseTo Repository name to search for.
     * @return Any close/matching repos found.
     */
    public Repository[] getRepos_search(String nameCloseTo) throws IOException {
        Log.d("getUsers_repos", "Starting search repos extraction...");
        Repository[] repos = null;

        GitHubClient client = new GitHubClient();
        if(login_token != null) {
            client.setOAuth2Token(login_token); //better rate limit for authenticated requests
        }
        GitHubRequest request = new GitHubRequest();
        request.setUri("/search/repositories");
        HashMap<String, String> params = new HashMap<>(1);
        params.put("q", nameCloseTo); //search for nameCloseTo
        request.setParams(params);
        InputStream stream = null;
        try {
            stream = client.getStream(request);
            InputStreamReader streamReader = new InputStreamReader(stream);
            SearchResult_Repos searched = GsonUtils.fromJson(streamReader, SearchResult_Repos.class);
            /* Convert users into skeleton Profiles.
             * (missing repos, following, followers which will optionally be loaded later) */
            repos = searched.items;
            Log.d("getRepos_search", "Parsed " + repos.length + " Repositories.");
            /*for(int i = 0; i < repos.length; i++) {
                String tab = "     ";
                Log.d("getRepos_search", "Repo #" + i + ":");
                Log.d("getRepos_search", tab + "name = '" + repos[i].getName() + "'");
                Log.d("getRepos_search", tab + "owner = " + repos[i].getOwner().getLogin());
                Log.d("getRepos_search", tab + "desc. = '" + repos[i].getDescription() + "'");
            }*/

            Log.d("getRepos_search", "extraction finished successfully.");
        } catch (IOException e) {
            Log.e("getRepos_search", "API FAILED w/ IOException msg. '" + e.getMessage() + "'");
            throw e;
        }
        return repos;
    }

}
