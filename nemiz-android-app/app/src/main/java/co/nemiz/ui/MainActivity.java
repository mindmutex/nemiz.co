package co.nemiz.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.RequestHandle;

import java.io.IOException;
import java.util.List;

import co.nemiz.R;
import co.nemiz.domain.AudioDefinition;
import co.nemiz.domain.User;
import co.nemiz.services.AudioManager;
import co.nemiz.services.DefaultRestService;
import co.nemiz.auth.AccountUtils;
import co.nemiz.dao.ApplicationContext;
import co.nemiz.support.AsyncHttpRequestPool;

public class MainActivity extends Activity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = MainActivity.class.getCanonicalName();

    /**
     * Obtained from Google API console (linked to ivarsv@gmail.com account)
     */
    private static final String SENDER_ID = "918488499724";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // initialise context to be accessible statically
        ApplicationContext.init(getSharedPreferences("Nemiz", Context.MODE_PRIVATE));
        setContentView(R.layout.activity_main);

        // clear active facebook session if there is any
        if (Session.getActiveSession() != null) {
            Session.getActiveSession().closeAndClearTokenInformation();
        }
        if (checkPlayServices()) {
            initializeGcmIfRequired();

            AccountManager accountManager = AccountManager.get(this);
            Account[] accounts = accountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
            if (accounts.length == 0) {
                // attempt to login if facebook session is not open.
                startActivity(new Intent(this, LoginActivity.class));
            } else {
                obtainAccessToken(accounts[0]);
            }
        } else {
            handleFailure(R.string.play_services_error);
        }
    }

    private void handleFailure(int messageCode) {
        Toast.makeText(this, messageCode, Toast.LENGTH_LONG).show();
        findViewById(R.id.progressBar).setVisibility(View.GONE);
    }

    /**
     * Checks for the play services support.
     *
     * @return status either supported or not
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void initializeGcmIfRequired() {
        final ApplicationContext appContext = ApplicationContext.get();
        final Context context = this;

        if (appContext.getPushRegistrationId().isEmpty()) {
            AsyncTask<Void, String, String> asyncTask = new AsyncTask<Void, String, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String msg;
                    try {
                        GoogleCloudMessaging messaging = GoogleCloudMessaging.getInstance(context);
                        String token = messaging.register(SENDER_ID);
                        msg = "Device registered, registration ID=" + token;

                        appContext.setPushRegistrationId(token);
                    } catch (IOException ex) {
                        msg = "Error :" + ex.getMessage();
                    }
                    return msg;
                }

                @Override
                protected void onPostExecute(String msg) {
                    Log.i(TAG, msg);
                }
            };
            asyncTask.execute(null, null, null);
        }
    }

    private void obtainAccessToken(Account account) {
        AccountManager accountManager = AccountManager.get(this);
        if (DefaultRestService.hasAccessToken()) {
            startActivity(new Intent(this, ContactsActivity.class));
        } else {
            final Context context = this;
            accountManager.getAuthToken(account, AccountUtils.AUTH_TOKEN_TYPE,
                    null, this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> bundleAccountManagerFuture) {
                    try {
                        Bundle result = bundleAccountManagerFuture.getResult();

                        DefaultRestService.setAccessToken(result.getString(AccountManager.KEY_AUTHTOKEN));

                        initializeContent();
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to authenticate", e);
                        handleFailure(R.string.access_token_error);
                    }
                }
            }, null);
        }
    }

    private void initializeContent() {
        final Context context = this;

        DefaultRestService service = DefaultRestService.get();

        AsyncHttpRequestPool requestPool = new AsyncHttpRequestPool();
        requestPool.setListener(new AsyncHttpRequestPool.AsyncHttpRequestPoolListener() {
            @Override
            public void onRequestsComplete() {
                startActivity(new Intent(context, ContactsActivity.class));
            }
        });
        requestPool.add(service.getContacts(null, new DefaultRestService.BaseResult<List<User>>()));
        requestPool.add(service.getAudioDefinition(new DefaultRestService.Result<AudioDefinition>() {
            @Override
            public void handle(int statusCode, AudioDefinition result, String stringResult) {
                AudioManager audioManager = AudioManager.get(context);
                audioManager.setDefinition(result);
            }
        }));

        try {
            requestPool.waitForAll(10000000);
        } catch (InterruptedException e) {
            Log.e(TAG, "Failed to initialize data");
        }
    }
}