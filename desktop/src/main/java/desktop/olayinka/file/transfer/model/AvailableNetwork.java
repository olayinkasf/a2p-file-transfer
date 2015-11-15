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

package desktop.olayinka.file.transfer.model;

import javax.swing.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by olayinka on 7/30/15.
 */
public class AvailableNetwork extends DefaultListModel<Object> {

    public static final String MAC_ERROR = "Couldn't get MAC address";
    public static final String REACHABLE = "Ready to connect";
    public static final String UNREACHABLE = REACHABLE;
    public static final String CONNECTED = "Connected";



    public static AvailableNetwork getAvailableNetwork() throws SocketException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        AvailableNetwork availableNetwork = new AvailableNetwork();
        int index = 0;
        for (NetworkInterface netInterface : Collections.list(nets))
            if (netInterface.isUp() && !netInterface.isLoopback() ) {
                Enumeration<InetAddress> inetAddresses = netInterface.getInetAddresses();
                boolean interfaceAdded = false;

                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                    if (!(inetAddress.isLoopbackAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isMulticastAddress())) {
                        if (!interfaceAdded) {
                            availableNetwork.add(index, netInterface);
                            index++;
                            interfaceAdded = true;
                        }
                        availableNetwork.add(index, inetAddress);
                        index++;
                    }
                }
            }
        return availableNetwork;
    }

    public NetworkInterface getNetworkInterface(int index) {
        Object networkInterface;
        while (!((networkInterface = get(index)) instanceof NetworkInterface)) {
            index--;
        }
        return (NetworkInterface) networkInterface;
    }
}
