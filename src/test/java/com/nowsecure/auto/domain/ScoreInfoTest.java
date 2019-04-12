package com.nowsecure.auto.domain;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.nowsecure.auto.domain.ScoreInfo;

public class ScoreInfoTest {
    @Test
    public void testFrom() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("score.json").toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String json = new String(fileBytes);
        ScoreInfo score = ScoreInfo.fromJson(json);
        Assert.assertEquals("v3", score.getCvssVersion());
    }

    @Test
    public void testFromBad() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("badscore.json").toURI());
        byte[] fileBytes = Files.readAllBytes(path);
        String json = new String(fileBytes);
        ScoreInfo score = ScoreInfo.fromJson(json);
        Assert.assertNull(score);
    }

    @Test
    public void testGetSetScore() throws Exception {
        ScoreInfo score = new ScoreInfo();
        score.setScore(2);
        Assert.assertEquals(2, score.getScore(), 0.000001);
    }

    @Test
    public void testGetSetBaseScore() throws Exception {
        ScoreInfo score = new ScoreInfo();
        score.setBaseScore(2);
        Assert.assertEquals(2, score.getBaseScore(), 0.000001);
    }

    @Test
    public void testGetSetIssues() throws Exception {
        ScoreInfo score = new ScoreInfo();
        score.setIssues("issues");
        Assert.assertEquals("issues", score.getIssues());
    }

    @Test
    public void testGetSetAffectedIssues() throws Exception {
        ScoreInfo score = new ScoreInfo();
        score.setAdjustedIssues("issues");
        Assert.assertEquals("issues", score.getAdjustedIssues());
    }

    @Test
    public void testGetSetFindingsDigest() throws Exception {
        ScoreInfo score = new ScoreInfo();
        score.setFindingsDigest("digest");
        Assert.assertEquals("digest", score.getFindingsDigest());
    }

    @Test
    public void testGetSetFindingsVersion() throws Exception {
        ScoreInfo score = new ScoreInfo();
        score.setFindingsVersion("ver");
        Assert.assertEquals("ver", score.getFindingsVersion());
    }

    @Test
    public void testGetSetCvssVersion() throws Exception {
        ScoreInfo score = new ScoreInfo();
        score.setCvssVersion("ver");
        Assert.assertEquals("ver", score.getCvssVersion());
    }

}
