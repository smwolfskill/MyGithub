package com.example.assignment30;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;

/**
 * MainActivity --- Main entry point in the program that also handles nav_bar navigation.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     10/23/2017
 * @last_edit   02/05/2019
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public DB db;
    public Handler handler;
    public static final int HANDLER_POPUP = 1; //signal to MainActivity to show popup message
    private boolean shownFragments = false; //if true, have shown a fragment
    private String oldUsersQuery = ""; //previous search query for users.
    private String oldReposQuery = ""; //previous search query for repos.
    private final String loadLoginInfoFailed = "Loading user login info was unsuccessful. Showing default profile.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //1. Draw navbar UI elements
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //2. Make event handler, and load GitHub user/token info if present
        handler = makeHandler();
        boolean loadLoginInfoSuccess = GithubParser.LoadLoginInfo(this);

        //3. Init. DB and activities fragments
        int profilePicWidth_small = (int) getResources().getDimension(R.dimen.profilePic_width_small);
        int profilePicHeight_small = (int) getResources().getDimension(R.dimen.profilePic_height_small);
        Log.d("MainActivity", "profile image size will be compressed to at minimum " + profilePicWidth_small + " x " + profilePicHeight_small);
        db = new DB(profilePicWidth_small, profilePicHeight_small); //set to always obtain smallest profile pic size
        db.initFragments(this);

        //4. Start loading & populating the DB asynchronously: extract login profile in separate requests
        db.ExtractLoginProfile_full(true, true,this);

        //5. Show content with default page
        displaySelectedContent(R.id.nav_profile);   // set default content as Profile page

        //6. If failed to load user login info, show popup after a delay.
        if(!loadLoginInfoSuccess) {
            ShowPopup(loadLoginInfoFailed, 1000);
        }
    }

    /**
     * Construct a handler for this activity to receive messages on (to show popup messages).
     * @return Handler constructed.
     */
    @NonNull
    private Handler makeHandler() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                switch(message.what) {
                    case HANDLER_POPUP: //show popup for each message in errorBundle
                        Bundle errorBundle = message.getData();
                        int count = 0;
                        char[] errMsg;
                        while(true) {
                            errMsg = errorBundle.getCharArray(String.valueOf(count));
                            if(errMsg != null) {
                                Log.d("MainActivityHandler", String.valueOf(count) + ": '" + String.valueOf(errMsg) + "'");
                                ShowPopup(String.valueOf(errMsg), message.arg1);
                                count++;
                            } else {
                                break;
                            }
                        }
                        break;
                    default: Log.e("MainActivityHandler" ,message.what + " is invalid command code.");
                }
            }
        };
    }

    /**
     * Show popup message notifying user that loading their GitHub login info has failed.
     * @param delayMillis Delay in milliseconds before showing the popup.
     */
    public void ShowPopup(String message, int delayMillis) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Snackbar loadFailed = Snackbar.make(
                        findViewById(android.R.id.content),
                        message,
                        Snackbar.LENGTH_LONG);
                loadFailed.show();
            }
        }, delayMillis);
    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch(id) {
            case R.id.nav_profile: //load user (or default) profile fully, w/ separate requests per data type
                db.ExtractLoginProfile_full(false, true, this);
                break;
            case R.id.nav_repos: //load user (or default) repositories
                db.ExtractLoginRepos(this);
                break;
            case R.id.nav_following: //load user (or default) following
                db.ExtractLoginFollowing(this);
                break;
            case R.id.nav_followers: //load user (or default) followers
                db.ExtractLoginFollowers(this);
                break;
        }
        displaySelectedContent(id);
        return true;
    }

    public void btn_click(View v) {
        switch(v.getId()) {
            case R.id.btn_Profile_Repos: //show Repos page
                displaySelectedContent(R.id.nav_repos);
                break;
            case R.id.btn_Profile_Following: //show Following page
                displaySelectedContent(R.id.nav_following);
                break;
            case R.id.btn_Profile_Followers: //show Followers page
                displaySelectedContent(R.id.nav_followers);
                break;
            case R.id.btnSearch:
                EditText et_search = (EditText) findViewById(R.id.et_search);
                String query = et_search.getText().toString();
                if (!query.equals("")) { //perform search!
                    RadioButton rad_users = (RadioButton) findViewById(R.id.rad_users);
                    if(rad_users.isChecked()) { //search for users
                        Log.d("btn_click", "search for users matching '" + query + "'");
                        if(!oldUsersQuery.equals(query)) { //only do API request if new users query
                            //All false except following (here meaning users), and searchMode
                            db.searchUsersFragment.resetView();
                            GithubParser.Param param = new GithubParser.Param(db, query,
                                    null, db.profileImage_width, db.profileImage_height, this);
                            param.setMode_SearchUsers();
                            db.startDataExtraction(param, false);
                        }
                        displaySelectedContent(R.id.rad_users);
                        oldUsersQuery = query;
                    } else { //search for repos
                        Log.d("btn_click", "search for repos matching '" + query + "'");
                        if(!oldReposQuery.equals(query)) { //only do API request if new repos query
                            //All false except repos, and searchMode
                            db.searchReposFragment.resetView();
                            GithubParser.Param param = new GithubParser.Param(db, query,
                                    null, db.profileImage_width, db.profileImage_height, this);
                            param.setMode_SearchRepos();
                            db.startDataExtraction(param, false);
                        }
                        displaySelectedContent(R.id.rad_repos);
                        oldReposQuery = query;
                    }
                }
                break;
        }
    }

    /**
     * Display a specified XML Layout.
     * (Demo from Simplified Coding video @ youtube.com/watch?v=-SUvA1fXaKw)
     * @param id id of XML Layout to display
     */
    public void displaySelectedContent(int id) {
        ActivityFragment fragment = null;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch(id) {
            case R.id.nav_profile:
                fragment = db.profileFragment;
                break;
            case R.id.nav_repos:
                fragment = db.reposFragment;
                break;
            case R.id.nav_following:
                fragment = db.followingFragment;
                break;
            case R.id.nav_followers:
                fragment = db.followersFragment;
                break;
            case R.id.nav_notifications:
                fragment = db.notificationsFragment;
                break;
            case R.id.rad_users: //users search results
                fragment = db.searchUsersFragment;
                break;
            case R.id.rad_repos: //repos search results
                fragment = db.searchReposFragment;
                break;
        }

        if(fragment != null) {
            ft.replace(R.id.cl_main, fragment);
            if(shownFragments) { //if not 1st fragment, add prev one to back stack.
                ft.addToBackStack(null);
            }
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        shownFragments = true;
    }

}
