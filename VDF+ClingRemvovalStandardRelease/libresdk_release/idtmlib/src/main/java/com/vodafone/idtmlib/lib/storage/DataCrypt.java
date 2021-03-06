package com.vodafone.idtmlib.lib.storage;

import android.os.Build;
import android.os.Looper;
import androidx.annotation.WorkerThread;
import android.text.TextUtils;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.vodafone.idtmlib.lib.network.IdtmApi;
import com.vodafone.idtmlib.lib.storage.basic.Aes;
import com.vodafone.idtmlib.lib.storage.basic.AesException;
import com.vodafone.idtmlib.lib.storage.basic.AndroidKeyStore;
import com.vodafone.idtmlib.lib.storage.basic.Preferences;
import com.vodafone.idtmlib.lib.storage.basic.WrongPasswordException;
import com.vodafone.idtmlib.lib.utils.Device;
import com.vodafone.idtmlib.lib.utils.Printer;

import org.json.JSONException;
import org.json.JSONObject;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DataCrypt {
    private Printer printer;
    private Preferences preferences;
    private Device device;
    private IdtmApi idtmApi;
//    private EventWaiter<RefreshCompletePushMessageEvent> refreshCompleteEventWaiter;

    @Inject
    public DataCrypt(Printer printer, Preferences preferences,
                     Device device, IdtmApi idtmApi) {
        this.printer = printer;
        this.preferences = preferences;
        this.device = device;
        this.idtmApi = idtmApi;
    }

    private String getAutogeneratedPassword() {
        return Hashing.sha256().hashString(device.getAndroidUuid(), Charsets.UTF_8).toString();
    }

    public void set(String name, boolean value) throws AesException {
        set(name, String.valueOf(value));
    }

    public boolean getBoolean(String name, boolean defaultValue) throws AesException {
        String value = getString(name);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    public void set(String name, String value) throws AesException {
        if (value == null) {
            preferences.remove(name);
        } else {
            String encryptedValue;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                SecretKey aesKey = AndroidKeyStore.getAesKey(Keys.PREFERENCES);
                encryptedValue = Aes.encrypt(value, aesKey);
            } else {
                encryptedValue = Aes.encrypt(value, getAutogeneratedPassword());
            }
            preferences.set(name, encryptedValue);
        }
    }

    public String getString(String name) throws AesException {
        String encryptedValue = preferences.getString(name);
        if (TextUtils.isEmpty(encryptedValue)) {
            return encryptedValue;
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                SecretKey aesKey = AndroidKeyStore.getAesKey(Keys.PREFERENCES);
                return Aes.decrypt(encryptedValue, aesKey);
            } else {
                try {
                    return Aes.decrypt(encryptedValue, getAutogeneratedPassword());
                } catch (WrongPasswordException e) {
                    // should never happen
                    return null;
                }
            }
        }
    }

    public void set(String name, long value) throws AesException {
        set(name, String.valueOf(value));
    }

    public long getLong(String name, long defaultValue) throws AesException {
        String value = getString(name);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        return Long.parseLong(value);
    }

    public String saveServerSymmetricKey(String symmetricKey) throws JSONException, AesException {
        JSONObject publicKeyJobj = new JSONObject(symmetricKey);
        String kty = publicKeyJobj.getString("kty");
        String k = publicKeyJobj.getString("k");
        if (!TextUtils.equals("oct", kty)) {
            throw new JSONException("Unsupported key type (" + kty + ")");
        }
        set(Prefs.SERVER_SYMMETRIC_KEY, k);
        return k; // returns the aes key in the json, ready to be used
    }

    public SecretKey loadServerSymmetricKey() throws AesException {
        String keyStr = null;
        // check and request the new symmetric key only if we are running on a worker thread,
        // otherwise the client will use the expired key
        if (Looper.myLooper() != Looper.getMainLooper()) {
            long expireDate = getLong(Prefs.SERVER_SYMMETRIC_EXPIRE_TIME_MS, Long.MAX_VALUE);
            if (System.currentTimeMillis() > expireDate) {
                keyStr = updateSymmetricKey();
            }
        }
        // if the symmetric key was just refreshed and saved, no need to reload it from the Prefs
        if (TextUtils.isEmpty(keyStr)) {
            keyStr = getString(Prefs.SERVER_SYMMETRIC_KEY);
        }
        return Aes.create(keyStr);
    }

    @WorkerThread
    private String updateSymmetricKey() {
//        eventBus.register(this);
//        refreshCompleteEventWaiter = new EventWaiter<>();
//        try {
//            printer.i("Updating symmetric key on the server");
//            RSAPublicKey rsaPublicKey = loadPublicKey();
//            String vfDeviceId = preferences.getString(Prefs.VF_DEVICE_ID);
//            String refreshState = preferences.getString(Prefs.SERVER_SYMMETRIC_REFRESH_STATE);
//            JSONObject mainJobj = new JSONObject();
//            try {
//                mainJobj.put("vf-device-id", vfDeviceId);
//                mainJobj.put("refresh-state", refreshState);
//            } catch (JSONException e) { /* shouldn't happen */
//                printer.w(e);
//            }
//            EncryptedJWT encryptedJWT = JwtHelper.rsaEncrypt(rsaPublicKey, mainJobj.toString());
//            JwtBody body = new JwtBody(encryptedJWT.serialize());
//            refreshCompleteEventWaiter.ready();
//            JwtResponse jwtResponse = Response.retrieve(JwtResponse.class,
//                    vfidApi.refreshSymmetricKey(vfDeviceId, body));
//            // waits for push message with the public key
//            RefreshCompletePushMessageEvent refreshCompletePushMessageEvent = refreshCompleteEventWaiter.await(BuildConfig.PUSH_MESSAGE_TIMEOUT_SECS);
//            if (refreshCompletePushMessageEvent == null) {
//                throw new PushTimeoutException();
//            }
//            // validate refresh response against the push message
//            if (!TextUtils.equals(jwtResponse.getCode(), refreshCompletePushMessageEvent.getHashedServerKey())) {
//                throw new SecureCommunicationException();
//            }
//            // store the encrypted symmetric key along with the new expire values
//            StartSetupCompletePnMessage message = refreshCompletePushMessageEvent.getPnMessage();
//            if (message != null) {
//                return saveSymmetric(message.getSymmetricKey(), message.getKeyExpDate(), message.getRefreshState());
//            }
//        } catch (Exception e) {
//            printer.e(e);
//        } finally {
//            eventBus.unregister(this);
//        }
        return null;
    }

//    @Subscribe
//    public void refreshCompletePushMessage(RefreshCompletePushMessageEvent refreshCompletePushMessageEvent) {
//        refreshCompleteEventWaiter.received(refreshCompletePushMessageEvent);
//    }
}
