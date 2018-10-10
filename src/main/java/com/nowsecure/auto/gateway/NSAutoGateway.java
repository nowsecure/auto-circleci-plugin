package com.nowsecure.auto.gateway;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.parser.ParseException;

import com.nowsecure.auto.domain.AssessmentRequest;
import com.nowsecure.auto.domain.NSAutoLogger;
import com.nowsecure.auto.domain.NSAutoParameters;
import com.nowsecure.auto.domain.ReportInfo;
import com.nowsecure.auto.domain.ScoreInfo;
import com.nowsecure.auto.domain.UploadRequest;
import com.nowsecure.auto.utils.IOHelperI;

public class NSAutoGateway {
    private static final String BINARY_URL_SUFFIX = "/binary/";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_UPLOADED_BINARY_JSON = "/nowsecure-auto-security-test-uploaded-binary.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_REPORT_REQUEST_JSON = "/nowsecure-auto-security-test-request.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_PREFLIGHT_JSON = "/nowsecure-auto-security-test-preflight.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_SCORE_JSON = "/nowsecure-auto-security-test-score.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_REPORT_JSON = "/nowsecure-auto-security-test-report.json";
    static int POLL_INTERVAL = 1000 * 60;
    //
    private final NSAutoParameters params;
    private final NSAutoLogger logger;
    private final IOHelperI helper;

    public NSAutoGateway(NSAutoParameters params, NSAutoLogger logger, IOHelperI helper) {
        this.params = params;
        this.logger = logger;
        this.helper = helper;
    }

    public void execute() throws IOException {
        logger.info("executing plugin for " + this);
        try {
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
        }
    }

    UploadRequest uploadBinary() throws IOException, ParseException {
        File file = params.getFile();
        //
        String url = buildUrl(BINARY_URL_SUFFIX);
        logger.info("uploading binary " + file.getAbsolutePath() + " to " + url);
        String json = helper.upload(url, params.getApiKey(), file);
        File path = new File(
                params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_UPLOADED_BINARY_JSON);
        helper.save(path, json); //
        UploadRequest request = UploadRequest.fromJson(json);
        logger.info("uploaded binary with digest " + request.getBinary() + " and saved output to " + path);
        return request;
    }

    UploadRequest preflight(UploadRequest request) throws IOException, ParseException {
        String url = buildUrl("/binary/" + request.getBinary() + "/analysis");
        logger.info("Executing preflight for digest " + request.getBinary() + " to " + url);
        try {
            String json = helper.get(url, params.getApiKey());
            File path = new File(
                    params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_PREFLIGHT_JSON);
            helper.save(path, json); //
            logger.info("saved preflight results to " + path);
            if (json.contains("error")) {
                throw new IOException("Preflight failed");
            }
            return request;
        } catch (IOException e) {
            String msg = e.toString().contains("401 for URL") ? "" : " due to " + e.toString();
            throw new IOException("Failed to execute preflight for " + request.getBinary() + msg, e);
        }
    }

    AssessmentRequest triggerAssessment(UploadRequest uploadRequest) throws IOException, ParseException {
        String url = buildUrl(
                "/app/" + uploadRequest.getPlatform() + "/" + uploadRequest.getPackageId() + "/assessment/");
        String json = helper.post(url, params.getApiKey());
        File path = new File(
                params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_REPORT_REQUEST_JSON);
        helper.save(path, json); //
        AssessmentRequest request = AssessmentRequest.fromJson(json);
        logger.info("triggered security test for digest " + uploadRequest.getBinary() + " to " + url
                    + " and saved output to " + path);
        return request;
    }

    ReportInfo[] getReportInfos(AssessmentRequest uploadInfo) throws IOException, ParseException {
        String resultsUrl = buildUrl("/app/" + uploadInfo.getPlatform() + "/" + uploadInfo.getPackageId()
                                     + "/assessment/" + uploadInfo.getTask() + "/results");
        File resultsPath = new File(
                params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_REPORT_JSON);
        String reportJson = helper.get(resultsUrl, params.getApiKey());
        ReportInfo[] reportInfos = ReportInfo.fromJson(reportJson);
        if (reportInfos.length > 0) {
            helper.save(resultsPath, reportJson);
            logger.info("saved test report from " + resultsUrl + " to " + resultsPath);
        }
        return reportInfos;
    }

    ScoreInfo getScoreInfo(AssessmentRequest uploadInfo) throws ParseException, IOException {
        String scoreUrl = buildUrl("/assessment/" + uploadInfo.getTask() + "/summary");
        File scorePath = new File(
                params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_SCORE_JSON);
        String scoreJson = helper.get(scoreUrl, params.getApiKey());
        if (scoreJson.isEmpty()) {
            return null;
        }
        helper.save(scorePath, scoreJson);
        logger.info("saved score report from " + scoreUrl + " to " + scorePath);
        return ScoreInfo.fromJson(scoreJson);
    }

    void waitForResults(AssessmentRequest uploadInfo) throws IOException, ParseException {
        //
        long started = System.currentTimeMillis();
        for (int min = 0; min < params.getWaitMinutes(); min++) {
            logger.info("waiting test results for job " + uploadInfo.getTask() + getElapsedMinutes(started));
            try {
                Thread.sleep(POLL_INTERVAL);
            } catch (InterruptedException e) {
                Thread.interrupted();
            } // wait a minute
            ScoreInfo scoreInfo = getScoreInfo(uploadInfo);
            if (scoreInfo != null) {
                getReportInfos(uploadInfo);
                if (scoreInfo.getScore() < params.getScoreThreshold()) {
                    throw new IOException("Test failed because score (" + scoreInfo.getScore()
                                          + ") is lower than threshold " + params.getScoreThreshold());
                }
                logger.info("test passed with score " + scoreInfo.getScore() + getElapsedMinutes(started));
                return;
            }
        }
        throw new IOException(
                "Timedout" + getElapsedMinutes(started) + " while waiting for job " + uploadInfo.getTask());
    }

    String getElapsedMinutes(long started) {
        long min = (System.currentTimeMillis() - started) / POLL_INTERVAL;
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

    @Override
    public String toString() {
        String tok = params.getApiKey() != null && params.getApiKey().length() > 4
                ? params.getApiKey().substring(0, 4) + "***" : "Unknown";
        return "NSAutoGateway [artifactsDir=" + params.getArtifactsDir() + ", apiUrl=" + params.getApiUrl() + ", group="
               + params.getGroup() + ", file=" + params.getFile() + ", waitMinutes=" + params.getWaitMinutes()
               + ", scoreThreshold=" + params.getScoreThreshold() + ", apiKey=" + tok + "]";
    }
}
