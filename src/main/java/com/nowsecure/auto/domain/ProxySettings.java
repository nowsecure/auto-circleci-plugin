package com.nowsecure.auto.domain;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.nowsecure.auto.utils.IOHelper;

public class ProxySettings implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String[] PROP_NAMES = new String[] { "http.proxyHost", "http.proxyPort", "https.proxyHost",
            "https.proxyPort", "http.proxyUser", "http.proxyPassword", "http.nonProxyHosts" };
    private String proxyServer = "";
    private int proxyPort;
    private String userName = "";
    private String proxyPass = "";
    private String noProxyHost = "";

    public String getProxyServer() {
        return proxyServer;
    }

    public void setProxyServer(String proxyServer) {
        if (proxyServer != null) {
            this.proxyServer = proxyServer;
        }
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (userName != null) {
            this.userName = userName;
        }
    }

    public String getProxyPass() {
        return proxyPass;
    }

    public void setProxyPass(String proxyPass) {
        if (proxyPass != null) {
            this.proxyPass = proxyPass;
        }
    }

    public String getNoProxyHost() {
        return noProxyHost;
    }

    public void setNoProxyHost(String noProxyHost) {
        if (noProxyHost != null) {
            this.noProxyHost = noProxyHost;
        }
    }

    public Map<String, String> getOldSettings(String... names) {
        Map<String, String> oldSettings = new HashMap<String, String>();
        for (String name : names) {
            oldSettings.put(name, System.getProperty(name));
        }
        return oldSettings;
    }

    public void restoreOldSettings(Map<String, String> map) {
        for (String name : PROP_NAMES) {
            Object value = map.get(name);
            if (value != null && value instanceof String && ((String) value).length() > 0) {
                System.setProperty(name, (String) value);
            } else {
                System.clearProperty(name);
            }
        }
    }

    public Map<String, String> overrideSystemProperties() {
        Map<String, String> oldSettings = getOldSettings(PROP_NAMES);
        if (!IOHelper.isEmpty(proxyServer) && proxyPort > 0) {
            System.setProperty("http.proxyHost", proxyServer.trim());
            System.setProperty("http.proxyPort", String.valueOf(proxyPort));
            System.setProperty("https.proxyHost", proxyServer.trim());
            System.setProperty("https.proxyPort", String.valueOf(proxyPort));
            if (!IOHelper.isEmpty(userName) && !IOHelper.isEmpty(proxyPass)) {
                System.setProperty("http.proxyUser", userName);
                System.setProperty("http.proxyPassword", proxyPass);
            }
            if (!IOHelper.isEmpty(noProxyHost)) {
                System.setProperty("http.nonProxyHosts", noProxyHost);
            }
        }
        return oldSettings;
    }

    public void validate(String prefix) throws IOException {
        if (!IOHelper.isEmpty(proxyServer) && proxyPort > 0) {
            try {
                InetAddress.getByName(proxyServer);
            } catch (Exception e) {
                throw new IOException(prefix + " Failed to lookup proxy server " + proxyServer + " due to " + e);
            }
            //
            try {
                Socket socket = new Socket(proxyServer, proxyPort);
                socket.close();
            } catch (Exception e) {
                throw new IOException(prefix + " Failed to connect to proxy server " + proxyServer + " due to " + e);
            }
        }
    }

    @Override
    public String toString() {
        if (!IOHelper.isEmpty(proxyServer) && proxyPort > 0) {
            return "[proxyServer=" + proxyServer + ", proxyPort=" + proxyPort + ", userName=" + userName
                   + ", noProxyHost=" + noProxyHost + "]";
        } else {
            return "[NONE]";
        }
    }

}
