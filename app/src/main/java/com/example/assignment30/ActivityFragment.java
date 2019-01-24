package com.example.assignment30;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

/**
 * ActivityFragment --- Abstract class holding general information and methods used
 *                      by all derived Activity fragment classes.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     10/26/2017
 * @last_edit   11/07/2017
 */
public abstract class ActivityFragment extends Fragment {
    public View view = null;
    protected boolean loadNewContent = false; //if true, new content exists but not yet loaded into this fragment

    /**
     * If possible and there exists new content, load it.
     */
    public void loadContent() {
        //1. Check if Fragment not used yet. If so, mark that next time it's used, need to load content.
        if(view == null) {
            loadNewContent = true;
            return;
        }
        //2. If there's new content to load and window is rendered, load it.
        if(loadNewContent && getContext() != null) {
            Log.d("loadContent", "loading new content");
            loadNewContent = false;
            _loadContent();
        }
    }

    /**
     * Loading to be implemented by derived class.
     */
    protected abstract void _loadContent();

    /**
     * Resetting of the view to the default, which will be implemented by derived class.
     */
    public abstract void resetView();
}
