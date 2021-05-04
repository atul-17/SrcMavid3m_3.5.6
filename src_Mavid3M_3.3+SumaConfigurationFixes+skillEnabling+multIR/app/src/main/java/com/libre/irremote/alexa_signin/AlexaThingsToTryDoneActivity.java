package com.libre.irremote.alexa_signin;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.libre.irremote.MavidHomeTabsActivity;
import com.libre.irremote.R;
import com.libre.irremote.models.ModelAlexaSkillResponse;
import com.libre.irremote.utility.AlexaSkillStatusEnum;
import com.libre.irremote.utility.MavidAlexaSkillEnablingViewModel;
import com.libre.irremote.utility.UIRelatedClass;
import com.libre.libresdk.LibreMavidHelper;
import com.libre.libresdk.TaskManager.Communication.Listeners.CommandStatusListener;


public class AlexaThingsToTryDoneActivity extends AppCompatActivity implements View.OnClickListener {

    private String speakerIpaddress;
    private TextView text_done, mAlexaApp;
    private Button changeLangBtn;
    //    private ImageView back;
    private String fromActivity;
    private String prevScreen;
//    private ProgressDialog m_progressDlg;

    private Dialog mDialog;

    AppCompatTextView progress_title;
    ProgressBar progress_bar;

    AppCompatTextView progress_message;

    AppCompatTextView text_sign_out;

    int callMSearchfiveTimes;

    MavidAlexaSkillEnablingViewModel mavidAlexaSkillEnablingViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.alexa_things_to_try_done);

        Intent myIntent = getIntent();
        if (myIntent != null) {
            speakerIpaddress = myIntent.getStringExtra("speakerIpaddress");
        }

        text_done = (TextView) findViewById(R.id.text_done);
        mAlexaApp = (TextView) findViewById(R.id.tv3);
        changeLangBtn = (Button) findViewById(R.id.changeLangBtn);
//        signOutBtn = (Button) findViewById(R.id.signOutBtn);
//        back = findViewById(R.id.iv_back);

        text_sign_out = findViewById(R.id.text_sign_out);

        text_sign_out.setOnClickListener(this);


        text_done.setOnClickListener(this);
        mAlexaApp.setOnClickListener(this);
        changeLangBtn.setOnClickListener(this);
//        signOutBtn.setOnClickListener(this);
//        back.setOnClickListener(this);

        fromActivity = getIntent().getStringExtra("fromActivity");
        prevScreen = getIntent().getStringExtra("prevScreen");


//        if (fromActivity.equalsIgnoreCase(ConnectingToMainNetwork.class.getSimpleName())){
//            signOutBtn.setVisibility(View.GONE);
//        }

//        showLoader();
//        callMSearchEvery500ms(new Timer());

        mavidAlexaSkillEnablingViewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(MavidAlexaSkillEnablingViewModel.class);
    }

    public String getMsearchPayload() {
        String mSearchPayload = "M-SEARCH * HTTP/1.1\r\n" +
                "MX: 10\r\n" +
                "ST: urn:schemas-upnp-org:device:DDMSServer:1\r\n" +
                "HOST: 239.255.255.250:1800\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "\r\n";
        return mSearchPayload;
    }



