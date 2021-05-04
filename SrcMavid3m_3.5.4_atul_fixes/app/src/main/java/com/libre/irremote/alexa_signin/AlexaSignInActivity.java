package com.libre.irremote.alexa_signin;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;

import org.json.JSONException;
import org.json.JSONObject;

import com.amazon.identity.auth.device.authorization.api.AuthzConstants;

import com.danimahardhika.cafebar.CafeBar;
import com.libre.irremote.BLEApproach.BleCommunication;
import com.libre.irremote.BLEApproach.BleWriteInterface;
import com.libre.irremote.Constants.Constants;
import com.libre.irremote.MavidApplication;
import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;
import com.libre.irremote.models.ModelAlexaSkillResponse;
import com.libre.irremote.utility.AlexaSkillStatusEnum;
import com.libre.irremote.utility.ApiConstants;
import com.libre.irremote.utility.DB.MavidNodes;
import com.libre.irremote.utility.OnButtonClickCallback;
import com.libre.irremote.viewmodels.MavidAlexaSkillEnablingViewModel;
import com.libre.irremote.utility.UIRelatedClass;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Alexa.CompanionProvisioningInfo;
import com.libre.libresdk.TaskManager.Alexa.DeviceProvisioningInfo;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListenerWithResponse;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.DeviceInfo;
import com.libre.libresdk.TaskManager.Discovery.Listeners.ListenerUtils.MessageInfo;
import com.libre.libresdk.Util.LibreLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.libre.irremote.Dialogs.AlexaDialogs.somethingWentWrong;

public class AlexaSignInActivity extends AppCompatActivity implements View.OnClickListener, BleWriteInterface {

    private Button bt_signIn;
    private AppCompatImageView iv_home;
    private AppCompatImageView iv_back;
    private Dialog m_progressDlg;
    int avsStateOneCount = 0, avsStatetwoCount = 0, avsStatezeroCount = 0;
    private static boolean isAVSState2InProgress = false, isAVSState0InProgress = false;

    DeviceProvisioningInfo mDeviceProvisioningInfo;

    //    private static DeviceProvisioningInfo mDeviceProvisioningInfo;
    public static final String PRODUCT_ID = "productID";
    public static final String ALEXA_ALL_SCOPE = "alexa:all";
    public static final String DEVICE_SERIAL_NUMBER = "deviceSerialNumber";
    public static final String PRODUCT_INSTANCE_ATTRIBUTES = "productInstanceAttributes";
    public static final String[] APP_SCOPES = {ALEXA_ALL_SCOPE};
    final int ALEXA_META_DATA_TIMER = 0x12;
    private AmazonAuthorizationManager mAuthManager;
    private String speakerIpaddress;
    private String fromWhichActivity = "";
    private String from;
    private Dialog alertDialog;
    private boolean invalidApiKey;
    private int ACCESS_TOKEN_TIMEOUT = 301;
    private boolean isMetaDateRequestSent = false;

    CafeBar cafeBar;

    AppCompatTextView tv_alert_title;
    AppCompatTextView tv_alert_message;

    AppCompatButton btn_ok;

    private Dialog mDialog;

    AppCompatTextView progress_title;
    ProgressBar progress_bar;

    AppCompatTextView progress_message;

    int callMSearchfiveTimes;

    MavidAlexaSkillEnablingViewModel mavidAlexaSkillEnablingViewModel;

    private WebView mavidUserLoginWebView;

    private LinearLayout activity_main1;

    String authCode;

    View llToolbarView;

    UIRelatedClass uiRelatedClass = new UIRelatedClass();

    DeviceInfo deviceInfo;


    List<String> deviceFwVersionList = new ArrayList<String>();

    String appComparisonVersion;


