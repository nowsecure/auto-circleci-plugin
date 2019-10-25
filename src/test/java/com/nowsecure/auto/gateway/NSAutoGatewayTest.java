package com.nowsecure.auto.gateway;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nowsecure.auto.domain.AssessmentRequest;
import com.nowsecure.auto.domain.Color;
import com.nowsecure.auto.domain.NSAutoLogger;
import com.nowsecure.auto.domain.NSAutoParameters;
import com.nowsecure.auto.domain.ProxySettings;
import com.nowsecure.auto.domain.ScoreInfo;
import com.nowsecure.auto.domain.UploadRequest;
import com.nowsecure.auto.utils.IOHelper;
import com.nowsecure.auto.utils.IOHelperI;

public class NSAutoGatewayTest implements NSAutoParameters, NSAutoLogger, IOHelperI {
    private String apiUrl = "https://lab-api.nowsecure.com";
    private String group = "good";
    private File artifactsDir = new File("/tmp");
    private File file = new File("/tmp/blah");
    private int waitMinutes = 30;
    private int scoreThreshold = 50;
    private String apiKey = "mykey";
    private String description = "blah";
    private String username;
    private String password;
    private boolean showStatusMessages;
    private String stopTestsForStatusMessage;
    private boolean debug;

    private List<String> stdout = new ArrayList<String>();
    private List<String> stderr = new ArrayList<String>();
    private NSAutoGateway gw;
    private String exceptionType = "IOException";
    private ProxySettings proxySettings = new ProxySettings();

    @Before
    public void setup() throws Exception {
        NSAutoGateway.FIFTEEN_SECONDS = 1;
        gw = new NSAutoGateway(this, this, this) {
            @Override
            UploadRequest uploadBinary() throws IOException, ParseException {
                if (exceptionType.equals("ParseException")) {
                    throw new ParseException(1);
                }
                return super.uploadBinary();
            }

            @Override
            ScoreInfo getScoreInfo(AssessmentRequest uploadInfo) throws ParseException, IOException {
                if (exceptionType.equals("timeout")) {
                    return null;
                }
                return super.getScoreInfo(uploadInfo);
            }
        };
    }

    @Test
    public void testToString() throws Exception {
        Assert.assertNotNull(gw.toString());
        apiKey = null;
        Assert.assertNotNull(gw.toString());
        apiKey = "";
        Assert.assertNotNull(gw.toString());
        apiKey = "1234";
        Assert.assertNotNull(gw.toString());
    }

    @Test
    public void testExecuteWithWait() throws Exception {
        exceptionType = "";
        waitMinutes = 30;
        gw.execute(true);
    }

    @Test(expected = IOException.class)
    public void testExecuteWithLowScore() throws Exception {
        waitMinutes = 30;
        scoreThreshold = 80;
        gw.execute(true);
    }

    @Test(expected = IOException.class)
    public void testExecuteWithTimeout() throws Exception {
        exceptionType = "timeout";
        waitMinutes = 1;
        gw.execute(true);
    }

    @Test
    public void testExecuteWithoutWait() throws Exception {
        waitMinutes = 0;
        gw.execute(true);
    }

