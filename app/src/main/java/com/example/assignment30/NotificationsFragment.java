package com.example.assignment30;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * NotificationsFragment --- ActivityFragment for displaying GitHub Notifications for start user.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     11/06/2017
 * @last_edit   11/07/2017
 */
public class NotificationsFragment extends ActivityFragment implements View.OnClickListener {
    public DB db;
    private Notification[] notifications;
    private int[] notificationXmlIds = null; //ids of Notification xml obj's holding gui about a given notification.

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Notifications");
        if (notifications != null) {
            loadNewContent = true; //need to re-render the content
        }
        loadContent(); //render new content if exists
        Log.d("NotificationsFragment", "onViewCreated");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_notifications, container, false);
        Button btnUpdate = view.findViewById(R.id.btn_Notifications_Update);
        btnUpdate.setOnClickListener(this);
        Log.d("NotificationsFragment", "onCreateView");
        return view;
    }

    @Override
    public void onClick(View v) {
        if(notificationXmlIds != null) {
            //Check if any notification has been clicked
            for (int i = 0; i < notificationXmlIds.length; i++) {
                if(v.getId() == notificationXmlIds[i]) { //clicked i'th notification xml obj.
                    Log.d("NotificationsFragment", "notification " + notificationXmlIds[i] + " xml click!");
                    if(notifications[i].unread) {
                        notifications[i].unread = false;
                        setNotificationType(i);
                        //TODO use API to set Notification read!
                    }
                    break;
                }
            }
        }
        if (v.getId() == R.id.btn_Notifications_Update) { //update/refresh Notifications page
            Log.d("NotificationsFragment", "refresh clicked!");
            resetView();
            GithubParser.Param param = new GithubParser.Param(db, "",
                    new boolean[] {false, false, false, false, true, false}); //only notifications
            db.startDataExtraction(param);
        }
    }

    @Override
    public void resetView() {
        notifications = null;
        notificationXmlIds = null;
        if(view != null) { //show "Loading..." tv and delete LinearLayout elements
            TextView loading = view.findViewById(R.id.tv_Notifications_loading);
            loading.setVisibility(View.VISIBLE);
            LinearLayout llNotifications = view.findViewById(R.id.ll_Notifications);
            llNotifications.setVisibility(View.GONE);
            llNotifications.removeAllViews();
        }
    }

    /**
     * Update UI elements to load in new Profile
     * @param newNotifications Profile to load UI from.
     */
    public void loadContent(Notification[] newNotifications) {
        Log.d("NotificationsFragment", "load new content");
        notifications = newNotifications;
        loadNewContent = true;
        loadContent();
    }

    /**
     * If in view, update UI elements to load in new content.
     */
    @Override
    protected void _loadContent() {
        Log.d("NotificationsFragment", "_loadContent");
        if(notifications == null) {
            return;
        }
        TextView loading = view.findViewById(R.id.tv_Notifications_loading);
        LinearLayout llNotifications = view.findViewById(R.id.ll_Notifications);
        Context context = getContext();
        loading.setVisibility(View.GONE);

        if(notifications.length == 0) {
            //user has no notifications
            llNotifications.addView(DB.createTv_dne(context));
        } else {
            //Load and set xml repo object for each repo in repos array.
            View curNotification;
            TextView curType;
            TextView curTitle;
            TextView curDesc;
            TextView curReason;
            String reason;
            notificationXmlIds = new int[notifications.length];
            for (int i = 0; i < notifications.length; i++) {
                curNotification = View.inflate(context, R.layout.notification, null);
                curNotification.setId(View.generateViewId());
                curNotification.setOnClickListener(this);
                notificationXmlIds[i] = curNotification.getId();
                //Set children views
                curType = curNotification.findViewById(R.id.tv_Notification_type);
                curTitle = curNotification.findViewById(R.id.tv_Notification_title);
                curReason = curNotification.findViewById(R.id.tv_Notification_reason);
                curDesc = curNotification.findViewById(R.id.tv_Notification_desc);
                //Set content in format [reason][desc], and add to layout.
                setNotificationType(curNotification, curType, i);
                curTitle.setText(notifications[i].subject.title);
                curDesc.setText(notifications[i].getDescription());
                curReason.setText(Notification.getReason.get(notifications[i].reason));
                llNotifications.addView(curNotification);
            }
        }
        llNotifications.setVisibility(View.VISIBLE);
    }

    /**
     * Set Notification Type field and border depending on if it's unread or not.
     * @param index Index in notifications array of Notification referring to.
     */
    protected void setNotificationType(int index) {
        View notification = view.findViewById(notificationXmlIds[index]);
        TextView type = notification.findViewById(R.id.tv_Notification_type);
        setNotificationType(notification, type, index);
    }

    /**
     * Set Notification Type field and border depending on if it's unread or not.
     * @param notification View repr. notification xml element.
     * @param notification_type TextView repr. (notification)'s Type element.
     * @param index Index in notifications array of Notification referring to.
     */
    protected void setNotificationType(View notification, TextView notification_type, int index) {
        if(notifications[index].unread) {
            notification.setBackgroundResource(R.drawable.notification_border_unread);
        } else {
            notification.setBackgroundResource(R.drawable.notification_border_read);
        }
        notification_type.setText(notifications[index].getType());
    }

}
