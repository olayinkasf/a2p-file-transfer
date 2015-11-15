package desktop.olayinka.file.transfer;

import com.olayinka.file.transfer.A2PClient;

/**
 * Created by Olayinka on 11/14/2015.
 */
public class A2PClientThread implements Runnable {

    private final A2PClient a2PClient;

    public A2PClientThread(A2PClient a2PClient) {
        this.a2PClient = a2PClient;
    }

    @Override
    public void run() {
        a2PClient.run();
    }
}