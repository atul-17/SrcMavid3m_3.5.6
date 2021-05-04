package com.vodafone.idtmlib.lib.ui.dagger;

import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

import com.vodafone.idtmlib.lib.utils.Printer;

import dagger.Module;
import dagger.Provides;

@Module
public class IdGatewayActivityModule {
    public static final String BASE_LOG = "webview - ";

    @Provides
    ViewGroup.LayoutParams provideLayoutParams() {
        return new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Provides
    WebChromeClient provideWebChromeClient(final Printer printer) {
        return new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                switch (consoleMessage.messageLevel()) {
                    case DEBUG:
                        printer.d(BASE_LOG, consoleMessage.message());
                        break;
                    case ERROR:
                        printer.e(BASE_LOG, consoleMessage.message());
                        break;
                    case WARNING:
                        printer.w(BASE_LOG, consoleMessage.message());
                        break;
                    default:
                        printer.i(BASE_LOG, consoleMessage.message());
                }
                return true;
            }
        };
    }
}
