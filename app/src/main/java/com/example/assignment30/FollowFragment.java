package com.example.assignment30;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * FollowFragment --- ActivityFragment for displaying GitHub users that current user is following,
 *                    or users following current user.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     10/23/2017
 * @last_edit   11/07/2017
 */
public class FollowFragment extends ActivityFragment implements View.OnClickListener {
    public MainActivity parent = null;
    public String pageTitle = ""; //either "Following", "Followers", or "Search Users"
    private Profile[] follow = null; //following or followers
    private int[] followXmlIds = null; //ids of User xml obj's holding User info of following or followers

    public void setFields(MainActivity parent, String pageTitle) {
        this.parent = parent;
        this.pageTitle = pageTitle;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("FollowFragment", String.valueOf(getActivity().findViewById(R.id.iv_navHeader_profilePic) == null));
        getActivity().setTitle(pageTitle);
        if(follow != null) {
            loadNewContent = true; //need to re-render the content
        }
        loadContent(); //render new content if exists
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_users, container, false);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(followXmlIds != null) {
            //Check if any user has been clicked
            for (int i = 0; i < followXmlIds.length; i++) {
                if(v.getId() == followXmlIds[i]) { //clicked i'th user xml obj.
                    Profile curProfile = follow[i];
                    Log.d("FollowFragment", "user '" + curProfile.user.getLogin() + "' xml click!");

                    parent.db.resetViews();

                    //1. Check if need to acquire new data.
                    boolean[] mode = new boolean[6];
                    boolean needNewData = false;
                    //1.1 Check if skeleton-filled in user. Need to acquire it again.
                    if(curProfile.user.getName() == null) {
                        mode[0] = true;
                        needNewData = true;
                    } else {
                        parent.db.profileFragment.loadContent(curProfile);
                    }
                    //1.2 Check if need to acquire repos.
                    if(curProfile.repos == null) {
                        mode[1] = true;
                        needNewData = true;
                    } else {
                        parent.db.reposFragment.loadContent(curProfile.repos);
                    }
                    //1.3 Check if need to acquire following.
                    if(curProfile.following == null) {
                        mode[2] = true;
                        needNewData = true;
                    } else {
                        parent.db.followingFragment.loadContent(curProfile.following);
                    }
                    //1.4 Check if need to acquire followers.
                    if(curProfile.followers == null) { //need to acquire followers
                        mode[3] = true;
                        needNewData = true;
                    } else {
                        parent.db.followersFragment.loadContent(curProfile.followers);

                    }
                    mode[4] = false; //don't need notification
                    mode[5] = false; //not searching

                    //2. Show profile fragment page: loading
                    parent.displaySelectedContent(R.id.nav_profile);

                    //3. Start loading & populating the DB asynchronously
                    if(needNewData) {
                        GithubParser.Param param =
                                new GithubParser.Param(parent.db, curProfile.user.getLogin(), mode, parent);
                        parent.db.startDataExtraction(param);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void resetView() {
        follow = null;
        followXmlIds = null;
        if(view != null) { //show "Loading..." tv and delete LinearLayout elements
            TextView loading = view.findViewById(R.id.tv_Users_loading);
            loading.setVisibility(View.VISIBLE);
            LinearLayout llUsers = view.findViewById(R.id.ll_Users);
            llUsers.setVisibility(View.GONE);
            llUsers.removeAllViews();
        }
    }

    /**
     * Load new content into the fragment.
     * @param newFollow New Following or Followers array.
     */
    public void loadContent(Profile[] newFollow) {
        Log.d("followFragment", "load new content");
        resetView();
        follow = newFollow;
        loadNewContent = true;
        loadContent();
    }

    @Override
    protected void _loadContent() {
        Log.d("FollowFragment", "_loadContent");
        if(follow == null) {
            return;
        }
        TextView loading = view.findViewById(R.id.tv_Users_loading);
        LinearLayout llUsers = view.findViewById(R.id.ll_Users);
        Context context = getContext();
        loading.setVisibility(View.GONE);

        if(follow.length == 0) {
            //user has no following
            llUsers.addView(DB.createTv_dne(context));
        } else {
            //Load and set xml repo object for each repo in repos array.
            View curFollowing;
            ImageView curProfilePic;
            TextView curUsername;
            followXmlIds = new int[follow.length];
            for (int i = 0; i < follow.length; i++) {
                curFollowing = View.inflate(context, R.layout.user, null);
                curFollowing.setId(View.generateViewId());
                curFollowing.setOnClickListener(this);
                followXmlIds[i] = curFollowing.getId();
                curProfilePic = curFollowing.findViewById(R.id.iv_User_profilePic);
                curUsername = curFollowing.findViewById(R.id.tv_User_username);
                //Set profile pic, username text, and add to layout.
                curProfilePic.setImageDrawable(follow[i].profilePic);
                curUsername.setText(follow[i].user.getLogin());
                llUsers.addView(curFollowing);
            }
        }
        llUsers.setVisibility(View.VISIBLE);
    }
}
