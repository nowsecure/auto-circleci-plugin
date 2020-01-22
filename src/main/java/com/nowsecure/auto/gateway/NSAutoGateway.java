package com.nowsecure.auto.gateway;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.parser.ParseException;

import com.nowsecure.auto.domain.AssessmentRequest;
import com.nowsecure.auto.domain.Color;
import com.nowsecure.auto.domain.Message;
import com.nowsecure.auto.domain.NSAutoLogger;
import com.nowsecure.auto.domain.NSAutoParameters;
import com.nowsecure.auto.domain.ReportInfo;
import com.nowsecure.auto.domain.ScoreInfo;
import com.nowsecure.auto.domain.UploadRequest;
import com.nowsecure.auto.utils.IOHelper;
import com.nowsecure.auto.utils.IOHelperI;

public class NSAutoGateway {
    static int FIFTEEN_SECONDS = 15000;
    private static final String BINARY_URL_SUFFIX = "/binary/";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_UPLOADED_BINARY_JSON = "/nowsecure-auto-security-test-uploaded-binary.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_REPORT_REQUEST_JSON = "/nowsecure-auto-security-test-request.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_PREFLIGHT_JSON = "/nowsecure-auto-security-test-preflight.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_SCORE_JSON = "/nowsecure-auto-security-test-score.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_REPORT_JSON = "/nowsecure-auto-security-test-report.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_BUILD_JSON = "/nowsecure-auto-security-test-build.json";
    //
    private final NSAutoParameters params;
    private final NSAutoLogger logger;
    private final IOHelperI helper;
    List<File> artifacts = new ArrayList<File>();
    private Set<String> statusMessages = new HashSet<String>();

    //
    public NSAutoGateway(NSAutoParameters params, NSAutoLogger logger, IOHelperI helper) throws IOException {
        this.params = params;
        this.logger = logger;
        this.helper = helper;
        //
        logEnv("Master");
        // validate("Master");
        if (params.getProxySettings() != null) {
            params.getProxySettings().validate("Master");
        }
    }

