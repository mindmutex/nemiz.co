package co.nemiz.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import java.util.Arrays;
import java.util.List;

import co.nemiz.R;
import co.nemiz.domain.Client;
import co.nemiz.domain.Device;
import co.nemiz.services.DefaultRestService;
import co.nemiz.auth.AccountUtils;
import co.nemiz.dao.ApplicationContext;

public class LoginActivity extends Activity {
    private UiLifecycleHelper uiHelper;

    private Session.StatusCallback sessionStatusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session);
        }
    };

    // add required from facebook here
    private static final List<String> PERMISSIONS = Arrays.asList("public_profile","email", "user_friends");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_login);

        uiHelper = new UiLifecycleHelper(this, sessionStatusCallback);
        uiHelper.onCreate(savedInstanceState);

        Session session = Session.getActiveSession();
        if (session != null && (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session);
        }
        LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions(PERMISSIONS);

        Button loginButton = (Button) findViewById(R.id.btnLogin);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txtUsername = (TextView) findViewById(R.id.txtEmail);
                TextView txtPassword = (TextView) findViewById(R.id.txtPassword);

                login(txtUsername.getText().toString(), txtPassword.getText().toString());
            }
        });
    }

    private void login(final String username, final String password) {
        final Context context = this;
        final Button loginButton = (Button) findViewById(R.id.btnLogin);

        ApplicationContext appContext = ApplicationContext.get();

        final Device device = new Device();
        device.setName(android.os.Build.MODEL);
        device.setType("android");
        device.setToken(appContext.getPushRegistrationId());

        loginButton.setEnabled(false);

        final DefaultRestService service = DefaultRestService.get();
        service.login(username, password, new DefaultRestService.BaseResult<String>() {
            @Override
            public void handle(int statusCode, String result, String stringResult) {
                if (result != null) {
                    DefaultRestService.setAccessToken(result);
                    service.createDevice(device, new DefaultRestService.BaseResult<Client>() {
                        @Override
                        public void handle(int statusCode, Client result, String stringResult) {
                            if (result != null) {
                                AccountUtils.createAccount(context, username, password, result);
                                startActivity(new Intent(context, MainActivity.class));
                            } else {
                                Toast.makeText(context,
                                    R.string.login_failed, Toast.LENGTH_SHORT).show();
                            }
                            loginButton.setEnabled(true);
                        }
                    });
                } else {
                    Toast.makeText(context, R.string.login_failed, Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                }
            }
        });
    }

    private void onSessionStateChange(Session session) {
        if (session != null && session.isOpened()) {
            startActivity(new Intent(this, RegisterActivity.class));
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccountsByType(AccountUtils.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            uiHelper.onResume();
        } else {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }
}
