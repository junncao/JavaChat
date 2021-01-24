package com.muc;

import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public interface MessageListener {
    public void onMessage(String fromLogin, byte[] msgBody) throws Exception;
}