    public Map<String, String> getArtifactContents(boolean delete) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        for (File file : artifacts) {
            String contents = new String(helper.load(file), StandardCharsets.UTF_8);
            map.put(file.getName(), contents);
            if (delete) {
                file.delete();
            }
        }
        return map;
    }

    public void execute(boolean master) throws IOException {
        Map<String, String> settings = params.getProxySettings() != null
                ? params.getProxySettings().overrideSystemProperties() : new HashMap<String, String>();
        //
        logger.info("[master: " + master + "] executing plugin for " + this);
        try {
            if (!master) {
                logEnv("Slave");
                if (params.isValidateDnsUrlConnectionEnabled()) {
                    validate("Slave");
                } else {
                    logger.info("[master: " + master
                                + "] Skipping validation for DNS and HTTPS Connection with the NowSecure API.");
                }
                params.getProxySettings().validate("Slave");
            }
            //
            AssessmentRequest request = triggerAssessment(preflight(uploadBinary()));
            //
            if (params.getWaitMinutes() > 0) {
                waitForResults(request);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to run security test due to " + e, e);
        } finally {
            if (params.getProxySettings() != null) {
                params.getProxySettings().restoreOldSettings(settings);
            }
        }
    }

    UploadRequest uploadBinary() throws IOException, ParseException {
        File file = params.getFile();
        //
        String url = buildUrl(BINARY_URL_SUFFIX);
        logger.info("uploading binary " + file.getAbsolutePath() + " of size " + file.length() + " bytes, hash "
                    + IOHelper.toDigest(file, "SHA-256") + " to " + url);
        String json = helper.upload(url, params.getApiKey(), file);
        File path = new File(
                params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_UPLOADED_BINARY_JSON);
        helper.save(path, json); //
        artifacts.add(path);
        UploadRequest request = UploadRequest.fromJson(json);
        //
        if (params.isDebug() || json.contains("error") || IOHelper.isEmpty(request.getPackageId())
            || IOHelper.isEmpty(request.getPlatform())) {
            logger.info("Debug Upload Results: " + json, Color.Cyan);
        }

        logger.info(
                "uploaded binary with digest " + request.getBinary() + " and saved output to " + path.getAbsolutePath(),
                Color.Green);
        return request;
    }

    UploadRequest preflight(UploadRequest request) throws IOException, ParseException {
        String url = buildUrl("/binary/" + request.getBinary() + "/analysis");
        logger.info("executing preflight for digest " + request.getBinary() + " to " + url);
        try {
            String json = helper.get(url, params.getApiKey());
            if (params.isDebug() || json.contains("error") || IOHelper.isEmpty(request.getPackageId())
                || IOHelper.isEmpty(request.getPlatform())) {

                logger.info("Prefight Results: " + json, Color.Cyan);
            }
            File path = new File(
                    params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_PREFLIGHT_JSON);
            helper.save(path, json); //
            artifacts.add(path);
            logger.info("saved preflight results to " + path.getAbsolutePath(), Color.Green);

            if (json.contains("error")) {
                throw new IOException("Preflight failed");
            }
            return build(request);
        } catch (IOException e) {
            String msg = e.toString().contains("401 for URL") ? "" : " due to " + e.toString();
            throw new IOException("Failed to execute preflight for " + request.getBinary() + msg, e);
        }
    }

    UploadRequest build(UploadRequest request) throws IOException, ParseException {
        if (IOHelper.isEmpty(request.getPackageId()) || IOHelper.isEmpty(request.getPlatform())) {
            File file = params.getFile();
            String url = buildUrl(BINARY_URL_SUFFIX);
            String json = helper.upload(url, params.getApiKey(), file);

            request = UploadRequest.fromJson(json);
            if (params.isDebug() || json.contains("error") || IOHelper.isEmpty(request.getPackageId())
                || IOHelper.isEmpty(request.getPlatform())) {
                logger.info("Debug Retried Upload Results: " + json, Color.Cyan);
            }
        }
        return request;

        // String url = buildUrl("/build/" + request.getBinary());
        // logger.info("executing build for digest " + request.getBinary() + "
        // to " + url);
        // try {
        // String json = helper.get(url, params.getApiKey());
        // File path = new File(params.getArtifactsDir().getCanonicalPath() +
        // NOWSECURE_AUTO_SECURITY_TEST_BUILD_JSON);
        // helper.save(path, json); //
        // artifacts.add(path);
        // logger.info("saved build results to " + path.getAbsolutePath(),
        // Color.Green);
        // if (json.contains("error")) {
        // throw new IOException("Build results failed");
        // }
        // return request;
        // } catch (IOException e) {
        // String msg = e.toString().contains("401 for URL") ? "" : " due to " +
        // e.toString();
        // throw new IOException("Failed to execute build for " +
        // request.getBinary() + msg, e);
        // }
    }

    AssessmentRequest triggerAssessment(UploadRequest uploadRequest) throws IOException, ParseException {
        String url = buildUrl(
                "/app/" + uploadRequest.getPlatform() + "/" + uploadRequest.getPackageId() + "/assessment/");
        logger.info("triggering security test for digest " + uploadRequest.getBinary() + " to " + url);
        
        String json = helper.post(url, params.getApiKey());
        File path = new File(
                params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_REPORT_REQUEST_JSON);
        helper.save(path, json); //
        artifacts.add(path);

        AssessmentRequest request = AssessmentRequest.fromJson(json);
        logger.info("triggered security test for digest " + uploadRequest.getBinary() + " to " + url
                    + " and saved output to " + path.getAbsolutePath());
        return request;
    }

    void showStatusMessages(AssessmentRequest request) throws MalformedURLException {
        String url = buildUrl("/analysis-events/" + request.getTask() + "/dynamic");
        try {
            String json = helper.get(url, params.getApiKey());
            List<String> msgs = Message.fromJson(json);
            for (String msg : msgs) {
                if (!statusMessages.contains(msg)) {
                    statusMessages.add(msg);
                    logger.info("status: " + msg);
                }
            }
        } catch (Exception e) {
            logger.error("URL " + url + " failed " + e);
        }
    }

    //
    ReportInfo[] getReportInfos(AssessmentRequest uploadInfo) throws IOException, ParseException {
        String resultsUrl = buildUrl("/app/" + uploadInfo.getPlatform() + "/" + uploadInfo.getPackageId()
                                     + "/assessment/" + uploadInfo.getTask() + "/results");
        File path = new File(params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_REPORT_JSON);
        String reportJson = helper.get(resultsUrl, params.getApiKey());
        ReportInfo[] reportInfos = ReportInfo.fromJson(reportJson);
        if (reportInfos.length > 0) {
            helper.save(path, reportJson);
            artifacts.add(path);
            logger.info("saved test report from " + resultsUrl + " to " + path.getAbsolutePath(), Color.Green);
        }
        return reportInfos;
    }

    ScoreInfo getScoreInfo(AssessmentRequest uploadInfo) throws ParseException, IOException {
        String scoreUrl = buildUrl("/assessment/" + uploadInfo.getTask() + "/summary");
        File path = new File(params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_SCORE_JSON);
        String scoreJson = helper.get(scoreUrl, params.getApiKey());
        if (scoreJson.isEmpty()) {
            return null;
        }
        helper.save(path, scoreJson);
        artifacts.add(path);
        logger.info("saved score report from " + scoreUrl + " to " + path.getAbsolutePath(), Color.Green);
        return ScoreInfo.fromJson(scoreJson);
    }

    void waitForResults(AssessmentRequest request) throws IOException, ParseException {
        //
        long started = System.currentTimeMillis();
        for (int min = 0; min < params.getWaitMinutes(); min++) {
            if (params.isShowStatusMessages()) {
                for (int j = 0; j < 4; j++) {
                    try {
                        Thread.sleep(FIFTEEN_SECONDS);
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                    showStatusMessages(request);
                }
            } else {
                logger.info("waiting test results for job " + request.getTask() + getElapsedMinutes(started));
                try {
                    Thread.sleep(FIFTEEN_SECONDS * 4);
                } catch (InterruptedException e) {
                    Thread.interrupted();
                } // wait a minute
            }
            //
            ScoreInfo scoreInfo = getScoreInfo(request);
            if (scoreInfo != null) {
                getReportInfos(request);
                if ("failed".equals(scoreInfo.getStatus())) {
                    throw new IOException("Job failed and could not produce score");
                } else if (scoreInfo.getScore() < params.getScoreThreshold()) {
                    throw new IOException("Test failed because score (" + scoreInfo.getScore()
                                          + ") is lower than threshold " + params.getScoreThreshold());
                }
                logger.info("test passed with score " + scoreInfo.getScore() + getElapsedMinutes(started), Color.Green);
                return;
            }
        }
        throw new IOException("Timedout" + getElapsedMinutes(started) + " while waiting for job " + request.getTask());
    }

    String getElapsedMinutes(long started) {
        long min = (System.currentTimeMillis() - started) / (FIFTEEN_SECONDS * 4);
        if (min == 0) {
            return "";
        }
        return " [" + min + " minutes]";
    }

    String buildUrl(String path) throws MalformedURLException {
        return buildUrl(path, new URL(params.getApiUrl()), params.getGroup());
    }

    public static String buildUrl(String path, URL api, String group) throws MalformedURLException {
        String baseUrl = api.getProtocol() + "://" + api.getHost();
        if (api.getPort() > 0) {
            baseUrl += ":" + api.getPort();
        }
        String url = baseUrl + path;
        if (group != null && group.length() > 0) {
            url += "?group=" + group;
        }
        return url;
    }

    void validate(String prefix) throws IOException {
        logger.info("Validating DNS and HTTPS Connection with the NowSecure API: " + params.getApiUrl());

        URL url = null;
        try {
            url = new URL(params.getApiUrl());
        } catch (Exception e) {
            throw new IOException(prefix + " Failed to parse URL " + params.getApiUrl() + " due to " + e);
        }
        //
        try {
            InetAddress.getByName(url.getHost());
        } catch (Exception e) {
            throw new IOException(prefix + " Failed to lookup host URL " + url + " due to " + e);
        }
        //
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while (rd.readLine() != null) {
            }
            rd.close();
        } catch (Exception e) {
            throw new IOException(prefix + " Failed to connect to URL " + url + " due to " + e + "\n" + this, e);
        }
    }

    private void logEnv(String prefix) throws UnknownHostException {
        logger.info(prefix + " Local Hostname: " + InetAddress.getLocalHost() + ", debug " + params.isDebug());
        if (params.isDebug()) {
            logMap(prefix + " Environment variables:\n", System.getenv());
            logMap(prefix + " System properties:\n", System.getProperties());
        }
    }

    private void logMap(String prefix, Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            String key = e.getKey().toString().toLowerCase();
            // if (!key.contains("password") && key.matches(".*(http|proxy).*"))
            // {
            String val = e.getValue().toString();
            if (val.length() > 80) {
                val = val.substring(0, 80);
            }
            sb.append("\t" + e.getKey() + " = " + val + "\r\n");
            // }
        }
        logger.info(prefix + sb + "\n\n");
    }

    @Override
    public String toString() {
        String tok = params.getApiKey() != null && params.getApiKey().length() > 4
                ? params.getApiKey().substring(0, 4) + "***" : "Unknown";
        return "NSAutoGateway [artifactsDir=" + params.getArtifactsDir() + ", apiUrl=" + params.getApiUrl() + ", group="
               + params.getGroup() + ", file=" + params.getFile() + ", waitMinutes=" + params.getWaitMinutes()
               + ", scoreThreshold=" + params.getScoreThreshold() + ", apiKey=" + tok + ", proxySettings="
               + params.getProxySettings() + ", debug=" + params.isDebug() + "]";
    }
}
