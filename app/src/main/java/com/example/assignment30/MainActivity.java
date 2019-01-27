package com.example.assignment30;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import static com.example.assignment30.GithubParser.LoadLoginInfo;

/**
 * MainActivity --- Main entry point in the program that also handles nav_bar navigation.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     10/23/2017
 * @last_edit   01/26/2019
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public DB db;
    private boolean shownFragments = false; //if true, have shown a fragment
    private final String startUser = "smwolfskill"; //will default to this GitHub profile if loading login info fails
    private String oldUsersQuery = ""; //previous search query for users.
    private String oldReposQuery = ""; //previous search query for repos.

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
        //2. Load GitHub user/token info if present
        boolean loadSuccess = GithubParser.LoadLoginInfo();
        if(!loadSuccess) {
            //TODO: Show some popup message and give location of login file
        }
        //3. Init. DB and activities fragments
        db = new DB();
        initFragments();
        Notification.setGetReason();

        //4. Start loading & populating the DB asynchronously
        boolean[] mode = new boolean[] {true, true, true, true, true, false}; //get all 4: profile, repos, following, followers
        GithubParser.Param param = new GithubParser.Param(db, startUser, mode);
        db.startDataExtraction(param);

        //5. Show content with default page
        displaySelectedContent(R.id.nav_profile);   // set default content as Profile page
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
                                    new boolean[] {false, false, true, false, false, true});
                            db.startDataExtraction(param);
                        }
                        displaySelectedContent(R.id.rad_users);
                        oldUsersQuery = query;
                    } else { //search for repos
                        Log.d("btn_click", "search for repos matching '" + query + "'");
                        if(!oldReposQuery.equals(query)) { //only do API request if new repos query
                            //All false except repos, and searchMode
                            db.searchReposFragment.resetView();
                            GithubParser.Param param = new GithubParser.Param(db, query,
                                    new boolean[]{false, true, false, false, false, true});
                            db.startDataExtraction(param);
                        }
                        displaySelectedContent(R.id.rad_repos);
                        oldReposQuery = query;
                    }
                }
                break;
        }
    }

    /**
     * Initialize the four page fragments. They will each have data loaded on them asynchronously.
     */
    private void initFragments() {
        db.profileFragment = new ProfileFragment();
        db.reposFragment = new ReposFragment();
        db.reposFragment.pageTitle = "Public Repos";
        db.followingFragment = new FollowFragment();
        db.followersFragment = new FollowFragment();
        db.followingFragment.setFields(this, "Following");
        db.followersFragment.setFields(this, "Followers");
        db.notificationsFragment = new NotificationsFragment();
        db.notificationsFragment.db = db;
        db.searchUsersFragment = new FollowFragment();
        db.searchUsersFragment.setFields(this, "Search Users");
        db.searchReposFragment = new ReposFragment();
        db.searchReposFragment.pageTitle = "Search Repos";
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
