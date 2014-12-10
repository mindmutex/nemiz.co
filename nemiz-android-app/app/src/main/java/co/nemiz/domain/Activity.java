package co.nemiz.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by ivarsv on 12/6/14.
 */
public class Activity {
    private Long id;
    private User user, friend;

    @SerializedName("date_created")
    private Date dateCreated;

    private boolean received;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}
