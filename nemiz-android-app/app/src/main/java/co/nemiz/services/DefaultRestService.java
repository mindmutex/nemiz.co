package co.nemiz.services;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
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

import co.nemiz.domain.Activity;
import co.nemiz.domain.AudioDefinition;
import co.nemiz.domain.Client;
import co.nemiz.domain.Device;
import co.nemiz.domain.Register;
import co.nemiz.domain.User;

public class DefaultRestService {
    private static final String TAG = DefaultRestService.class.getSimpleName();

    /**
     * API endpoint URL.
     */
    private static final String BASE_URL = "http://54.93.36.184";

    /**
     * URL where to find audio fles.
     */
    private static final String AUDIO_URL = "http://a.nemiz.co";

    /**
     * Default client id to use to obtain access token using password grant type.
     */
    private static final String PASSWORD_GRANT_CLIENT_ID = "2_6644h3uhl2scog4088kwso8kgscgg4g4w4wwssowgkc84g8ggw";

    private static DefaultRestService singletonInstance;
    private static TokenRequestListener tokenRequestListener;
    private static Gson gson;

    private static AsyncHttpClient asyncHttpClient;
    private static SyncHttpClient syncHttpClient;

    /**
     * Currently used access token to get or post data to API server.
     * Once token is expired it is set to NULL
     */
    private static String accessToken;

    public static DefaultRestService get() {
        if (singletonInstance == null) {
            singletonInstance = new DefaultRestService();
        }
        return singletonInstance;
    }

    /**
     * Access token is used by all requests.
     *
     * @param accessToken access token
     */
    public static void setAccessToken(String accessToken) {
        DefaultRestService.accessToken = accessToken;
    }

    /**
     * Check if access token is defined.
     *
     * @return status
     */
    public static boolean hasAccessToken() {
        return accessToken != null && !accessToken.isEmpty();
    }

