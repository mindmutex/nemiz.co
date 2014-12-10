package co.nemiz.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Register implements Serializable {
    private static final long serialVersionUID = 5811790819206000914L;

    private User user;

    private Device device;

    private List<String> friends = new ArrayList<String>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public List<String> getFriends() {
        return friends;
    }

    public void setFriends(List<String> friends) {
        this.friends = friends;
    }
}
