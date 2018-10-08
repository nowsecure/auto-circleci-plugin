package com.nowsecure.auto.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.nowsecure.auto.domain.AssessmentRequest;

public class AssessmentRequestTest {

    @Test
    public void testFrom() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("upload.json").toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String json = new String(fileBytes);
        AssessmentRequest info = AssessmentRequest.fromJson(json);
        Assert.assertEquals("myaccount", info.getAccount());
        Assert.assertTrue(info.toString().length() > 0);
    }

    @Test(expected = IOException.class)
    public void testFromErrorWithName() throws Exception {
        AssessmentRequest.fromJson("{\"name\":\"err\"}");
    }

    @Test(expected = IOException.class)
    public void testFromErrorWithMessage() throws Exception {
        AssessmentRequest.fromJson("{\"message\":\"msg\"}");
    }

    @Test(expected = IOException.class)
    public void testFromError() throws Exception {
        AssessmentRequest.fromJson("{\"name\":\"err\", \"message\":\"msg\"}");
    }

    @Test(expected = IOException.class)
    public void testFromNoPackage() throws Exception {
        AssessmentRequest.fromJson("{}");
    }

    @Test(expected = IOException.class)
    public void testFromEmptyPackage() throws Exception {
        AssessmentRequest.fromJson("{\"package\":\"\"}");
    }

    @Test(expected = IOException.class)
    public void testFromNoBinary() throws Exception {
        AssessmentRequest.fromJson("{\"package\":\"blah\"}");
    }

    @Test(expected = IOException.class)
    public void testFromEmptyBinary() throws Exception {
        AssessmentRequest.fromJson("{\"package\":\"blah\", \"binary\":\"\"}");
    }

    @Test(expected = IOException.class)
    public void testFromTask() throws Exception {
        AssessmentRequest.fromJson("{\"package\":\"blah\", \"binary\":\"blah\"}");
    }

    @Test
    public void testFromFull() throws Exception {
        AssessmentRequest.fromJson("{\"package\":\"blah\", \"binary\":\"blah\", \"task\":10}");
    }

    @Test
    public void testGetSetGroup() throws Exception {
        AssessmentRequest info = new AssessmentRequest();
        info.setGroup("group");
        Assert.assertEquals("group", info.getGroup());
    }

    @Test
    public void testGetSetAccount() throws Exception {
        AssessmentRequest info = new AssessmentRequest();
        info.setAccount("act");
        Assert.assertEquals("act", info.getAccount());
    }

    @Test
    public void testGetSetTask() throws Exception {
        AssessmentRequest info = new AssessmentRequest();
        info.setTask(11L);
        Assert.assertEquals(new Long(11), info.getTask());
    }

    @Test
    public void testGetSetCreator() throws Exception {
        AssessmentRequest info = new AssessmentRequest();
        info.setCreator("user");
        Assert.assertEquals("user", info.getCreator());
    }

    @Test
    public void testGetSetCreated() throws Exception {
        AssessmentRequest info = new AssessmentRequest();
        info.setCreated("date");
        Assert.assertEquals("date", info.getCreated());
    }

}
