package co.nemiz.services;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import co.nemiz.domain.Activity;
import co.nemiz.domain.AudioDefinition;
import co.nemiz.domain.Client;
import co.nemiz.domain.Device;
import co.nemiz.domain.Register;
import co.nemiz.domain.User;

public class DefaultRestService {
    private static final String TAG = DefaultRestService.class.getSimpleName();

    private static final String BASE_URL = "http://54.93.34.31";
    private static final String AUDIO_URL = "http://a.nemiz.co";

    private static final String PASSWORD_GRANT_CLIENT_ID = "2_6644h3uhl2scog4088kwso8kgscgg4g4w4wwssowgkc84g8ggw";

    private static DefaultRestService singletonInstance;
    private static AsyncHttpClient asyncHttpClient;
    private static SyncHttpClient syncHttpClient;
    private static Gson gson;
    private static String accessToken;

    public static DefaultRestService get() {
        if (singletonInstance == null) {
            singletonInstance = new DefaultRestService();
        }
        return singletonInstance;
    }

    public static void setAccessToken(String accessToken) {
        DefaultRestService.accessToken = accessToken;
    }

    public static boolean hasAccessToken() {
        return accessToken != null && !accessToken.isEmpty();
    }

    public static String absoluteUrl(String relativeUrl) {
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl;
        }
        return BASE_URL + relativeUrl;
    }

    public DefaultRestService() {
        asyncHttpClient = new AsyncHttpClient();
        syncHttpClient = new SyncHttpClient();

        GsonBuilder builder = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        gson = builder.create();
    }

    public static RequestHandle get(String path, RequestParams params, ResponseHandlerInterface responseHandlerInterface) {
        asyncHttpClient.addHeader("Content-Type", "application/json; charset=utf-8");
        if (hasAccessToken()) {
            asyncHttpClient.addHeader("Authorization", "Bearer " + accessToken);
        }
        return asyncHttpClient.get(absoluteUrl(path), params, responseHandlerInterface);
    }

    public static <T> ResponseHandlerInterface defaultResponseHandler(final Class<T> clazz, final Type type, final Result<T> result) {
        return new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                result.handle(statusCode, null, responseString);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                T retValue = (T) gson.fromJson(responseString, clazz != null ? clazz : type);
                result.handle(statusCode, retValue, responseString);
            }
        };
    }

    public static <T> RequestHandle get(String path, RequestParams params, Class<T> clazz, Result<T> resultHandler) {
        return get(path, params, defaultResponseHandler(clazz, null, resultHandler));
    }

    public static <T> RequestHandle get(String path, RequestParams params, Type type, Result<T> resultHandler) {
        return get(path, params, defaultResponseHandler(null, type, resultHandler));
    }

    public static RequestHandle post(String path, Object requestBody, ResponseHandlerInterface responseHandlerInterface) {
        return internalPost(asyncHttpClient, path, requestBody, responseHandlerInterface);
    }

    public static RequestHandle syncPost(String path, Object requestBody, ResponseHandlerInterface responseHandlerInterface) {
        return internalPost(syncHttpClient, path, requestBody, responseHandlerInterface);
    }

    private static RequestHandle internalPost(AsyncHttpClient client,
            String path, Object requestBody, ResponseHandlerInterface responseHandlerInterface) {

        client.addHeader("Content-Type", "application/json; charset=utf-8");
        if (hasAccessToken()) {
            client.addHeader("Authorization", "Bearer " + accessToken);
        }
        try {
            return client.post(null, absoluteUrl(path),
                    new StringEntity(gson.toJson(requestBody), HTTP.UTF_8),

                    "application/json", responseHandlerInterface);
        } catch (UnsupportedEncodingException e) {
            responseHandlerInterface.sendFailureMessage(500, null, null, e);
        }
        return new RequestHandle(null);
    }

    public static <T> RequestHandle post(String path, Object requestBody, Class<T> clazz, Result<T> resultHandler) {
        return post(path, requestBody, defaultResponseHandler(clazz, null, resultHandler));
    }

    public static interface Result<T> {
        void handle(int statusCode, T result, String stringResult);
    }

    public static class BaseResult<T> implements Result<T> {
        @Override
        public void handle(int statusCode, T result, String stringResult) {
        }
    }

    // -------------------------------------------------------------------------------------------------------------------

    public void register(Register registration, Result<Client> resultHandler) {
        post("/register", registration, Client.class, resultHandler);
    }

    public void login(String username, String password, final Result<String> resultHandler) {
        RequestParams params = new RequestParams();
        params.add("grant_type", "password");
        params.add("client_id", PASSWORD_GRANT_CLIENT_ID);
        params.add("client_secret", "");
        params.add("username", username);
        params.add("password", password);

        get("/oauth/v2/token" , params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    resultHandler.handle(statusCode, response.getString("access_token"), "");
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to get access token", e);
                    throw new IllegalStateException(e);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                resultHandler.handle(statusCode, null, responseString);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                resultHandler.handle(statusCode, null, null);
            }
        });
    }

    public void createDevice(Device device, Result<Client> resultHandler) {
        post("/api/devices", device, Client.class, resultHandler);
    }

    // -------------------------------------------------------------------------------------------------------------------
    // CONTACTS

    private List<User> contactsList;

    public RequestHandle getContacts(final String filter, final Result<List<User>> resultHandler) {
        if (contactsList == null) {
            Type type = new TypeToken<ArrayList<User>>() {}.getType();
            return get("/api/friends", null, type, new Result<List<User>>() {
                @Override
                public void handle(int statusCode, List<User> result, String stringResult) {
                    if (result != null) {
                        contactsList = result;
                        resultHandler.handle(
                            statusCode, applyUserFilter(filter), stringResult);
                    } else {
                        resultHandler.handle(statusCode, result, stringResult);
                    }
                }
            });
        } else {
            resultHandler.handle(200, applyUserFilter(filter), null);
            return new RequestHandle(null);
        }
    }

    public void clearContacts() {
        contactsList = null;
    }

    public void pokeSync(User friend) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("friend", friend.getId());

        syncPost("/api/friends", params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
            }
        });
    }

    private List<User> applyUserFilter(String filter) {
        List<User> filterList = new ArrayList<User>();
        if (filter != null && !filter.isEmpty()) {
            for (User contact : contactsList) {
                if (contact.getName().toLowerCase().contains(filter.toLowerCase())) {
                    filterList.add(contact);
                }
            }
        } else {
            filterList = contactsList;
        }
        return filterList;
    }


    // -------------------------------------------------------------------------------------------------------------------
    // ACTIVITY

    private List<Activity> activityList;
    private long activityLastOffset = 0L;

    public void getActivity(Long offset, final Result<List<Activity>> resultHandler) {
        if (activityList == null || activityLastOffset != offset) {
            Type type = new TypeToken<ArrayList<Activity>>() {}.getType();

            RequestParams params = new RequestParams();
            params.add("offset", String.valueOf(offset));
            activityLastOffset = offset;

            get("/api/activity", params, type, new Result<List<Activity>>() {
                @Override
                public void handle(int statusCode, List<Activity> result, String stringResult) {
                    if (result != null) {
                        if (activityList == null) {
                            activityList = result;
                        } else {
                            activityList.addAll(result);
                        }
                        resultHandler.handle(statusCode, activityList, stringResult);
                    } else {
                        resultHandler.handle(statusCode, result, stringResult);
                    }
                }
            });
        } else {
            resultHandler.handle(200, activityList, null);
        }
    }

    public void clearActivity() {
        this.activityLastOffset = 0;
        this.activityList = null;
    }

    // -------------------------------------------------------------------------------------------------------------------
    // Retrieve Audio defintiions

    public RequestHandle getAudioDefinition(Result<AudioDefinition> resultHandler) {
        return get(AUDIO_URL, null, AudioDefinition.class, resultHandler);
    }
}
