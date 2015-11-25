/*
 * Copyright 2015
 *
 *     Olayinka S. Folorunso <mail@olayinkasf.com>
 *     http://olayinkasf.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olayinka.file.transfer;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by Olayinka on 8/5/2015.
 */


public class FileTransfer {

    public FileTransfer(DataInputStream mInputStream, DataOutputStream mOutputStream) {
        this.mInputStream = mInputStream;
        this.mOutputStream = mOutputStream;
    }

    DataInputStream mInputStream;
    DataOutputStream mOutputStream;

    public void initiateTransfer(FileTransferListener listener) throws IOException, DirectoryAccessException {
        try {
            final byte[] buffer = new byte[A2PClient.MAX_BYTES];

            //read index (int)
            int index = mInputStream.readInt();
            String name = SocketUtils.readStringData(mInputStream);

            LOG.w("readData", "Started reading data for file: " + name);

            File outputFile = new File(listener.getSaveDirectory(), name);
            listener.registerFileName(outputFile.getAbsolutePath());
            if (!outputFile.getParentFile().exists()) {
                if (!outputFile.getParentFile().mkdirs())
                    throw new SecurityException();
            }

            //read  file size (long)
            long maxBytesAvailable = mInputStream.readLong();

            listener.registerFileSize(maxBytesAvailable);

            long readData = 0;
            DataOutputStream fileOutputStream = new DataOutputStream(new FileOutputStream(outputFile));

            mOutputStream.write(new byte[]{-1});
            mOutputStream.flush();
            mOutputStream.write(ByteBuffer.allocate(4).putInt(index).array());
            mOutputStream.flush();

            int result;

            while (readData < maxBytesAvailable) {
                result = mInputStream.read(buffer, 0, (int) Math.min(maxBytesAvailable - readData, A2PClient.MAX_BYTES));
                if (result == -1) {
                    throw new IOException("Error reading header data");
                }

                fileOutputStream.write(buffer, 0, result);
                fileOutputStream.flush();
                readData += result;

                byte rate = (byte) ((readData * 100) / maxBytesAvailable);
                LOG.w("readData", "Successfully read: " + readData + " at " + rate + "%");
                listener.registerProgress(readData);

                mOutputStream.write(new byte[]{rate});
                mOutputStream.flush();
            }

            mOutputStream.write(new byte[]{-2});
            mOutputStream.flush();

            fileOutputStream.flush();
            fileOutputStream.close();

            LOG.w("readData", "Read data of size " + readData + " bytes from " + maxBytesAvailable + " bytes");

            listener.registerFinished();

        } catch (SecurityException e) {
            listener.registerErrorMessage("Can't create or access destination folder!");
            listener.registerFinished();
            throw new DirectoryAccessException("Can't create or access destination folder!");
        } catch (IOException e) {
            listener.registerFinished();
            throw e;
        }
    }

    public interface FileTransferListener {
        /**
         * @param name of the file
         */
        void registerFileName(String name);

        /**
         * @param maxBytesAvailable maximum size of the expected file
         */
        void registerFileSize(long maxBytesAvailable);

        /**
         * @param readData size of the file already read
         */
        void registerProgress(long readData);

        /**
         * @param message message in case of a failure
         */
        void registerErrorMessage(String message);

        /**
         * Register termination of file transfer
         */
        void registerFinished();

        File getSaveDirectory();
    }

}