    private Handler askRefreshTokenForEvery5SecHandler = new Handler(Looper.getMainLooper());
    private Runnable askRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            askRefreshTokenForEvery5SecHandler.removeCallbacks(askRefreshRunnable);
            Log.v("RecievedState", "Handler Running...  Runnable...Ask Refresh Token>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            askRefreshToken();
            isAVSState2InProgress = false;
            if (avsStatetwoCount < 10)
                askRefreshTokenForEvery5SecHandler.postDelayed(this, 5000);
        }
    };
    private Handler askRefreshTokenForEvery5SecHandlerState0 = new Handler(Looper.getMainLooper());
    private Runnable askRefreshRunnableState0 = new Runnable() {
        @Override
        public void run() {
            askRefreshTokenForEvery5SecHandlerState0.removeCallbacks(askRefreshRunnableState0);
            Log.v("RecievedState", "Handler Running...  Runnable...Ask Refresh Token STATE 0>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            askRefreshToken();
            isAVSState0InProgress = false;
            if (avsStatezeroCount < 10)
                askRefreshTokenForEvery5SecHandlerState0.postDelayed(this, 5000);
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALEXA_META_DATA_TIMER:
                    closeLoader();
                    Log.d("atul_alexa_signin", String.valueOf(msg));
                    somethingWentWrong(AlexaSignInActivity.this);

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alexa_signin);
        if (getIntent() != null) {
            from = getIntent().getStringExtra("fromActivity");
            speakerIpaddress = getIntent().getStringExtra("speakerIpaddress");
            fromWhichActivity = getIntent().getStringExtra("BeforeSignScreen");
            appComparisonVersion = getIntent().getStringExtra("appComparisonVersion");
        }
        LibreLogger.d(this, "suma in get ipaddress SIGNIN_IP" + speakerIpaddress);
        bt_signIn = (Button) findViewById(R.id.btn_login_amazon);
        iv_home = (AppCompatImageView) findViewById(R.id.iv_home);

        bt_signIn.setOnClickListener(this);
        iv_home.setOnClickListener(this);


        llToolbarView = findViewById(R.id.llToolbarView);


        deviceInfo = MavidNodes.getInstance().getDeviceInfoFromDB(speakerIpaddress);

        setAppComparisonVersion();

        if (MavidApplication.checkifAlexaSignStartedDuringConfiguration) {
//          BleCommunication bleCommunication = new BleCommunication(AlexaSignInActivity.this);
//          BleCommunication.writeInteractorStopSac();
            LibreLogger.d(this, "suma checking alexa sign in activity two");

        }
        // showLoader("Please wait..","");
        //callMSearchEvery500ms(new Timer());


        mavidAlexaSkillEnablingViewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(MavidAlexaSkillEnablingViewModel.class);


        mavidUserLoginWebView = findViewById(R.id.mavidUserLoginWebView);

        activity_main1 = findViewById(R.id.activity_main1);


        mavidUserLoginWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("atul_in_webview_url:", url);
                Uri parsedUrl = Uri.parse(url);
                if (parsedUrl.getHost().equals("127.0.0.1")) {

                    authCode = parsedUrl.getQueryParameter("code");

                    Log.d("atul_in_webview_auth:", authCode);

                    showLoader("Notice", "Please wait...");
                    enaableDisableAlexaSkill(authCode);

                    return true;
                }
                return false;
            }
        });


        mavidUserLoginWebView.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK) && mavidUserLoginWebView.canGoBack()) {
                    mavidUserLoginWebView.clearFocus();
                    mavidUserLoginWebView.goBack();
                    return true;
                }
                return false;
            }
        });

    }


    private void setAppComparisonVersion() {
        if (appComparisonVersion != null) {
            /* Got the app comp version
              from prev activity
              via MavidBLEConfigureSpeakerActivity
              */
        } else if (deviceInfo != null && deviceInfo.getFwVersion() != null) {
            /* appComparisonVersion is null
             * because configuration was done
             * via other methods
             * */
            //Dev.HGIBS.001.1046.001
            deviceFwVersionList = Arrays.asList(deviceInfo.getFwVersion().split("\\."));
            appComparisonVersion = deviceFwVersionList.get(4);

        } else {
            /* Both appComparisonVersion
             * and deviceInfo.getFwVersion() is null
             * taking default as the latest version
             * present in the app
             * */
            appComparisonVersion = String.valueOf(Constants.App_Compatibility_Version);
        }
    }

    public String getMsearchPayload() {
        return "M-SEARCH * HTTP/1.1\r\n" +
                "MX: 10\r\n" +
                "ST: urn:schemas-upnp-org:device:DDMSServer:1\r\n" +
                "HOST: 239.255.255.250:1800\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "\r\n";
    }


    public void enaableDisableAlexaSkill(final String authCode) {
        /**Disabling the skill*/
        mavidAlexaSkillEnablingViewModel.getAlexaStatus(AlexaSkillStatusEnum.DISABLE.getStatus(), authCode, speakerIpaddress).observe(this,
                new Observer<ModelAlexaSkillResponse>() {
                    @Override
                    public void onChanged(ModelAlexaSkillResponse modelAlexaSkillResponse) {
                        if (modelAlexaSkillResponse.getModelAlexaSkillResponseSucess() != null) {
                            if (modelAlexaSkillResponse.getModelAlexaSkillResponseSucess().getAlexaSkillStatus() == 1) {
                                /**Enabling skill */
                                mavidAlexaSkillEnablingViewModel.getAlexaStatus(AlexaSkillStatusEnum.ENABLE.getStatus(), authCode, speakerIpaddress).observe(
                                        AlexaSignInActivity.this, new Observer<ModelAlexaSkillResponse>() {
                                            @Override
                                            public void onChanged(ModelAlexaSkillResponse modelAlexaSkillResponse) {

                                                if (modelAlexaSkillResponse.getModelAlexaSkillResponseSucess() != null) {

                                                    if (modelAlexaSkillResponse.getModelAlexaSkillResponseSucess().getEnabledAlexaStatus().equalsIgnoreCase("ENABLED")) {

                                                        closeLoader();

                                                        new UIRelatedClass().buildSnackBarWithoutButton(AlexaSignInActivity.this,
                                                                getWindow().getDecorView().findViewById(android.R.id.content), "Yay Alexa skill is Enabled!!");

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                intentToAlexaThingsToTryDoneActivity();
                                                            }
                                                        }, 1500);

                                                    }
                                                } else {
                                                    new UIRelatedClass().buildSnackBarWithoutButton(AlexaSignInActivity.this,
                                                            getWindow().getDecorView().findViewById(android.R.id.content), "There seems to be an error while linking the skill");
                                                }

                                            }

                                        });
                            }
                        } else if (modelAlexaSkillResponse.getModelErrorAlexaErrorResponse() != null) {
                            closeLoader();
                            intentToAlexaThingsToTryDoneActivity();
                            new UIRelatedClass().buildSnackBarWithoutButton(AlexaSignInActivity.this,
                                    getWindow().getDecorView().findViewById(android.R.id.content), "There seems to be an error while linking the skill");
                        } else {
                            closeLoader();
                            intentToAlexaThingsToTryDoneActivity();
                            new UIRelatedClass().buildSnackBarWithoutButton(AlexaSignInActivity.this,
                                    getWindow().getDecorView().findViewById(android.R.id.content), "There seems to be an error while linking the skill");
                        }

                    }
                });
    }


    /**
     * As of now for this release the app compaitiblity
     * Needs to be updated appropriately for each customer release
     *
     * <p>
     * if it is equal == then allow for normal funtionality
     * if it is lesser than the device version then --> app needs to be updated
     * if it is greater than the app version then --> device needs to be updated
     */
    public boolean checkAppCompatiblityVersion(int deviceAppCompatiblityVersion) {
        if (deviceAppCompatiblityVersion < Constants.App_Compatibility_Version) {
            //device needs to be updated
            uiRelatedClass.showCustomAlertWhileFirmWareIsUpdating(AlexaSignInActivity.this);
            return false;
        } else if (deviceAppCompatiblityVersion > Constants.App_Compatibility_Version) {
            //tell the user that app needs be updated
            uiRelatedClass.showCustomAlertThatAppNeedsToBeUpdated(AlexaSignInActivity.this);
            return false;
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
            invalidApiKey = false;
        } catch (Exception e) {
            LibreLogger.d(this, "suma in alexa sign in activity" + e);
            invalidApiKey = true;
            buildSnackBar("" + e.getMessage());
            bt_signIn.setEnabled(false);
            bt_signIn.setAlpha(0.5f);
        }
        setMetaDateRequestSent(false);
    }

    @Override
    public void onClick(View view) {

        int i = view.getId();

        if (i == R.id.btn_login_amazon) {

            if (checkAppCompatiblityVersion(Integer.parseInt(appComparisonVersion))) {

                if (invalidApiKey) {
                    somethingWentWrong(AlexaSignInActivity.this);
                    return;
                }
                showLoader(getString(R.string.notice), getString(R.string.fetchingDetails));

                if (MavidApplication.checkifAlexaSignStartedDuringConfiguration) {
                    BleCommunication bleCommunication = new BleCommunication(AlexaSignInActivity.this);
                    BleCommunication.writeInteractorStopSac();
                    MavidApplication.gotoHomeTabOnClick = true;
                    LibreLogger.d(this, "suma in getting stop sac activity 21");

                } else {
                    MavidApplication.gotoHomeTabOnClick = false;

                }
                handler.sendEmptyMessageDelayed(ALEXA_META_DATA_TIMER, 15000);
                LibreMavidHelper.askMetaDataInfo(speakerIpaddress, new CommandStatusListenerWithResponse() {
                    @Override
                    public void response(MessageInfo messageInfo) {
                        String alexaMessage = messageInfo.getMessage();
                        Log.d("Alexalogin", " AVS STATE Alexa Value From 234  " + alexaMessage);
                        try {
                            final String TAG_WINDOW_CONTENT = "Window CONTENTS";
                            JSONObject jsonRootObject = new JSONObject(alexaMessage);
                            // JSONArray jsonArray = jsonRootObject.optJSONArray("Window CONTENTS");
                            JSONObject jsonObject = jsonRootObject.getJSONObject(TAG_WINDOW_CONTENT);
                            String productId = jsonObject.optString("PRODUCT_ID").toString();
                            String dsn = jsonObject.optString("DSN").toString();
                            String sessionId = jsonObject.optString("SESSION_ID").toString();
                            String codeChallenge = jsonObject.optString("CODE_CHALLENGE").toString();
                            String codeChallengeMethod = jsonObject.optString("CODE_CHALLENGE_METHOD").toString();
                            String locale = "";
                            if (jsonObject.has("LOCALE"))
                                locale = jsonObject.optString("LOCALE").toString();
                            mDeviceProvisioningInfo = new DeviceProvisioningInfo(productId, dsn, sessionId, codeChallenge, codeChallengeMethod, locale);
                            handler.removeMessages(ALEXA_META_DATA_TIMER);
                            setAlexaViews();
                            closeLoader();
                            Bundle options = new Bundle();

                            JSONObject scopeData = new JSONObject();
                            JSONObject productInfo = new JSONObject();
                            JSONObject productInstanceAttributes = new JSONObject();
                            productInstanceAttributes.put(DEVICE_SERIAL_NUMBER, mDeviceProvisioningInfo.getDsn());
                            productInfo.put(PRODUCT_ID, mDeviceProvisioningInfo.getProductId());
                            productInfo.put(PRODUCT_INSTANCE_ATTRIBUTES, productInstanceAttributes);
                            scopeData.put(ALEXA_ALL_SCOPE, productInfo);

                            options.putString(AuthzConstants.BUNDLE_KEY.SCOPE_DATA.val, scopeData.toString());
                            options.putBoolean(AuthzConstants.BUNDLE_KEY.GET_AUTH_CODE.val, true);
                            options.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE.val, codeChallenge);
                            options.putString(AuthzConstants.BUNDLE_KEY.CODE_CHALLENGE_METHOD.val, codeChallengeMethod);
                            if (mAuthManager != null) {
                                mAuthManager.authorize(APP_SCOPES, options, new AuthListener());
                            }
                            askRefreshToken();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void failure(Exception e) {
                        Log.d("Alexalogin", e.getMessage());
                    }

                    @Override
                    public void success() {

                    }
                });
            }
        } else if (i == R.id.iv_home) {
            LibreLogger.d(this, "suma in getting home icon click\n" + fromWhichActivity);
            if (fromWhichActivity != null && fromWhichActivity.equals("MavidBLEConfigureSpeakerActivity")) {
                if (MavidApplication.checkifAlexaSignStartedDuringConfiguration) {
//                    MavidApplication.checkifAlexaSignStartedDontGOTOHomeTABS=false;
                    showLoader("", "Please Wait...");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (MavidApplication.gotoHomeTabOnClick) {
                        BleCommunication bleCommunication = new BleCommunication(AlexaSignInActivity.this);
                        BleCommunication.writeInteractorStopSac();
                        startActivity(new Intent(AlexaSignInActivity.this, MavidHomeTabsActivity.class));
                        LibreLogger.d(this, "suma in getting home icon click1\n" + fromWhichActivity + "gotoHomeTABS\n" + MavidApplication.checkifAlexaSignStartedDontGOTOHomeTABS);
                        closeLoader();
                    } else {
                        BleCommunication bleCommunication = new BleCommunication(AlexaSignInActivity.this);
                        BleCommunication.writeInteractorStopSac();
                        startActivity(new Intent(AlexaSignInActivity.this, MavidHomeTabsActivity.class));
                        MavidApplication.clickedHomeDonsendStopSAC = true;
                        LibreLogger.d(this, "suma in getting stop sac activity 33");
                        LibreLogger.d(this, "suma in getting home icon click2\n" + fromWhichActivity + "gotoHomeTABS\n" + MavidApplication.checkifAlexaSignStartedDontGOTOHomeTABS);
                        closeLoader();
                    }


                }
            } else if (fromWhichActivity != null && fromWhichActivity.equals("DeviceSettingsActivity")) {
                Intent intent = new Intent(AlexaSignInActivity.this, MavidHomeTabsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("mSACConfiguredIpAddress", speakerIpaddress);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                LibreLogger.d(this, "suma in getting home icon click2\n" + fromWhichActivity);

            } else {
                Intent intent = new Intent(AlexaSignInActivity.this, MavidHomeTabsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("mSACConfiguredIpAddress", speakerIpaddress);
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                LibreLogger.d(this, "suma in getting home icon click3\n" + fromWhichActivity);

            }


            //go to homescreen
//            if (from != null && !from.isEmpty()) {
//                startActivity(new Intent(this, AlexaThingsToTryDoneActivity.class)
//                        .putExtra("speakerIpaddress", speakerIpaddress)
//                        .putExtra("fromActivity", from)
//                        .putExtra("prevScreen", AlexaSignInActivity.class.getSimpleName()));
//                finish();
//            }

        }
    }


    public void buildSnackBar(String message) {
        CafeBar.Builder builder = CafeBar.builder(AlexaSignInActivity.this);
        builder.autoDismiss(true);
        builder.customView(R.layout.custom_snackbar_layout);

        cafeBar = builder.build();
        AppCompatTextView tv_message = cafeBar.getCafeBarView().findViewById(R.id.tv_message);
        tv_message.setText(message);


        cafeBar.show();
    }

    private void setAlexaViews() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bt_signIn.setEnabled(true);
                bt_signIn.setAlpha(1f);
            }
        });
    }

    private void performSigninClick() {
        findViewById(R.id.btn_login_amazon).performClick();
    }

    private void intentToAlexaLangUpdateActivity() {
        Intent alexaLangScreen = new Intent(AlexaSignInActivity.this, AlexaLangUpdateActivity.class);
        alexaLangScreen.putExtra("current_deviceip", speakerIpaddress);
        alexaLangScreen.putExtra("fromActivity", from);
        alexaLangScreen.putExtra("prevScreen", AlexaSignInActivity.class.getSimpleName());
        startActivity(alexaLangScreen);
        finish();
    }

