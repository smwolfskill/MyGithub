package com.example.assignment30;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

/**
 * Profile --- Class holding information about a GitHub user profile.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     10/23/2017
 * @last_edit   10/31/2017
 */
public class Profile {
    public User user;       //GitHub user this profile corresponds to
    public Drawable profilePic;
    public Repository[] repos;
    public Profile[] following;
    public Profile[] followers;

    public Profile(User user, Drawable profilePic) {
        this.user = user;
        this.profilePic = profilePic;
        this.following = null;  //not set yet
        this.followers = null;  //...
    }

    public void logData(String tag) {
        String tab = "     ";
        Log.d(tag, tab + "avatar @ '" + user.getAvatarUrl() + "'");
        Log.d(tag, tab + "name: " + user.getName());
        Log.d(tag, tab + "username: " + user.getLogin());
        Log.d(tag, tab + "bio: " + user.getBio());
        Log.d(tag, tab + "email: " + user.getEmail());
        Log.d(tag, tab + "# public repos = " + user.getPublicRepos());
        Log.d(tag, tab + "# followers = " + user.getFollowers());
        Log.d(tag, tab + "# following = " + user.getFollowing());
        Log.d(tag, tab + "created: " + user.getCreatedAt());
    }
}
