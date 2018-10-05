package com.nowsecure.auto.circleci.gateway;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.parser.ParseException;

import com.nowsecure.auto.circleci.domain.AssessmentRequest;
import com.nowsecure.auto.circleci.domain.NSAutoParameters;
import com.nowsecure.auto.circleci.domain.ReportInfo;
import com.nowsecure.auto.circleci.domain.ScoreInfo;
import com.nowsecure.auto.circleci.domain.UploadRequest;
import com.nowsecure.auto.circleci.utils.IOHelper;

public class NSAutoGateway {
    private static final String BINARY_URL_SUFFIX = "/binary/";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_UPLOADED_BINARY_JSON = "/nowsecure-auto-security-test-uploaded-binary.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_REPORT_REQUEST_JSON = "/nowsecure-auto-security-test-request.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_PREFLIGHT_JSON = "/nowsecure-auto-security-test-preflight.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_SCORE_JSON = "/nowsecure-auto-security-test-score.json";
    private static final String NOWSECURE_AUTO_SECURITY_TEST_REPORT_JSON = "/nowsecure-auto-security-test-report.json";
    private static final int ONE_MINUTE = 1000 * 60;
    //
    private final NSAutoParameters params;
    private final IOHelper helper;

    public NSAutoGateway(NSAutoParameters params, IOHelper helper) {
        this.params = params;
        this.helper = helper;
    }

    public void execute() throws IOException {
        params.info("Executing step for " + this);
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
            throw new IOException("Failed to run security test due to " + e);
        }
    }

    private UploadRequest uploadBinary() throws IOException, ParseException {
        File file = params.getFile();
        //
        String url = buildUrl(BINARY_URL_SUFFIX);
        params.info("uploading binary " + file.getAbsolutePath() + " to " + url);
        String json = helper.upload(url, params.getApiKey(), file.getCanonicalPath());
        String path = params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_UPLOADED_BINARY_JSON;
        helper.save(path, json); //
        UploadRequest request = UploadRequest.fromJson(json);
        params.info("uploaded binary with digest " + request.getBinary() + " and saved output to " + path);
        return request;
    }

    private UploadRequest preflight(UploadRequest request) throws IOException, ParseException {
        String url = buildUrl("/binary/" + request.getBinary() + "/analysis");
        params.info("Executing preflight for digest " + request.getBinary() + " to " + url);
        try {
            String json = helper.get(url, params.getApiKey());
            String path = params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_PREFLIGHT_JSON;
            helper.save(path, json); //
            params.info("saved preflight results to " + path);
            if (json.contains("error")) {
                throw new IOException("Preflight failed");
            }
            return request;
        } catch (IOException e) {
            String msg = e.getMessage().contains("401 for URL") ? "" : " due to " + e.getMessage();
            throw new IOException("Failed to execute preflight for " + request.getBinary() + msg);
        }
    }

    private AssessmentRequest triggerAssessment(UploadRequest uploadRequest) throws IOException, ParseException {
        String url = buildUrl(
                "/app/" + uploadRequest.getPlatform() + "/" + uploadRequest.getPackageId() + "/assessment/");
        String json = helper.post(url, params.getApiKey());
        String path = params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_REPORT_REQUEST_JSON;
        helper.save(path, json); //
        AssessmentRequest request = AssessmentRequest.fromJson(json);
        params.info("triggered security test for digest " + uploadRequest.getBinary() + " to " + url
                    + " and saved output to " + path);
        return request;
    }

    private ReportInfo[] getReportInfos(AssessmentRequest uploadInfo) throws IOException, ParseException {
        String resultsUrl = buildUrl("/app/" + uploadInfo.getPlatform() + "/" + uploadInfo.getPackageId()
                                     + "/assessment/" + uploadInfo.getTask() + "/results");
        String resultsPath = params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_REPORT_JSON;
        String reportJson = helper.get(resultsUrl, params.getApiKey());
        ReportInfo[] reportInfos = ReportInfo.fromJson(reportJson);
        if (reportInfos.length > 0) {
            helper.save(resultsPath, reportJson);
            params.info("saved test report from " + resultsUrl + " to " + resultsPath);
        }
        return reportInfos;
    }

    private ScoreInfo getScoreInfo(AssessmentRequest uploadInfo) throws ParseException, IOException {
        String scoreUrl = buildUrl("/assessment/" + uploadInfo.getTask() + "/summary");
        String scorePath = params.getArtifactsDir().getCanonicalPath() + NOWSECURE_AUTO_SECURITY_TEST_SCORE_JSON;
        String scoreJson = helper.get(scoreUrl, params.getApiKey());
        if (scoreJson.isEmpty()) {
            return null;
        }
        helper.save(scorePath, scoreJson);
        params.info("saved score report from " + scoreUrl + " to " + scorePath);
        return ScoreInfo.fromJson(scoreJson);
    }

    private void waitForResults(AssessmentRequest uploadInfo) throws IOException, ParseException {
        //
        long started = System.currentTimeMillis();
        for (int min = 0; min < params.getWaitMinutes(); min++) {
            params.info("waiting test results for job " + uploadInfo.getTask() + getElapsedMinutes(started));
            try {
                Thread.sleep(ONE_MINUTE);
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
                params.info("test passed with score " + scoreInfo.getScore() + getElapsedMinutes(started));
                return;
            }
        }
        throw new IOException(
                "Timedout" + getElapsedMinutes(started) + " while waiting for job " + uploadInfo.getTask());
    }

    private String getElapsedMinutes(long started) {
        long min = (System.currentTimeMillis() - started) / ONE_MINUTE;
        if (min == 0) {
            return "";
        }
        return " [" + min + " minutes]";
    }

    private String buildUrl(String path) throws MalformedURLException {
        return buildUrl(path, new URL(params.getApiUrl()), params.getGroup());
    }

    public static String buildUrl(String path, URL api, String group) throws MalformedURLException {
        String baseUrl = api.getProtocol() + "://" + api.getHost();
        if (api.getPort() > 0) {
            baseUrl += ":" + api.getPort();
        }
        String url = baseUrl + path;
        if (group != null && group.length() > 0) {
            if (url.contains("?")) {
                url += "&";
            } else {
                url += "?";
            }
            url += "group=" + group;
        }
        return url;
    }

}
