package com.seetong5.app.seetong.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2014-06-25.
 */
public class MD5 {
    public String fromFile(String fileName) {
        File file = new File(fileName);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            MappedByteBuffer byteBuffer = fis.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            return fromBuffer(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fis) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    public String fromBuffer(ByteBuffer bytes) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(bytes);
            BigInteger bi = new BigInteger(1, md5.digest());
            return bi.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }
}
