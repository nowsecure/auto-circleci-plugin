package com.nowsecure.auto.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IOHelperTest {
    private static final String GROUP = "aaaa";
    private static final String API = "eyJ";
    private static final File file = new File("apkpure_app_887.apk");
    private IOHelper helper = new IOHelper("test", 60000);

    @Before
    public void setup() {
        IOHelper.VERSION = "";
    }
    
    @Test
    public void testLoad() throws Exception {
        byte[] data = helper.load(new File("src/test/resources/score.json"));
        Assert.assertTrue(data.length > 0);
    }

    @Test(expected = FileNotFoundException.class)
    public void testLoadUnknown() throws Exception {
        helper.load(new File("src/test/resources/blah.json"));
    }

    @Test
    public void testLoadStream() throws Exception {
        byte[] data = helper.load(helper.getClass().getResourceAsStream("/score.json"));
        Assert.assertTrue(data.length > 0);
    }

    @Test
    public void testGetVersion() throws Exception {
        IOHelper.getVersion();
    }

    @Test
    public void testDigestFile() throws Exception {
        File tempFile = File.createTempFile("prefix-", "-suffix");
        tempFile.deleteOnExit();
        String digest = IOHelper.toDigest(tempFile, "SHA256");
        Assert.assertNotNull(digest);
    }

    @Test
    public void testDigestFileUnknown() throws Exception {
        String digest = IOHelper.toDigest(new File("/tmp/unknown"), "SHA256");
        Assert.assertEquals("NO-DIGEST", digest);
    }

    @Test
    public void testDigest() throws Exception {
        File file = File.createTempFile("test", "test");
        file.deleteOnExit();
        FileWriter out = new FileWriter(file);
        out.write("My bytes");
        out.close();
        String digest = IOHelper.toDigest(file, "SHA-256");
        Assert.assertEquals("SHA-256=67a3742ea9fc0dad80b8a78d311592ab14cb0c4f14bf820faf40a208c9f164f6", digest);
    }

    @Test
    public void testDigestRandom() throws Exception {
        File file = File.createTempFile("test", "test");
        file.deleteOnExit();
        FileWriter out = new FileWriter(file);
        out.write("This is test\nfile with random\ndata.\n");
        out.close();
        String digest = IOHelper.toDigest(file, "SHA-256");
        Assert.assertEquals("SHA-256=60fa63fbf0029122912c8eb73a3151915ee4ab323318b47229ed484d31042494", digest);
    }

    @Test
    public void testDigestBad() throws Exception {
        File file = File.createTempFile("test", "test");
        file.deleteOnExit();
        FileWriter out = new FileWriter(file);
        out.write("My bytes");
        out.close();
        String digest = IOHelper.toDigest(file, "Bad");
        Assert.assertEquals("NO-DIGEST", digest);
    }

    @Test
    public void testGetVersionUnknown() throws Exception {
        IOHelper.VERSION_TXT = "xxx";
        String version = IOHelper.getVersion();
        Assert.assertEquals("1.0.0", version);
    }

    @Test
    public void testFindUnknown() throws Exception {
        File file = helper.find(new File("src"), new File("xxxscore.json"));
        Assert.assertNull(file);
        file = helper.find(new File("yyyyy"), new File("xxxscore.json"));
        Assert.assertNotNull(file);
        file = helper.find(new File("xxxx"), new File("src"));
        Assert.assertNotNull(file);
    }

    @Test
    public void testFindDirect() throws Exception {
        File file = helper.find(new File("xxxx"), new File("src/test/resources/score.json"));
        Assert.assertNotNull(file);
    }

    @Test
    public void testFind() throws Exception {
        new File("/tmp/test.out").createNewFile();
        File file = helper.find(new File("/tmp/xx"), new File("test.out"));
        Assert.assertNotNull(file);
    }

    @Test
    public void testSave() throws Exception {
        helper.save(new File("/tmp/save.txt"), "test");
    }

    @Test
    public void testGet() throws Exception {
        String json = helper.get("https://google.com", API);
        Assert.assertNotNull(json);
    }

    @Test
    public void testPost() throws Exception {
        String json = helper.post("https://httpbin.org/post", API);
        Assert.assertNotNull(json);
    }

    @Test
    public void testPostUpload() throws Exception {
        String json = helper.upload("https://httpbin.org/post", API, new File("/tmp/save.txt"));
        Assert.assertNotNull(json);
    }

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertTrue(IOHelper.isEmpty(null));
        Assert.assertTrue(IOHelper.isEmpty(""));
        Assert.assertFalse(IOHelper.isEmpty("x"));
    }

    @Test
    public void testGetLocalhost() throws Exception {
        Assert.assertNotNull(IOHelper.getLocalHost());
    }

    // @Test
    public void testGetResults() throws Exception {
        String json = helper.get("https://lab-api.nowsecure.com/app/android/pkg/assessment/task/results", API);
        Assert.assertNotNull(json);
    }

    // @Test
    public void testUploadBinary() throws Exception {
        String json = helper.upload("https://lab-api.nws-stg-west.nowsecure.io/binary/", API, file);
        Assert.assertNotNull(json);
    }

    // @Test
    public void testUpload() throws Exception {
        String json = helper.upload("https://lab-api.nowsecure.com/build/?group=" + GROUP, API, file);
        Assert.assertNotNull(json);
    }

    // @Test
    public void testGetScore() throws Exception {
        String json = helper.get("https://lab-api.nowsecure.com/assessment/task/summary", API);
        Assert.assertNotNull(json);
    }

    @Test(expected = IOException.class)
    public void testGetUsage() throws Exception {
        String json = helper.get("https://lab-api.nowsecure.com/resource/usage", API);
        Assert.assertNotNull(json);
    }

}
