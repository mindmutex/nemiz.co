package co.nemiz.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import co.nemiz.services.DefaultRestService;
import co.nemiz.ui.MainActivity;

public class AccountAuthenticator extends AbstractAccountAuthenticator {
    private final static String TAG  = AccountAuthenticator.class.getSimpleName();
    private final Context context;

    public AccountAuthenticator(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
            String accountType, String authTokenType, String[] features, Bundle bundle) throws NetworkErrorException {

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(AccountUtils.ACCOUNT_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);

        Bundle result = new Bundle();
        result.putParcelable(AccountManager.KEY_INTENT, intent);

        return result;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
            Account account, Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse,
                Account account, String authTokenType, Bundle bundle)
            throws NetworkErrorException {

        final Bundle result = new Bundle();

        if (!authTokenType.equals(AccountUtils.AUTH_TOKEN_TYPE)) {
            result.putString(
                AccountManager.KEY_ERROR_MESSAGE, "invalid token type");
            return result;
        }
        AccountManager accountManager = AccountManager.get(context);
        String password = accountManager.getPassword(account);

        String clientAccess = accountManager.getUserData(account, AccountUtils.KEY_CLIENT_ID);
        String clientSecret =
            accountManager.getUserData(account, AccountUtils.KEY_CLIENT_SECRET);

        String token = obtainAccessToken(clientAccess, clientSecret);

        result.putString(AccountManager.KEY_ACCOUNT_TYPE, authTokenType);
        result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        result.putString(AccountManager.KEY_PASSWORD, password);
        result.putString(AccountManager.KEY_AUTHTOKEN, token);

        return result;
    }

    public String obtainAccessToken(String clientId, String clientSecret) {
        List<NameValuePair> nvp = new ArrayList<>(1);
        nvp.add(new BasicNameValuePair("grant_type", "client_credentials"));
        nvp.add(new BasicNameValuePair("client_id", clientId));
        nvp.add(new BasicNameValuePair("client_secret", clientSecret));

        String params = URLEncodedUtils.format(nvp, "UTF-8");

        HttpClient client = new DefaultHttpClient();

        HttpGet request = new HttpGet(DefaultRestService.absoluteUrl("/oauth/v2/token") + "?" + params);
        request.getParams().setParameter("grant_type", "client_credentials");
        request.getParams().setParameter("client_id", clientId);
        request.getParams().setParameter("client_secret", clientSecret);

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String response = client.execute(request, responseHandler);

            JSONObject json = new JSONObject(response);
            return json.getString("access_token");
        } catch (JSONException | IOException e) {
            Log.w(TAG, "Failed to obtain token", e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                Account account, String s, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse,
            Account account, String[] strings) throws NetworkErrorException {
        return null;
    }
}
