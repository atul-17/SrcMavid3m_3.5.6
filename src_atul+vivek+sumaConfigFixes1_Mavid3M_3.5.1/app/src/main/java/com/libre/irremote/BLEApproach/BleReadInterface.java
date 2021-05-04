package com.libre.irremote.BLEApproach;

import java.io.IOException;

public interface BleReadInterface {

    public void onReadSuccess(byte[] data) throws IOException;
}
