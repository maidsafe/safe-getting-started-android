package net.maidsafe.sample.viewmodel;

import android.net.Uri;
import android.system.Os;
import android.util.Log;

import net.maidsafe.api.Authenticator;
import net.maidsafe.api.model.AuthIpcRequest;
import net.maidsafe.api.model.IpcRequest;
import net.maidsafe.sample.services.AsyncOperation;
import net.maidsafe.sample.services.Result;
import net.maidsafe.sample.services.SafeApi;

public class MockServices {

    private static final String LOCATOR = "locator";
    private static final String PASSWORD = "password";
    private static final String INVITE = "invite";
    private static final String ACCOUNT_EXISTS_CODE = "-102";

    public static Uri mockAuthenticate(String uri) throws Exception {
        Authenticator authenticator = null;
        try {
            authenticator = Authenticator.createAccount(LOCATOR, PASSWORD, INVITE).get();
            Log.d("STAGE:","Account created");
        } catch (Exception e) {
            if (e.getMessage().contains(ACCOUNT_EXISTS_CODE)) {
                authenticator = Authenticator.login(LOCATOR, PASSWORD).get();
                Log.d("STAGE:","Logged in to existing account");
            } else {
                e.printStackTrace();
            }
        }

        if (authenticator == null) {
            throw new Exception("Not logged in!" + "\nMOCK VAULT PATH: " + Os.getenv("SAFE_MOCK_VAULT_PATH"));
        }
        String data = uri.replaceAll(".*\\/+", "");
        IpcRequest request = authenticator.decodeIpcMessage(data).get();
        AuthIpcRequest authIpcRequest = (AuthIpcRequest) request;
        String response = authenticator.encodeAuthResponse(authIpcRequest, true).get();
        String appId = authIpcRequest.getAuthReq().getApp().getId();

        return Uri.parse(appId + "://" + response);
    }

    public static void simulateDisconnect(){
        new AsyncOperation(loading -> {

        }).execute(() -> {
            try {
                SafeApi api = SafeApi.getInstance(null);
                api.disconnect();
                return new Result(null);
            } catch (Exception e) {
                return new Result(e);
            }
        }).onResult(result -> {

        }).onException(e -> {
           e.printStackTrace();
        });
    }
}
