package co.nemiz.domain;

import java.io.Serializable;

public class Device implements Serializable {
    private static final long serialVersionUID = 7914723814094875345L;

    private String name, type, token;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
