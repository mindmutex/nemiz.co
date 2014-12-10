package co.nemiz.domain;

import java.io.Serializable;

public class Client implements Serializable {
    private static final long serialVersionUID = 6998129482822143052L;

    private String userId, clientId, clientSecret;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
