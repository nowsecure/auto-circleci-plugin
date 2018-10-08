package com.nowsecure.auto.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.nowsecure.auto.domain.ReportInfo;

public class ReportInfoTest {
    @Test
    public void testFrom() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("report.json").toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String json = new String(fileBytes);
        ReportInfo[] reports = ReportInfo.fromJson(json);
        Assert.assertEquals(6, reports.length);
    }

    @Test(expected = IOException.class)
    public void testFromErrorWithName() throws Exception {
        ReportInfo.fromJson("{\"name\":\"err\"}");
    }

    @Test
    public void testGetSetKind() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setKind("kind");
        Assert.assertEquals("kind", report.getKind());
    }

    @Test
    public void testGetSetKey() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setKey("key");
        Assert.assertEquals("key", report.getKey());
    }

    @Test
    public void testGetSetTitle() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setTitle("title");
        Assert.assertEquals("title", report.getTitle());
    }

    @Test
    public void testGetSetSummary() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setSummary("summary");
        Assert.assertEquals("summary", report.getSummary());
    }

    @Test
    public void testGetSetCvss() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setCvss(2);
        Assert.assertEquals(2, report.getCvss(), 0.000001);
    }

    @Test
    public void testGetSetCvssVector() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setCvssVector("vec");
        Assert.assertEquals("vec", report.getCvssVector());
    }

    @Test
    public void testGetSetAffected() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setAffected(true);
        Assert.assertEquals(true, report.isAffected());
    }

    @Test
    public void testGetSetSeverity() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setSeverity("critical");
        Assert.assertEquals("critical", report.getSeverity());
    }

    @Test
    public void testGetSetDescription() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setDescription("desc");
        Assert.assertEquals("desc", report.getDescription());
    }

    @Test
    public void testGetSetRegulatory() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setRegulatory("desc");
        Assert.assertEquals("desc", report.getRegulatory());
    }

    @Test
    public void testGetSetIssues() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setIssues("issues");
        Assert.assertEquals("issues", report.getIssues());
    }

    @Test
    public void testGetSetContext() throws Exception {
        ReportInfo report = new ReportInfo();
        report.setContext("ctx");
        Assert.assertEquals("ctx", report.getContext());
    }

}
