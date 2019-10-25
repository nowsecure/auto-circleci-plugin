package com.nowsecure.auto;

import java.net.InetAddress;
import java.net.URL;

public class DnsUrlCheck {
    private static final String DEFAULT_URL = "https://lab-api.nowsecure.com";

    public static void main(String[] args) throws Exception {
        String urlStr = DEFAULT_URL;
        if (args.length > 0) {
            urlStr = args[0];
        }
        System.out.println("Cheking URL " + urlStr);
        System.out.println("System config " + System.getProperties());
        URL url = new URL(urlStr);
        InetAddress.getByName(url.getHost());
        System.out.println("succeeded in checking DNS for host " + url.getHost());
    }
}
