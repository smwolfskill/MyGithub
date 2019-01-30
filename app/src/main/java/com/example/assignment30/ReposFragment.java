package com.example.assignment30;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eclipse.egit.github.core.Repository;

import static android.view.View.GONE;

/**
 * ReposFragment --- ActivityFragment for displaying GitHub Repository information.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     10/23/2017
 * @last_edit   01/29/2019
 */
public class ReposFragment extends ActivityFragment implements View.OnClickListener {
    public String pageTitle = null;
    //private View defaultView = null; //default view (loading view) of the fragment.
    private Repository[] repos = null;
    private int[] repoXmlIds = null; //ids of Repo xml obj's holding gui about a given repo.

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(pageTitle);
        if(repos != null) {
            loadNewContent = true; //need to re-render the content
        }
        loadContent(); //render new content if exists
        Log.d("ReposFragment", "onViewCreated");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_repos, container, false);
        Log.d("ReposFragment", "onCreateView");
        return view;
    }

    @Override
    public void onClick(View v) {
        if(repoXmlIds != null) {
            //Check if any repo has been clicked
            for (int i = 0; i < repoXmlIds.length; i++) {
                if(v.getId() == repoXmlIds[i]) { //clicked i'th repo xml obj.
                    String repoUrl = repos[i].getHtmlUrl();
                    Log.d("ReposFragment", "repo " + repoXmlIds[i] + " xml click!");
                    Log.d("ReposFragment", "     '" + repoUrl + "'");
                    //TODO open webpage! This always crashes VVV
                    /*Intent intent= new Intent(Intent.ACTION_VIEW, Uri.parse(repoUrl));
                    startActivity(intent);*/
                    break;
                }
            }
        }
    }

    @Override
    public void resetView() {
        repos = null;
        repoXmlIds = null;
        if(view != null) { //show "Loading..." tv and delete LinearLayout elements
            TextView loading = view.findViewById(R.id.tv_Repos_loading);
            loading.setVisibility(View.VISIBLE);
            LinearLayout llRepos = view.findViewById(R.id.ll_Repos);
            llRepos.setVisibility(View.GONE);
            llRepos.removeAllViews();
        }
    }

    /**
     * Gets the owner username ('login') of the first repository, if any.
     * @return owner username, or null if no repos loaded.
     */
    public String GetReposOwner() {
        if(repos == null || repos.length == 0) {
            return null;
        } else {
            return repos[0].getOwner().getLogin();
        }
    }

    public void loadContent(Repository[] newRepos) {
        Log.d("reposFragment", "load new content");
        resetView();
        repos = newRepos;
        loadNewContent = true;
        loadContent();
    }

    @Override
    protected void _loadContent() {
        Log.d("ReposFragment", "_loadContent");
        if(repos == null) {
            return;
        }
        TextView loading = view.findViewById(R.id.tv_Repos_loading);
        LinearLayout llRepos = view.findViewById(R.id.ll_Repos);
        Context context = getContext();
        loading.setVisibility(View.GONE);

        if(repos.length == 0) {
            //user has no repos
            llRepos.addView(DB.createTv_dne(context));
        } else {
            //Load and set xml repo object for each repo in repos array.
            View curRepo;
            TextView curTitle;
            TextView curDesc;
            repoXmlIds = new int[repos.length];
            for (int i = 0; i < repos.length; i++) {
                curRepo = View.inflate(context, R.layout.repo, null);
                curRepo.setId(View.generateViewId());
                curRepo.setOnClickListener(this);
                repoXmlIds[i] = curRepo.getId();
                curTitle = curRepo.findViewById(R.id.tv_Repo_title);
                curDesc = curRepo.findViewById(R.id.tv_Repo_desc);
                //Set title in format [username]/[repo], description, and add to layout.
                curTitle.setText(repos[i].getOwner().getLogin() + "/" + repos[i].getName());
                curDesc.setText(repos[i].getDescription());
                llRepos.addView(curRepo);
            }
        }
        llRepos.setVisibility(View.VISIBLE);
    }

}