    /**
     * Returns absolute URL unless starts with http(s)://.
     * The relative URL is a concatenation of BASE_URL and the given relative URL.
     *
     * @param relativeUrl relative url
     * @return absolute url
     */
    public static String absoluteUrl(String relativeUrl) {
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl;
        }
        return BASE_URL + relativeUrl;
    }

    /**
     * When no access token is available use this listener to obtain a new token.
     *
     * @param tokenRequestListener token request listener
     */
    public static void setTokenRequestListener(TokenRequestListener tokenRequestListener) {
        DefaultRestService.tokenRequestListener = tokenRequestListener;
    }

    /**
     * Cached contact list.
     */
    private List<User> contactsList;

    /**
     * Cached activity list.
     */
    private List<Activity> activityList;

    /**
     * Activity list last queried offset.
     */
    private long activityLastOffset = 0L;


    public DefaultRestService() {
        asyncHttpClient = new AsyncHttpClient();
        syncHttpClient = new SyncHttpClient();

        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        gson = builder.create();
    }

    public static RequestHandle get(String path, RequestParams params, ResponseHandlerInterface responseHandlerInterface) {
        asyncHttpClient.addHeader("Content-Type", "application/json; charset=utf-8");
        if (hasAccessToken()) {
            asyncHttpClient.addHeader("Authorization", "Bearer " + accessToken);
        }
        return asyncHttpClient.get(absoluteUrl(path), params, responseHandlerInterface);
    }

    /**
     * Converts response to JSON and invokes the result handler.
     *
     * On 401 failure clears the access token and uses token request listener to obtain a new token.
     * The result handler must implement retry method to handle such requests.
     *
     * @param clazz return type as class (must define class or type)
     * @param type  return type as type instance (must define class or type)
     * @param resultHandler result handler
     * @param <T> type
     *
     * @return interface
     */
    public static <T> AsyncHttpResponseHandler defaultResponseHandler(final Class<T> clazz, final Type type, final Result<T> resultHandler) {
        return new TextHttpResponseHandler() {
            private int retries = 0;

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                if (statusCode == 401) {
                    setAccessToken(null);
                }
                if (tokenRequestListener != null && !hasAccessToken()) {
                    final ResponseHandlerInterface responseHandler = this;
                    tokenRequestListener.onTokenRequest(new BaseResult<String>() {
                        @Override
                        public void handle(int statusCode, String result, String stringResult) {
                            DefaultRestService.setAccessToken(result);
                            responseHandler.sendRetryMessage(++retries);
                        }
                    });
                } else {
                    resultHandler.handle(statusCode, null, responseString);
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                T retValue = (T) gson.fromJson(responseString, clazz != null ? clazz : type);
                resultHandler.handle(statusCode, retValue, responseString);
            }

            @Override
            public void onRetry(int retryNo) {
                resultHandler.retry(retryNo);
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

    public static <T> RequestHandle post(String path, Object requestBody, Class<T> clazz, Result<T> resultHandler) {
        return post(path, requestBody, defaultResponseHandler(clazz, null, resultHandler));
    }

    public static RequestHandle syncPost(String path, Object requestBody, ResponseHandlerInterface responseHandlerInterface) {
        return internalPost(syncHttpClient, path, requestBody, responseHandlerInterface);
    }
    public static <T> RequestHandle syncPost(String path, Object requestBody, Class<T> clazz, Result<T> resultHandler) {
        return internalPost(syncHttpClient, path, requestBody, defaultResponseHandler(clazz, null, resultHandler));
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

    /**
     * Interface for hanlding response from server.
     * Automatically does the conversion from json string to POJO.
     *
     * @param <T> response type.
     */
    public static interface Result<T> {
        void handle(int statusCode, T result, String stringResult);
        void retry(int attempt);
    }

    /**
     * Invokes listener when new token must be obtained.
     * Used when the current token is expired.
     */
    public static interface TokenRequestListener {
        void onTokenRequest(Result<String> result);
    }

    /**
     * Basic result handler that does nothing unless overrided.
     *
     * @param <T> type
     */
    public static class BaseResult<T> implements Result<T> {
        @Override
        public void handle(int statusCode, T result, String stringResult) {
        }

        @Override
        public void retry(int attempt) {
            // by default return dummy internal server error
            handle(500, null, null);
        }
    }

    /**
     * Sends registration request to API server.
     *
     * @param registration reqistration
     * @param resultHandler result handler
     */
    public void register(Register registration, Result<Client> resultHandler) {
        post("/register", registration, Client.class, resultHandler);
    }

    /**
     * Uses password grant type to obtain an access token.
     * PASSWORD_GRANT_CLIENT_ID must be valid.
     *
     * @param username username
     * @param password password
     * @param resultHandler result handler
     */
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

    /**
     * Creates a new device.
     *
     * @param device device
     * @param resultHandler result handler
     */
    public void createDevice(Device device, Result<Client> resultHandler) {
        post("/api/devices", device, Client.class, resultHandler);
    }

    /**
     * Retrieves contact list.
     *
     * @param filter filters contact list by the user name.
     * @param resultHandler result handler
     *
     * @return request handle
     */
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
                        resultHandler.handle(statusCode, null, stringResult);
                    }
                }

                @Override
                public void retry(int attempt) {
                    resultHandler.retry(attempt);
                }
            });
        } else {
            resultHandler.handle(200, applyUserFilter(filter), null);
            return new RequestHandle(null);
        }
    }

    /**
     * Applies name filtering to cached contacts list.
     *
     * @param filter name filter
     * @return filtered array
     */
    private List<User> applyUserFilter(String filter) {
        List<User> filterList = new ArrayList<>();
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

    /**
     * Clears cached contacts.
     */
    public void clearContacts() {
        contactsList = null;
    }

    /**
     * Synchronous request to poke a user i.e. sends the notification.
     *
     * @param friend friend
     */
    public void pokeSync(final User friend) {
        final Map<String, Object> params = new HashMap<>();
        params.put("friend", friend.getId());

        syncPost("/api/friends", params, Object.class, new Result<Object>() {
            @Override
            public void handle(int statusCode, Object result, String stringResult) {}

            @Override
            public void retry(int attempt) {
                post("/api/friends", params, Object.class, this);
            }
        });
    }

    /**
     * Gets the user poke activity.
     *
     * @param offset pagination offset
     * @param resultHandler result handler
     */
    public void getActivity(final Long offset, final Result<List<Activity>> resultHandler) {
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
                        resultHandler.handle(statusCode, null, stringResult);
                    }
                }
                @Override
                public void retry(int attempt) {
                    resultHandler.retry(attempt);
                }
            });
        } else {
            resultHandler.handle(200, activityList, null);
        }
    }

    /**
     * Clears cached activity.
     */
    public void clearActivity() {
        this.activityLastOffset = 0;
        this.activityList = null;
    }


    /**
     * Returns a definition of audio files that can be sent as notifications. This is the audio.json
     * defining the sound and its checksum with other miscellaneous information.
     *
     * @param resultHandler result handler
     * @return request handle
     */
    public RequestHandle getAudioDefinition(Result<AudioDefinition> resultHandler) {
        return get(AUDIO_URL, null, AudioDefinition.class, resultHandler);
    }
}