//    private void closeLoader() {
//        if (m_progressDlg != null) {
//            if (m_progressDlg.isShowing()) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        m_progressDlg.dismiss();
//                    }
//                });
//            }
//        }
//    }

    public void closeLoader() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    if (!(AlexaSignInActivity.this.isFinishing())) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }
        });
    }

//    private void showLoader() {
//        if (AlexaSignInActivity.this.isFinishing())
//            return;
//        if (m_progressDlg == null)
//            m_progressDlg = ProgressDialog.show(AlexaSignInActivity.this, getString(R.string.notice), getString(R.string.fetchingDetails), true, true, null);
//        if (!m_progressDlg.isShowing()) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    m_progressDlg.show();
//                }
//            });
//        }
//    }

    public void showLoader(final String title, final String message) {
        if (mDialog == null) {
            mDialog = new Dialog(AlexaSignInActivity.this);
            mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialog.setContentView(R.layout.custom_progress_bar);

            mDialog.setCancelable(false);
            progress_title = mDialog.findViewById(R.id.progress_title);
            progress_bar = mDialog.findViewById(R.id.progress_bar);
            progress_message = mDialog.findViewById(R.id.progress_message);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("ShowingLoader", "Showing loader method");
                progress_title.setText(title);
                progress_message.setText(message);
                progress_bar.setIndeterminate(true);
                progress_bar.setVisibility(View.VISIBLE);
                if (!(AlexaSignInActivity.this.isFinishing())) {
                    mDialog.show();
                }
            }
        });
    }


    public boolean isMetaDateRequestSent() {
        return isMetaDateRequestSent;
    }

    public void setMetaDateRequestSent(boolean metaDateRequestSent) {
        isMetaDateRequestSent = metaDateRequestSent;
    }

    @Override
    public void onWriteSuccess() {

    }

    @Override
    public void onWriteFailure() {

    }

    private class AuthListener implements AuthorizationListener {
        @Override
        public void onSuccess(Bundle response) {
            try {
                final String authorizationCode = response.getString(AuthzConstants.BUNDLE_KEY.AUTHORIZATION_CODE.val);
                final String redirectUri = mAuthManager.getRedirectUri();
                final String clientId = mAuthManager.getClientId();
                final String sessionId = mDeviceProvisioningInfo.getSessionId();

                final CompanionProvisioningInfo companionProvisioningInfo = new CompanionProvisioningInfo(sessionId, clientId, redirectUri, authorizationCode);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLoader(getString(R.string.notice), getString(R.string.fetchingDetails));
                    }
                });
                LibreMavidHelper.sendAlexaAuthDetails(speakerIpaddress, companionProvisioningInfo, new CommandStatusListenerWithResponse() {
                    @Override
                    public void response(final MessageInfo messageInfo) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // closeLoader();
                                Log.d("Alexa sign in", " got 235 response : " + messageInfo.getMessage());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showLoader(getString(R.string.notice), getString(R.string.fetchingDetails));
                                    }
                                });
                                // intentToAlexaThingsToTryActivity();
                                askRefreshToken();
                            }
                        });
                    }

                    @Override
                    public void failure(final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("atul_alexa_signin", e.getMessage());
                            }
                        });

                    }

                    @Override
                    public void success() {
                        //intentToAlexaLangUpdateActivity();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                askRefreshToken();
                                // intentToAlexaThingsToTryActivity();
                            }
                        });
                    }
                });
                Log.e("Alexa sign in", "Alexa authorization successfull : " + companionProvisioningInfo.toJson().toString());

            } catch (final AuthError authError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("atul_alexa_auth_error", authError.getMessage());
                        authError.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(final AuthError ae) {
            Log.e("LSSDPNETWORK", "AuthError during authorization", ae);
            String error = ae.getMessage();
            if (error == null || error.isEmpty())
                error = ae.toString();
            final String finalError = error;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        showAlertDialog(finalError);
                    }
                }
            });
        }

        @Override
        public void onCancel(Bundle cause) {
            Log.e("LSSDPNETWORK", "User cancelled authorization");
            final String finalError = "Cancelled the Login with Alexa";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        showAlertDialog(finalError);
                    }
                }
            });
        }
    }

    private void intentToAlexaThingsToTryActivity() {
        Intent i = new Intent(AlexaSignInActivity.this, AlexaThingsToTryDoneActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("speakerIpaddress", speakerIpaddress);
        i.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
        startActivity(i);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeLoader();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);

    }

    private void showAlertDialog(String error) {

        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();

        alertDialog = null;

        alertDialog = new Dialog(AlexaSignInActivity.this);

        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        alertDialog.setContentView(R.layout.custom_single_button_layout);

        alertDialog.setCancelable(false);

        tv_alert_title = alertDialog.findViewById(R.id.tv_alert_title);

        tv_alert_message = alertDialog.findViewById(R.id.tv_alert_message);

        btn_ok = alertDialog.findViewById(R.id.btn_ok);

        btn_ok.setText("Close");

        tv_alert_title.setText("Error");

        tv_alert_message.setText(error);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();

//        AlertDialog.Builder builder = new AlertDialog.Builder(AlexaSignInActivity.this);
//        builder.setTitle("Alexa Signin Error");
//        builder.setMessage(error);
//        builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                alertDialog.dismiss();
//            }
//        });
//
//        if (alertDialog == null) {
//            alertDialog = builder.create();
//            alertDialog.show();
//        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // handleBack();
    }

    private void handleBack() {
        startActivity(new Intent(AlexaSignInActivity.this, MavidHomeTabsActivity.class));
        finish();
    }

    private void askRefreshToken() {
        LibreMavidHelper.askRefreshToken(speakerIpaddress, new CommandStatusListenerWithResponse() {
            @Override
            public void response(MessageInfo messageInfo) {
                String messages = messageInfo.getMessage();
                Log.d("DeviceManager", " AVS STATE got alexa token After Sac" + messages);
                messages = messages.substring(messages.length() - 1);

//                if (!messages.equalsIgnoreCase("0")) {
//                    if (avsStateInitCount != 0) {
//                        closeLoader();
//                    }
//                }

                try {
                    int messageVal = Integer.parseInt(messages);
                    if (messageVal > 3)
                        messages = "3";
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                Log.v("RecievedState", "state is getting thread timer state 2 alexa sign in if try getting ALEXA MSGS " + messages);

                switch (messages) {
                    case "0":
                        Log.v("RecievedState", "state is 0");
                        if (!isAVSState0InProgress) {
                            isAVSState0InProgress = true;
                            avsStatezeroCount = avsStatezeroCount + 1;
                            Log.v("RecievedState", "AVS STATE 0  Count IS AVS_STATE0...." + avsStatezeroCount);

                            if (avsStatezeroCount < 15) {
                                Log.v("RecievedState", "AVS STATE 0 COUNT LESS THAN 6..................." + avsStatezeroCount);
                                askRefreshTokenForEvery5SecHandlerState0.removeCallbacks(askRefreshRunnableState0);
                                askRefreshTokenForEvery5SecHandlerState0.postDelayed(askRefreshRunnableState0, 5000);

                            } else {
                                showLoader("Alert", "Unable To Connect to Speaker");
                                Log.v("RecievedState", "AVS STATE 0 COUNT LESS THAN 6 ELSE ..................." + avsStatezeroCount);
                                Log.v("RecievedState", "state is 0>>>> count greater than 6...stopping ask refresh token");
                                askRefreshTokenForEvery5SecHandlerState0.removeCallbacks(askRefreshRunnableState0);
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AlexaSignInActivity.this, "Fetching Alexa Login State from Device failed.Please try again later!!....", Toast.LENGTH_SHORT).show();
                                        Intent ssid = new Intent(AlexaSignInActivity.this, MavidHomeTabsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(ssid);
                                        Log.v("RecievedState", "AVS STATE 0 COUNT isFinishing onBTN click ..................." + avsStatetwoCount);
                                        finish();
                                    }
                                });


                            }

                        } else {
                            Log.v("RecievedState", "AVS STATE 0 IN PROGRESSSSSSS ELSE");

                        }
                        break;

                    case "1":
                        closeLoader();
                        askRefreshTokenForEvery5SecHandlerState0.removeCallbacks(askRefreshRunnableState0);
                        Log.v("RecievedState", "state is 1");
                        // buildSnackBar("Please try again Login Later!!..");
                        break;

                    case "2":
                        Log.v("RecievedState", "state is 2");
                        if (!isAVSState2InProgress) {
                            isAVSState2InProgress = true;
                            avsStatetwoCount = avsStatetwoCount + 1;
                            Log.v("RecievedState", "AVS STATE 2  Alexa sign Count IS AVS_STATE2...." + avsStatetwoCount);

                            if (avsStatetwoCount < 15) {
                                Log.v("RecievedState", "AVS STATE 2 Alexa sign COUNT LESS THAN 6..................." + avsStatetwoCount);
                                askRefreshTokenForEvery5SecHandler.removeCallbacks(askRefreshRunnable);
                                askRefreshTokenForEvery5SecHandler.postDelayed(askRefreshRunnable, 5000);

                            } else {
                                showLoader("Alert", "Unable To Connect to Speaker");
                                Log.v("RecievedState", "AVS STATE 2 Alexa sign COUNT LESS THAN 6 ELSE ..................." + avsStatetwoCount);
                                Log.v("RecievedState", "state is 2 Alexa sign>>>> count greater than 6...stopping ask refresh token");
                                askRefreshTokenForEvery5SecHandler.removeCallbacks(askRefreshRunnable);
                                Handler handler = new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AlexaSignInActivity.this, "Fetching Alexa Login State from Device failed.Please try again later!!....", Toast.LENGTH_SHORT).show();
                                        //ShowAlertDynamicallyGoingToHomeScreen("ALert","Unable to Connect to Speaker..",MavidBLEConfigureSpeakerActivity.this);
                                        //showTimeOutDailog();
                                        Intent ssid = new Intent(AlexaSignInActivity.this,
                                                MavidHomeTabsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(ssid);
                                        Log.v("RecievedState", "AVS STATE 2 Alexa sign COUNT isFinishing onBTN click ..................." + avsStatetwoCount);
                                        finish();
                                    }
                                });


                            }

                        } else {
                            Log.v("RecievedState", "AVS STATE 2 Alexa sign IN PROGRESSSSSSS ELSE");

                        }

                        break;

                    case "3":
                        closeLoader();


                        /**Call the authorize url to login the user
                         * */

                        String baseURL = ApiConstants.BASE_URL + "oauth2/authorize?";

                        final Uri authURl = Uri.parse(baseURL).buildUpon()
                                .appendQueryParameter("response_type", "code")
                                .appendQueryParameter("client_id", getString(R.string.skill_client_id))
                                .appendQueryParameter("redirect_uri", "https://127.0.0.1")
                                .appendQueryParameter("scope", "openid")
                                .appendQueryParameter("state", UUID.randomUUID().toString())
                                .build();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity_main1.setVisibility(View.GONE);

                                llToolbarView.setVisibility(View.GONE);

                                mavidUserLoginWebView.setVisibility(View.VISIBLE);

                                mavidUserLoginWebView.loadUrl(authURl.toString());
                            }
                        });

