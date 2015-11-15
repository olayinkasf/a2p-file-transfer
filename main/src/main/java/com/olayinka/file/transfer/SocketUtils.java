package com.olayinka.file.transfer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Olayinka on 10/17/2015.
 */
public class SocketUtils {

    //read string length (int), string (byte[])
    public static String readStringData(DataInputStream mInputStream) throws IOException {
        int nameSize = mInputStream.readInt();
        byte[] nameBuffer = new byte[nameSize];
        int result;
        {
            int read = 0;
            while (read < nameSize) {
                result = mInputStream.read(nameBuffer, read, nameSize - read);
                read += result;
            }
        }
        return new String(nameBuffer);
    }


    public static void writeStringData(DataOutputStream mDataOutputStream, String token) throws IOException {
        byte[] name = token.getBytes();
        mDataOutputStream.write(ByteBuffer.allocate(4).putInt(name.length).array());
        mDataOutputStream.flush();
        mDataOutputStream.write(name);
        mDataOutputStream.flush();
    }

}
