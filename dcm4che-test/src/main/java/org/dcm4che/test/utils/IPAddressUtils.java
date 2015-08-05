package org.dcm4che.test.utils;

import java.net.*;
import java.util.Enumeration;

/**
 * Created by umberto on 8/5/15.
 */
public class IPAddressUtils {

    public static String returnLocalIPv4 () throws SocketException {

        for (final Enumeration< NetworkInterface > interfaces =
             NetworkInterface.getNetworkInterfaces( );
             interfaces.hasMoreElements( );) {

            final NetworkInterface ni = interfaces.nextElement( );

            if (ni.isLoopback())
                continue;

            for (final InterfaceAddress addr : ni.getInterfaceAddresses()) {
                final InetAddress inet_addr = addr.getAddress();

                if (!(inet_addr instanceof Inet4Address))
                    continue;

                return inet_addr.getHostAddress();
            }
        }
        return null;
    }

    public static void main(String[] args) throws SocketException{
        System.out.println(IPAddressUtils.returnLocalIPv4());
    }
}
