package com.example.assignment30;

import org.eclipse.egit.github.core.Repository;

import java.util.HashMap;

/**
 * Notification --- Class representing a GitHub API-returned Notification element.
 *
 * @author      Scott Wolfskill, wolfski2
 * @created     11/06/2017
 * @last_edit   11/06/2017
 */
public class Notification {
    public static HashMap<String, String> getReason = null; //map API reason to human-readable reason for Notifying.
    public String id;
    public Repository repository;
    public Subject subject;
    public String reason;
    public boolean unread;
    public String updated_at;
    public String last_read_at;
    public String url;


    public class Subject {
        public String title;
        public String url;
        public String latest_comment_url;
        public String type;

        public Subject(String title, String url, String latest_comment_url, String type) {
            this.title = title;
            this.url = url;
            this.latest_comment_url = latest_comment_url;
            this.type = type;
        }

        @Override
        public String toString() {
            return "Subject{\r\n" +
                    "\ttitle='" + title + '\'' +
                    ",\r\n\turl='" + url + '\'' +
                    ",\r\n\tlatest_comment_url='" + latest_comment_url + '\'' +
                    ",\r\n\ttype='" + type + '\'' +
                    "\r\n}";
        }
    }

    public static void setGetReason() {
        getReason = new HashMap<>(9);
        getReason.put("assign", "You were assigned to this Issue.");
        getReason.put("author", "You created this thread.");
        getReason.put("comment", "You commented on this thread.");
        getReason.put("invitation", "You accepted an invitation to contribute to this repository.");
        getReason.put("manual", "You subscribed to this thread (via an Issue or Pull Request).");
        getReason.put("mention", "You were specifically @mentioned in the content.");
        getReason.put("state_change", "You changed the thread state.");
        getReason.put("subscribed", "You're watching this repository.");
        getReason.put("team_mention", "You're on a team that was mentioned.");
    }

    public Notification() {}

    /**
     * Get Type for this Notification, which depends on if it's unread or not.
     * @return String type
     */
    public String getType() {
        String type = "";
        if(unread) {
            type += "New ";
        }
        type += subject.type + ": ";
        return type;
    }

    /**
     * Get description string for this Notification.
     * @return Stirng description
     */
    public String getDescription() {
        return repository.getOwner().getLogin() + "/" + repository.getName();
    }

    @Override
    public String toString() {
        return "Notification{" +
                "\r\n\tid='" + id + '\'' +
                ",\r\n\trepository=" + repository.toString() +
                ",\r\n\tsubject=" + subject.toString() +
                ",\r\n\treason='" + reason + '\'' +
                ",\r\n\tunread=" + unread +
                ",\r\n\tupdated_at='" + updated_at + '\'' +
                ",\r\n\tlast_read_at='" + last_read_at + '\'' +
                ",\r\n\turl='" + url + '\'' +
                "\r\n}";
    }
}
