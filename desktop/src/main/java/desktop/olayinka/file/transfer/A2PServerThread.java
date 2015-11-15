package desktop.olayinka.file.transfer;

import com.olayinka.file.transfer.A2PClient;
import com.olayinka.file.transfer.A2PServer;
import com.olayinka.file.transfer.A2PServerListener;

/**
 * Created by Olayinka on 11/14/2015.
 */
public class A2PServerThread implements Runnable, A2PServer.Interface {

    A2PServer a2PServer;

    public A2PServerThread(A2PServerListener... listener) {
        a2PServer = new A2PServer(listener) {
            @Override
            protected void startClient(final A2PClient a2PClient) {
                new Thread(new A2PClientThread(a2PClient)).start();
            }
        };
    }

    @Override
    public void run() {
        a2PServer.run();
    }

    public void setListenerProvider(A2PClient.ListenerProvider mListenerView) {
        a2PServer.setListenerProvider(mListenerView);
    }

    public void disconnect() {
        a2PServer.disconnect();
    }

    public boolean isActive() {
        return a2PServer.isActive();
    }
}