    @Test(expected = IOException.class)
    public void testExecuteIOException() throws Exception {
        exceptionType = "IOException";
        group = "bad";
        gw.execute(false);
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteRTException() throws Exception {
        exceptionType = "RuntimeException";
        group = "bad";
        gw.execute(false);
    }

    @Test(expected = IOException.class)
    public void testExecuteParseException() throws Exception {
        exceptionType = "ParseException";
        group = "bad";
        gw.execute(false);
    }

    @Test(expected = IOException.class)
    public void testExecutePreflightError() throws Exception {
        group = "preflight-error";
        gw.execute(false);
    }

    @Test(expected = IOException.class)
    public void testPreflightError() throws Exception {
        group = "preflight-error";
        gw.preflight(new UploadRequest());
    }

    @Test
    public void testEmptyScore() throws Exception {
        group = "empty-score";
        Assert.assertNull(gw.getScoreInfo(new AssessmentRequest()));
    }

    @Test
    public void testGetElapsedMinutes() throws Exception {
        Assert.assertNotNull(gw.getElapsedMinutes(100));
    }

    @Test
    public void testWaitForResultsAndShowMessages() throws Exception {
        showStatusMessages = true;
        AssessmentRequest req = new AssessmentRequest();
        req.setTask(101L);
        req.setPackageId("com.apkpure.aegon");
        req.setPlatform("android");

        gw.waitForResults(req);
    }

    @Test
    public void testWaitForResults() throws Exception {
        AssessmentRequest req = new AssessmentRequest();
        req.setTask(101L);
        req.setPackageId("com.apkpure.aegon");
        req.setPlatform("android");

        gw.waitForResults(req);
    }

    @Test
    public void testBuildUrl() throws Exception {
        Assert.assertEquals("https://nowsecure.com:443/path?group=group",
                NSAutoGateway.buildUrl("/path", new URL("https://nowsecure.com:443"), "group"));
        Assert.assertEquals("https://nowsecure.com:443/path",
                NSAutoGateway.buildUrl("/path", new URL("https://nowsecure.com:443"), ""));
        Assert.assertEquals("https://nowsecure.com:443/path",
                NSAutoGateway.buildUrl("/path", new URL("https://nowsecure.com:443"), null));
    }

    @Test
    public void testGetArtifactContents() throws Exception {
        File file = File.createTempFile("pre", "suf");
        gw.artifacts.add(file);
        Map<String, String> map = gw.getArtifactContents(false);
        Assert.assertEquals(1, map.size());
        map = gw.getArtifactContents(true);
        Assert.assertEquals(1, map.size());
    }
    /////

    @Override
    public void info(String msg) {
        stdout.add(msg);
    }

    @Override
    public void info(String msg, Color color) {
        stdout.add(msg);
    }

    @Override
    public void error(String msg) {
        stderr.add(msg);
    }

    @Override
    public void debug(String msg) {
        stdout.add(msg);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getApiUrl() {
        return apiUrl;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public File getArtifactsDir() {
        return artifactsDir;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public int getWaitMinutes() {
        return waitMinutes;
    }

    @Override
    public int getScoreThreshold() {
        return scoreThreshold;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean isShowStatusMessages() {
        return showStatusMessages;
    }

    public void setShowStatusMessages(boolean showStatusMessages) {
        this.showStatusMessages = showStatusMessages;
    }

    @Override
    public String getStopTestsForStatusMessage() {
        return stopTestsForStatusMessage;
    }

    public void setStopTestsForStatusMessage(String stopTestsForStatusMessage) {
        this.stopTestsForStatusMessage = stopTestsForStatusMessage;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public ProxySettings getProxySettings() {
        return proxySettings;
    }

    public void setProxySettings(ProxySettings proxySettings) {
        this.proxySettings = proxySettings;
    }

    @Override
    public void save(File path, String contents) throws IOException {
        path.getParentFile().mkdirs();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
        writer.write(contents.trim());
        writer.close();

    }

    @Override
    public String get(String uri, String apiKey) throws IOException {
        if (uri.equals("https://lab-api.nowsecure.com/binary/mydigest/analysis?group=good")) {
            return new String(new IOHelper("test", 1).load(getClass().getResourceAsStream("/upload.json")));
        } else if (uri.equals("https://lab-api.nowsecure.com/assessment/101/summary?group=good")) {
            return new String(new IOHelper("test", 1).load(getClass().getResourceAsStream("/score.json")));
        } else if (uri.equals(
                "https://lab-api.nowsecure.com/app/android/com.apkpure.aegon/assessment/101/results?group=good")) {
            return new String(new IOHelper("test", 1).load(getClass().getResourceAsStream("/report.json")));
        } else if (uri.equals("https://lab-api.nowsecure.com/assessment/null/summary?group=empty-score")) {
            return "";
        } else if (uri.equals("https://lab-api.nowsecure.com/analysis-events/101/dynamic/?group=good")) {
            return "status";
        } else {
            return throwException("Bad GET " + uri);
        }
    }

    @Override
    public String post(String uri, String apiKey) throws IOException {
        if (uri.equals("https://lab-api.nowsecure.com/app/android/com.apkpure.aegon/assessment/?group=good")) {
            return new String(new IOHelper("test", 1).load(getClass().getResourceAsStream("/trigger.json")));
        } else {
            return throwException("Bad POST " + uri);
        }
    }

    @Override
    public String upload(String uri, String apiKey, File file) throws IOException {
        if (uri.equals("https://lab-api.nowsecure.com/binary/?group=good")) {
            return new String(new IOHelper("test", 1).load(getClass().getResourceAsStream("/binary.json")));
        } else if (uri.equals("https://lab-api.nowsecure.com/binary/?group=preflight-error")) {
            return new String(new IOHelper("test", 1).load(getClass().getResourceAsStream("/error.json")));
        } else {
            return throwException("UPLOAD " + uri);
        }
    }

    private String throwException(String msg) throws IOException {
        if (exceptionType.equals("IOException")) {
            throw new IOException(msg);
        } else {
            throw new RuntimeException(msg);
        }

    }

    @Override
    public byte[] load(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Could not find file " + file);
        }
        return Files.readAllBytes(Paths.get(file.getAbsolutePath()));
    }

    @Override
    public boolean isProxyEnabled() {
        return false;
    }

    @Override
    public boolean isValidateDnsUrlConnectionEnabled() {
        return true;
    }

}
