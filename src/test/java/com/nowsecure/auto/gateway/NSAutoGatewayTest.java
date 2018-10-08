package com.nowsecure.auto.gateway;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.nowsecure.auto.domain.AssessmentRequest;
import com.nowsecure.auto.domain.NSAutoLogger;
import com.nowsecure.auto.domain.NSAutoParameters;
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
    private List<String> stdout = new ArrayList<>();
    private List<String> stderr = new ArrayList<>();
    private NSAutoGateway gw = new NSAutoGateway(this, this, this) {
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
    private String exceptionType = "IOException";

    @Before
    public void setup() {
        NSAutoGateway.POLL_INTERVAL = 1;
    }

    @Test
    public void testExecuteWithWait() throws Exception {
        waitMinutes = 30;
        gw.execute();
    }

    @Test(expected = IOException.class)
    public void testExecuteWithLowScore() throws Exception {
        waitMinutes = 30;
        scoreThreshold = 80;
        gw.execute();
    }

    @Test(expected = IOException.class)
    public void testExecuteWithTimeout() throws Exception {
        exceptionType = "timeout";
        waitMinutes = 1;
        gw.execute();
    }

    @Test
    public void testExecuteWithoutWait() throws Exception {
        waitMinutes = 0;
        gw.execute();
    }

    @Test(expected = IOException.class)
    public void testExecuteIOException() throws Exception {
        exceptionType = "IOException";
        group = "bad";
        gw.execute();
    }

    @Test(expected = RuntimeException.class)
    public void testExecuteRTException() throws Exception {
        exceptionType = "RuntimeException";
        group = "bad";
        gw.execute();
    }

    @Test(expected = IOException.class)
    public void testExecuteParseException() throws Exception {
        exceptionType = "ParseException";
        group = "bad";
        gw.execute();
    }

    @Test(expected = IOException.class)
    public void testExecutePreflightError() throws Exception {
        group = "preflight-error";
        gw.execute();
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
    public void testBuildUrl() throws Exception {
        Assert.assertEquals("https://nowsecure.com:443/path?group=group",
                NSAutoGateway.buildUrl("/path", new URL("https://nowsecure.com:443"), "group"));
        Assert.assertEquals("https://nowsecure.com:443/path",
                NSAutoGateway.buildUrl("/path", new URL("https://nowsecure.com:443"), ""));
        Assert.assertEquals("https://nowsecure.com:443/path",
                NSAutoGateway.buildUrl("/path", new URL("https://nowsecure.com:443"), null));
    }

    /////

    @Override
    public void info(String msg) {
        stdout.add(msg);
    }

    @Override
    public void error(String msg) {
        stderr.add(msg);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getApiUrl() {
        // TODO Auto-generated method stub
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
    public void save(String path, String contents) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write(contents.trim());
        }
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
        } else {
            return throwException();
        }
    }

    @Override
    public String post(String uri, String apiKey) throws IOException {
        if (uri.equals("https://lab-api.nowsecure.com/app/android/com.apkpure.aegon/assessment/?group=good")) {
            return new String(new IOHelper("test", 1).load(getClass().getResourceAsStream("/trigger.json")));
        } else {
            return throwException();
        }
    }

    @Override
    public String upload(String uri, String apiKey, String file) throws IOException {
        if (uri.equals("https://lab-api.nowsecure.com/binary/?group=good")) {
            return new String(new IOHelper("test", 1).load(getClass().getResourceAsStream("/binary.json")));
        } else if (uri.equals("https://lab-api.nowsecure.com/binary/?group=preflight-error")) {
            return new String(new IOHelper("test", 1).load(getClass().getResourceAsStream("/error.json")));
        } else {
            return throwException();
        }
    }

    private String throwException() throws IOException {
        if (exceptionType.equals("IOException")) {
            throw new IOException();
        } else {
            throw new RuntimeException();
        }

    }
}
