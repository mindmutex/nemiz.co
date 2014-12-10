package co.nemiz.auth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;

import co.nemiz.domain.Client;

public class AccountUtils {
    public final static String ACCOUNT_TYPE = "co.nemiz.account";
    public final static String AUTH_TOKEN_TYPE = "co.nemiz.account.oauth2";
    public final static String KEY_USER_ID = "UserId";
    public final static String KEY_CLIENT_ID = "ClientId";
    public final static String KEY_CLIENT_SECRET = "ClientSecret";

    public static void createAccount(Context context, String username, String password, Client client) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(username, AccountUtils.ACCOUNT_TYPE);

        Bundle userData = new Bundle();
        userData.putString(AccountUtils.KEY_CLIENT_ID, client.getClientId());
        userData.putString(AccountUtils.KEY_CLIENT_SECRET, client.getClientSecret());
        userData.putString(AccountUtils.KEY_USER_ID, client.getUserId());

        accountManager.addAccountExplicitly(account, password, userData);
   }
}
