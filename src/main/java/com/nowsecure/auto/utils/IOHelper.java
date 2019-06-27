package com.nowsecure.auto.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.BiPredicate;

public class IOHelper implements IOHelperI {
    private static final String CONTENT_DIGEST = "Content-Digest";
    private static final String CONTENT_LENGTH = "Content-Length";
    static String VERSION_TXT = "/version.txt";
    static String JVERSION_TXT = "/jversion.txt";
    private static final String USER_AGENT = "User-Agent";
    private static final String GET = "GET";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String AUTHORIZATION = "Authorization";
    private static final String POST = "POST";
    private String pluginName;
    private int timeout;

    public IOHelper(String pluginName, int timeout) {
        this.pluginName = pluginName;
        this.timeout = timeout;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static String getLocalHost() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    public byte[] load(File file) throws IOException {
        return _load(file);
    }

    public static String getVersion() {
        try {
            InputStream in = IOHelper.class.getResourceAsStream(JVERSION_TXT);
            if (in == null) {
                in = IOHelper.class.getResourceAsStream(VERSION_TXT);
            }
            Scanner scanner = new Scanner(in, "UTF-8");
            String version = scanner.next();
            scanner.close();
            in.close();
            return version;
        } catch (RuntimeException e) {
            return "1.0.0";
        } catch (IOException e) {
            return "1.0.0";
        }
    }

    public File find(final File parent, final File file) throws IOException {
        if (file.isFile() && file.exists()) {
            return file;
        }
        if (!parent.exists()) {
            return file;
        }
        Optional<Path> matched = Files
                .find(Paths.get(parent.getCanonicalPath()), 10, new BiPredicate<Path, BasicFileAttributes>() {
                    @Override
                    public boolean test(Path t, BasicFileAttributes u) {
                        return t.toString().endsWith(file.getName());
                    }
                }, FileVisitOption.FOLLOW_LINKS).distinct().findFirst();
        if (matched.isPresent()) {
            return matched.get().toFile();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.utils.IOHelperI#save(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void save(File file, String contents) throws IOException {
        file.getParentFile().mkdirs();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
        writer.write(contents.trim());
        writer.close();
    }

    public byte[] load(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.utils.IOHelperI#get(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String get(String uri, String apiKey) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(GET);
        initConnection(apiKey, con);
        InputStream in = con.getInputStream();
        String json = new String(load(in), StandardCharsets.UTF_8);
        in.close();
        con.disconnect();
        return json.trim();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.utils.IOHelperI#post(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String post(String uri, String apiKey) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(POST);
        initConnection(apiKey, con);
        InputStream in = con.getInputStream();
        String json = new String(load(in), StandardCharsets.UTF_8);
        in.close();
        con.disconnect();
        return json;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.utils.IOHelperI#upload(java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public String upload(String uri, String apiKey, File file) throws IOException {
        byte[] binary = load(file);
        URL url = new URL(uri);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(POST);
        initConnection(apiKey, con);
        con.setRequestProperty(CONTENT_LENGTH, String.valueOf(binary.length));
        con.setRequestProperty(CONTENT_DIGEST, toDigest(binary, "SHA-256"));
        con.setDoOutput(true);
        con.connect();
        // Check http code first
        int code = con.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Failed to connect to " + uri + " to upload " + file + " due to HTTP status " + code + ", message " + con.getResponseMessage() + " -- " + con.getContent());
        }
        OutputStream out = con.getOutputStream();
        out.write(binary);
        out.flush();
        out.close();
        InputStream in = con.getInputStream();
        String json = new String(load(in), StandardCharsets.UTF_8);
        in.close();
        con.disconnect();
        return json;
    }

    private void initConnection(String apiKey, HttpURLConnection con) {
        con.setRequestProperty(CONTENT_TYPE, "application/json");
        con.setRequestProperty(AUTHORIZATION, "Bearer " + apiKey);
        con.setRequestProperty(USER_AGENT, pluginName + " v" + getVersion());
        con.setConnectTimeout(timeout);
        con.setReadTimeout(timeout);
        con.setInstanceFollowRedirects(false);
    }

    public static String toDigest(File file, String algorithm) {
        try {
            return toDigest(_load(file), algorithm);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String toDigest(byte[] b, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            return algorithm + "=" + byteArrayToHexString(md.digest(b), b.length);
        } catch (Exception e) {
            return "B16=" + byteArrayToHexString(b, 16);
        }
    }

    private static String byteArrayToHexString(byte[] b, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length && i < max; i++) {
            sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    private static byte[] _load(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find file " + file);
        }
        return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
    }
}
