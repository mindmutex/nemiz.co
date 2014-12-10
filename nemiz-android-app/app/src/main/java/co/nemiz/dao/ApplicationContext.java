package co.nemiz.dao;

import android.content.SharedPreferences;

public class ApplicationContext {
    public static final String KEY_GCM_ID = "GcmRegistrationId";

    public static ApplicationContext factory(SharedPreferences preferences) {
        ApplicationContext nemizContext = new ApplicationContext();
        nemizContext.setPreferences(preferences);

        return nemizContext;
    }
    private static ApplicationContext contextHolder;

    public static void init(SharedPreferences preferences) {
        contextHolder = factory(preferences);
    }

    public static ApplicationContext get() {
        if (contextHolder == null) {
            throw new IllegalStateException();
        }
        return contextHolder;
    }

    private SharedPreferences preferences;

    public void setPreferences(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public String getPushRegistrationId() {
        return preferences.getString(KEY_GCM_ID, "");
    }

    public void setPushRegistrationId(String key) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_GCM_ID, key);
        editor.commit();
    }
}