//    public void callMSearchEvery500ms(final Timer timer){
//        TimerTask doAsynchronousTask = new TimerTask() {
//            @Override
//            public void run() {
//                LibreMavidHelper.setAdvertiserMessage(getMsearchPayload());
//                try {
//                    if (callMSearchfiveTimes < 5) {
//                        Log.d("atul_alexa_ip",speakerIpaddress);
//                        LibreMavidHelper.advertiseWithIp(speakerIpaddress,AlexaThingsToTryDoneActivity.this);
//                        callMSearchfiveTimes = callMSearchfiveTimes + 1;
//                        Log.d("atul_m-search", "count:" + String.valueOf(callMSearchfiveTimes));
//                    } else {
//                        closeLoader();
//                        timer.cancel();
//                    }
//                } catch (WrongStepCallException e) {
//                    e.printStackTrace();
//                    Log.d("atul_m-search_except", e.toString());
//                }
//            }
//        };
//        timer.schedule(doAsynchronousTask, 0, 500); //execute in every 500 ms
//    }
    public void launchTheApp(String appPackageName) {

        Intent intent = getPackageManager().getLaunchIntentForPackage(appPackageName);
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } else {

            redirectingToPlayStore(intent, appPackageName);

        }

    }


    public void redirectingToPlayStore(Intent intent, String appPackageName) {

        try {

            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + appPackageName));
            startActivity(intent);

        } catch (android.content.ActivityNotFoundException anfe) {

            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName));
            startActivity(intent);

        }

    }



    private void signOutOfAmazon() {
        //Signing out from amazon credentials
        LibreMavidHelper.amazonSignout(speakerIpaddress, new CommandStatusListener() {
            @Override
            public void failure(Exception e) {
                closeLoader();
            }

            @Override
            public void success() {
                closeLoader();

                intentToAlexaSigninActivity();
            }
        });
    }

    @Override
    public void onClick(View v) {
        String readAlexaRefreshToken = "READ_AlexaRefreshToken";

        int i = v.getId();
        if (i == R.id.changeLangBtn) {
            Intent newIntent = new Intent(AlexaThingsToTryDoneActivity.this, AlexaLangUpdateActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.putExtra("current_deviceip", speakerIpaddress);
            newIntent.putExtra("fromActivity", fromActivity);
            newIntent.putExtra("prevScreen", AlexaThingsToTryDoneActivity.class.getSimpleName());
            startActivity(newIntent);
            finish();

        } else if (i == R.id.text_sign_out) {
            showLoader();
            /** Disable the alex skill  */
            mavidAlexaSkillEnablingViewModel.getAlexaStatus(AlexaSkillStatusEnum.DISABLE.getStatus(), null, speakerIpaddress).observe(this,
                    new Observer<ModelAlexaSkillResponse>() {
                        @Override
                        public void onChanged(ModelAlexaSkillResponse modelAlexaSkillResponse) {
                            if (modelAlexaSkillResponse.getModelAlexaSkillResponseSucess() != null) {
                                if (modelAlexaSkillResponse.getModelAlexaSkillResponseSucess().getAlexaSkillStatus() == 1) {
                                    signOutOfAmazon();
                                } else {
                                    signOutOfAmazon();
                                    new UIRelatedClass().buildSnackBarWithoutButton(AlexaThingsToTryDoneActivity.this,
                                            getWindow().getDecorView().findViewById(android.R.id.content), "There seems to be an error while disabling the skill");
                                }
                            } else {
                                signOutOfAmazon();
                                new UIRelatedClass().buildSnackBarWithoutButton(AlexaThingsToTryDoneActivity.this,
                                        getWindow().getDecorView().findViewById(android.R.id.content), "There seems to be an error while disabling the skill");
                            }
                        }
                    });

        } else if (i == R.id.text_done) {
            startActivity(new Intent(AlexaThingsToTryDoneActivity.this, MavidHomeTabsActivity.class));
            finish();
        } else if (i == R.id.tv3) {
            launchTheApp("com.amazon.dee.app");

        }

    }

    private void intentToAlexaSigninActivity() {
        Intent newIntent = new Intent(AlexaThingsToTryDoneActivity.this, AlexaSignInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        newIntent.putExtra("speakerIpaddress", speakerIpaddress);
        newIntent.putExtra("fromActivity", AlexaThingsToTryDoneActivity.class.getSimpleName());
        startActivity(newIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AlexaThingsToTryDoneActivity.this, MavidHomeTabsActivity.class));
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
                    if (!(AlexaThingsToTryDoneActivity.this.isFinishing())) {
                        mDialog.dismiss();
                        mDialog = null;
                    }
                }
            }
        });
    }

    public void showLoader() {
        if (mDialog == null) {
            mDialog = new Dialog(AlexaThingsToTryDoneActivity.this);
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
                progress_title.setText(getString(R.string.notice));
                progress_message.setText(getString(R.string.pleaseWait));
                progress_bar.setIndeterminate(true);
                progress_bar.setVisibility(View.VISIBLE);
                if (!(AlexaThingsToTryDoneActivity.this.isFinishing())) {
                    mDialog.show();
                }
            }
        });
    }

//    private void showLoader() {
//        if (AlexaThingsToTryDoneActivity.this.isFinishing())
//            return;
//        if (m_progressDlg == null)
//            m_progressDlg = ProgressDialog.show(AlexaThingsToTryDoneActivity.this, getString(R.string.notice), getString(R.string.pleaseWait), true, true, null);
//        if (!m_progressDlg.isShowing()) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    m_progressDlg.show();
//                }
//            });
//        }
//    }
}
