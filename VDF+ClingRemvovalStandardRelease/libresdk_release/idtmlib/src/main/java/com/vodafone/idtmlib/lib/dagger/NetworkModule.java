package com.vodafone.idtmlib.lib.dagger;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.vodafone.idtmlib.lib.network.Environment;
import com.vodafone.idtmlib.lib.network.IdtmApi;
import com.vodafone.idtmlib.lib.network.TLSSocketFactory;
import com.vodafone.idtmlib.lib.network.models.Certificate;
import com.vodafone.idtmlib.lib.utils.Device;
import com.vodafone.idtmlib.lib.utils.Printer;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class NetworkModule {
    private String certPinningHashes;

    public NetworkModule(String certPinningHashes) {
        this.certPinningHashes = certPinningHashes;
    }

    @Singleton
    @Provides
    Cache provideCache(Context context) {
        return new Cache(context.getCacheDir(), 10 * 1024 * 1024);
    }

    @Singleton
    @Provides
    HttpLoggingInterceptor.Logger provideLogger(final Printer printer) {
        return new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                printer.i(message);
            }
        };
    }

    @Singleton
    @Provides
    @Named("logging")
    Interceptor provideLoggingInterceptor(HttpLoggingInterceptor.Logger logger) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(logger);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    @Singleton
    @Provides
    Converter.Factory provideGsonConverter() {
        return GsonConverterFactory.create();
    }

    @Singleton
    @Provides
    @Named("headers")
    Interceptor provideHeadersInterceptor(final Device device) {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                return chain.proceed(chain.request().newBuilder()
                        .addHeader("x-vf-log-level", "trace")
                        .addHeader("Accept", "application/json;charset=UTF-8")
                        .addHeader("User-Agent", device.getUserAgent())
                        .build());
            }
        };
    }

    @Singleton
    @Provides
    CertificatePinner provideCertificatePinner(Gson gson, Environment environment) {
        CertificatePinner.Builder certPinnerBuilder = new CertificatePinner.Builder();
        if (TextUtils.isEmpty(certPinningHashes)) {
            Certificate idtmCertificate = environment.getIdtmCertificate();
            if (idtmCertificate != null) {
                certPinnerBuilder.add(idtmCertificate.getDomain(), idtmCertificate.getHashes());
            }
        } else {
            Certificate[] certificates = gson.fromJson(certPinningHashes, Certificate[].class);
            if (certificates != null) {
                for (Certificate certificate : certificates) {
                    certPinnerBuilder.add(certificate.getDomain(), certificate.getHashes());
                }
            }
        }
        return certPinnerBuilder.build();
    }

    @Singleton
    @Provides
    OkHttpClient provideClient(Cache cache,
                               @Named("headers") Interceptor headersInterceptor,
                               @Named("logging") Interceptor loggingInterceptor,
                               CertificatePinner certificatePinner) {

        OkHttpClient client = null;
        long timeOutInSeconds = 60L; // time is in seconds
        try {

            // To support TLS 1.2 in API level 19, we have to apply out own SSLFactory that enables TLS 1.2 explicitely
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

            client =  new OkHttpClient.Builder()
                    .connectTimeout(timeOutInSeconds, TimeUnit.SECONDS)
                    .readTimeout(timeOutInSeconds, TimeUnit.SECONDS)
                    .writeTimeout(timeOutInSeconds, TimeUnit.SECONDS)
                    .cache(cache)
                    .certificatePinner(certificatePinner)
                    .addInterceptor(headersInterceptor)
                    .addInterceptor(loggingInterceptor)
                    .sslSocketFactory(new TLSSocketFactory(), trustManager)
                    .build();

        } catch (KeyStoreException | IllegalStateException | NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }

        return client;
    }
    @Singleton
    @Provides
    Retrofit provideRetrofit(OkHttpClient client, Converter.Factory gsonConverter,
                             Environment environment) {
        return new Retrofit.Builder()
                .baseUrl(environment.getIdtmApiUrl())
                .client(client)
                .addConverterFactory(gsonConverter)
                .build();
    }

    @Singleton
    @Provides
    IdtmApi provideIdtmApi(Retrofit retrofit) {
        return retrofit.create(IdtmApi.class);
    }
}