//                        intentToAlexaThingsToTryDoneActivity();
                        askRefreshTokenForEvery5SecHandlerState0.removeCallbacks(askRefreshRunnableState0);
                        askRefreshTokenForEvery5SecHandler.removeCallbacks(askRefreshRunnable);
                        Log.v("RecievedState", "AVS STATE 3 in AlexaSignIN");

                        break;

                    case "-1":
                        buildSnackBar("Device Unable to Login");
                        // intentToHome();
                        Log.v("RecievedState", "state is -1");
                        break;


                }
            }

            @Override
            public void failure(Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to get AVS STATE response from Device.Try Again Later!!." + getApplicationContext(), Toast.LENGTH_SHORT).show();
                Intent ssid = new Intent(AlexaSignInActivity.this, MavidHomeTabsActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(ssid);
            }

            @Override
            public void success() {

            }
        });
    }

    //    public void intentToAlexaSignInActivity() {
//        Intent newIntent = new Intent(AlexaSignInActivity.this, AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        newIntent.putExtra("speakerIpaddress", mSACConfiguredIpAddress);
//        newIntent.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
//        startActivity(newIntent);
//    }
    public void intentToAlexaThingsToTryDoneActivity() {
        Intent i = new Intent(AlexaSignInActivity.this, AlexaThingsToTryDoneActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("speakerIpaddress", speakerIpaddress);
        i.putExtra("fromActivity", MavidHomeTabsActivity.class.getSimpleName());
        startActivity(i);
        finish();
    }
}
