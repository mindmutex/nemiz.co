package co.nemiz.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

import java.util.ArrayList;
import java.util.List;

import co.nemiz.R;
import co.nemiz.domain.Client;
import co.nemiz.domain.Device;
import co.nemiz.domain.Register;
import co.nemiz.domain.User;
import co.nemiz.services.DefaultRestService;
import co.nemiz.auth.AccountUtils;
import co.nemiz.dao.ApplicationContext;

public class RegisterActivity extends Activity {
    private Register registrationRequest = new Register();
    private List<GraphUser> facebookFriends = new ArrayList<>();

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_register);
        context = this;


        final Button register = (Button) findViewById(R.id.btnRegister);
        register.setEnabled(false);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateRegister()) {
                    register();
                }
            }
        });

        final Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser graphUser, Response response) {
                    Request.newMyFriendsRequest(session, new Request.GraphUserListCallback() {
                        @Override
                        public void onCompleted(List<GraphUser> graphUsers, Response response) {
                            facebookFriends = graphUsers;
                        }
                    }).executeAsync();

                    if (graphUser != null) {
                        applyFacebookDetails(graphUser);
                    }
                    register.setEnabled(true);
                }
            }).executeAsync();
        }
    }

    private void applyFacebookDetails(GraphUser graphUser) {

        User user = new User();
        user.setUsername(graphUser.getUsername());
        user.setEmail(graphUser.asMap().get("email").toString());
        user.setName(graphUser.getName());
        user.setFacebook(graphUser.getId());

        registrationRequest.setUser(user);

        EditText nameEdit = (EditText) findViewById(R.id.txtName);
        EditText mailEdit = (EditText) findViewById(R.id.txtEmail);

        nameEdit.setText(user.getName());
        mailEdit.setText(user.getEmail());
    }

    private boolean validateRegister() {
        EditText passwordEdit = (EditText) findViewById(R.id.txtPassword);
        EditText passwordConfirmEdit =
            (EditText) findViewById(R.id.txtPasswordConfirm);

        String password = passwordEdit.getText().toString();
        String passwordConfirm = passwordConfirmEdit.getText().toString();

        int errorCode = 0;
        if (password.isEmpty()) {
            errorCode = R.string.password_required;
        } else if (passwordConfirm.isEmpty()) {
            errorCode = R.string.password_confirm_required;
        } else if (!password.equals(passwordConfirm)) {
            errorCode = R.string.password_no_match;
        }
        if (errorCode != 0) {
            Toast.makeText(this, errorCode, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void register() {
        EditText passwordEdit = (EditText) findViewById(R.id.txtPassword);
        ApplicationContext appContext = ApplicationContext.get();

        Device device = new Device();
        device.setName(android.os.Build.MODEL);
        device.setType("android");
        device.setToken(appContext.getPushRegistrationId());

        List<String> friends = new ArrayList<>();
        for (GraphUser friend : facebookFriends) {
            friends.add(friend.getId());
        }

        registrationRequest.getUser().setPassword(passwordEdit.getText().toString());
        registrationRequest.setDevice(device);
        registrationRequest.setFriends(friends);

        toggleRegistrationMode(true);

        DefaultRestService service = DefaultRestService.get();
        service.register(registrationRequest, new DefaultRestService.Result<Client>() {
            @Override
            public void handle(int statusCode, Client result, String stringResult) {
                if (result != null) {
                    User user = registrationRequest.getUser();
                    AccountUtils.createAccount(
                        context, user.getEmail(), user.getPassword(), result);

                    startActivity(new Intent(context, MainActivity.class));
                } else {
                    Toast.makeText(context, R.string.register_failed, Toast.LENGTH_SHORT).show();
                    toggleRegistrationMode(false);
                }
            }
        });
    }

    private void toggleRegistrationMode(boolean status) {
        findViewById(R.id.layoutRegister).setVisibility(status ? View.VISIBLE : View.GONE);
        findViewById(R.id.layoutForm)
            .setVisibility(status ? View.GONE : View.VISIBLE);
    }
}
