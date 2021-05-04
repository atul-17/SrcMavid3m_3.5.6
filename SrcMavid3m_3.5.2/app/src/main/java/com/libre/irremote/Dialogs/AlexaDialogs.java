package com.libre.irremote.Dialogs;

import android.app.Dialog;
import android.content.Context;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.view.View;
import android.view.Window;

import com.libre.irremote.R;

/**
 * Created by bhargav on 8/2/18.
 */

public class AlexaDialogs {

    public static Dialog alert;


    public static void somethingWentWrong(Context context) {
        try {
            alert = null;

            alert = new Dialog(context);

            alert.requestWindowFeature(Window.FEATURE_NO_TITLE);

            alert.setContentView(R.layout.custom_single_button_layout);

            alert.setCancelable(false);

            AppCompatTextView tv_alert_title = alert.findViewById(R.id.tv_alert_title);

            AppCompatTextView tv_alert_message = alert.findViewById(R.id.tv_alert_message);

            AppCompatButton btn_ok = alert.findViewById(R.id.btn_ok);

            tv_alert_title.setText("");

            tv_alert_message.setText(context.getResources().getString(R.string.somethingWentWrong));

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alert.dismiss();
                    alert = null;
                }
            });

            alert.show();

        } catch (Exception e) {

        }
    }
}
