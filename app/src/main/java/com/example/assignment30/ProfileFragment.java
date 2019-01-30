package com.example.assignment30;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ProfileFragment --- ActivityFragment for displaying GitHub Profile information.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     10/23/2017
 * @last_edit   01/29/2019
 */
public class ProfileFragment extends ActivityFragment {
    private Profile profile;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Profile");
        if (profile != null) {
            loadNewContent = true; //need to re-render the content
        }
        loadContent(); //render new content if exists
        Log.d("ProfileFragment", "onViewCreated");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_profile, container, false);
        Log.d("ProfileFragment", "onCreateView");
        return view;
    }

    @Override
    public void resetView() {
        view = null;
        profile = null;
        //loadNewContent = false;
    }

    /**
     * Gets username for the loaded profile, if any.
     * @return username ("login") of loaded profile, or null if no profile set.
     */
    public String GetProfileUsername() {
        if(profile == null || profile.user == null) {
            return null;
        } else {
            return profile.user.getLogin();
        }
    }

    /**
     * Update UI elements to load in new Profile
     * @param newProfile Profile to load UI from.
     */
    public void loadContent(Profile newProfile) {
        Log.d("profileFragment", "load new content");
        profile = newProfile;
        loadNewContent = true;
        loadContent();
    }

    /**
     * If in view, update UI elements to load in new content.
     */
    @Override
    protected void _loadContent() {
        if(profile == null) {
            return;
        }
        Log.d("ProfileFragment_lc", "profile @ '" + profile.user.getLogin() + '"');
        //Set text fields
        ((TextView) view.findViewById(R.id.tv_Profile_name)).setText(profile.user.getName());
        ((TextView) view.findViewById(R.id.tv_Profile_username)).setText(profile.user.getLogin());
        ((TextView) view.findViewById(R.id.tv_Profile_email)).setText(profile.user.getEmail());
        ((TextView) view.findViewById(R.id.tv_Profile_website)).setText(profile.user.getBlog());
        ((TextView) view.findViewById(R.id.tv_Profile_bio)).setText(profile.user.getBio());
        String memberSince = "Member since " + profile.user.getCreatedAt().toString() + ".";
        ((TextView) view.findViewById(R.id.tv_Profile_date)).setText(memberSince);

        //Set button text to show # public repos, # following, # followers.
        String reposStr = profile.user.getPublicRepos() + " Public Repos";
        String followingStr = profile.user.getFollowing() + " Following";
        String followersStr = profile.user.getFollowers() + " Follower";
        if(profile.user.getFollowers() > 1) {
            followersStr += "s"; //plural
        }
        ((Button) view.findViewById(R.id.btn_Profile_Repos)).setText(reposStr);
        ((Button) view.findViewById(R.id.btn_Profile_Following)).setText(followingStr);
        ((Button) view.findViewById(R.id.btn_Profile_Followers)).setText(followersStr);

        //Set profile pic.
        ((ImageView) view.findViewById(R.id.iv_Profile_Pic)).setImageDrawable(profile.profilePic);
    }

}
