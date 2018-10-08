package com.nowsecure.auto.domain;

import org.junit.Assert;
import org.junit.Test;

import com.nowsecure.auto.domain.MetadataRequest;

public class MetadataRequestTest {
    @Test
    public void testGetSetApplication() throws Exception {
        MetadataRequest req = new MetadataRequest();
        req.setApplication("app");
        Assert.assertEquals("app", req.getApplication());
    }

    @Test
    public void testGetSetVersion() throws Exception {
        MetadataRequest req = new MetadataRequest();
        req.setVersion("v1");
        Assert.assertEquals("v1", req.getVersion());
    }

    @Test
    public void testGetSetPlatform() throws Exception {
        MetadataRequest req = new MetadataRequest();
        req.setPlatform("ios");
        Assert.assertEquals("ios", req.getPlatform());
    }

    @Test
    public void testGetSetPackageId() throws Exception {
        MetadataRequest req = new MetadataRequest();
        req.setPackageId("pkg");
        Assert.assertEquals("pkg", req.getPackageId());
    }

    @Test
    public void testGetSetBinary() throws Exception {
        MetadataRequest req = new MetadataRequest();
        req.setBinary("binary");
        Assert.assertEquals("binary", req.getBinary());
    }

}
