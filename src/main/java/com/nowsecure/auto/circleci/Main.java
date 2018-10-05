package com.nowsecure.auto.circleci;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.nowsecure.auto.circleci.domain.NSAutoParameters;
import com.nowsecure.auto.circleci.gateway.NSAutoGateway;
import com.nowsecure.auto.circleci.utils.IOHelper;

/**
 * This class defines business logic for uploading mobile binary and retrieving
 * results and score. It would fail the job if score is below user-defined
 * threshold.
 * 
 * @author sbhatti
 *
 */
public class Main implements NSAutoParameters {
    private static final int TIMEOUT = 60000;
    private static final String PLUGIN_NAME = " circleci-nowsecure-auto-security-test v" + IOHelper.getVersion();
    private static final String DEFAULT_URL = "https://lab-api.nowsecure.com";
    private String apiUrl = DEFAULT_URL;
    private String group;
    private File file;
    private int waitMinutes;
    private boolean breakBuildOnScore;
    private int scoreThreshold;
    private String apiKey;
    private File artifactsDir;
    private final IOHelper helper = new IOHelper(PLUGIN_NAME, TIMEOUT);

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getArtifactsDir()
     */
    @Override
    public File getArtifactsDir() {
        return artifactsDir;
    }

    public void setArtifactsDir(File artifactsDir) {
        this.artifactsDir = artifactsDir;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getApiUrl()
     */
    @Override
    public String getApiUrl() {
        return apiUrl != null && apiUrl.length() > 0 ? apiUrl : DEFAULT_URL;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getGroup()
     */
    @Override
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @Override
    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getBinaryName()
     */
    @Override
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getWaitMinutes()
     */
    @Override
    public int getWaitMinutes() {
        return waitMinutes;
    }

    public void setWaitMinutes(int waitMinutes) {
        this.waitMinutes = waitMinutes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.nowsecure.auto.jenkins.plugin.NSAutoParameters#getScoreThreshold()
     */
    @Override
    public int getScoreThreshold() {
        return scoreThreshold;
    }

    public void setScoreThreshold(int scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    public void execute() throws IOException {
        new NSAutoGateway(this, helper).execute();
    }

    @Override
    public String toString() {
        return "Main [artifactsDir=" + artifactsDir + ", apiUrl=" + apiUrl + ", group=" + group + ", file=" + file
               + ", waitMinutes=" + waitMinutes + ", breakBuildOnScore=" + breakBuildOnScore + ", scoreThreshold="
               + scoreThreshold + ", apiKey=" + apiKey + "]";
    }

    private static int parseInt(String name) {
        String value = System.getProperty(name, "").trim();
        if (value.length() == 0) {
            value = System.getenv(name);
            if (value == null) {
                return 0;
            }
            value = value.trim();
        }
        value = value.replaceAll("\\D+", "");
        if (value.length() == 0) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    private static String getString(String name, String def) {
        String value = System.getProperty(name, "").trim();
        if (value.length() == 0) {
            value = System.getenv(name);
            if (value == null) {
                return def;
            }
            value = value.trim();
        }
        value = value.replace("<nil>", "");
        return value.length() == 0 ? def : value;
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.parseArgs(args);

        try {
            main.execute();
            System.exit(0);
        } catch (IOException | RuntimeException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    private void usage(String msg) {
        System.err.println(this);

        System.err.println(msg);
        System.err.println("Usage:\n");
        System.err.println(
                "\tgradle run --args=\"-u auto-url -d artifacts-dir -t api-token -t mobile-binary-file -g user-group -f binary-file -w wait-for-completion-in-minutes -s min-score-to-pass\"");
        System.err.println("\tOR");
        System.err
                .println(
                        "Usage: gradle run -Dauto.dir=artifacts-dir -Dauto.url=auto-url -Dauto.token=api-token -Dauto.file=mobile-binary-file"
                         + " -Dauto.group=user-group -Dauto.file=binary-file -Dauto.wait=wait-for-completion-in-minutes -Dauto.score=min-score-to-pass");
        System.err.println("\tDefault url is " + DEFAULT_URL);
        System.err.println("\tDefault auto-wait is 0, which means just upload without waiting for results");
        System.err.println(
                "\tDefault auto-score is 0, which means build won't break, otherwise build will break if the app score is lower than this number");
        System.exit(1);
    }

    private static boolean isEmpty(String m) {
        return m == null || m.trim().length() == 0;
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if ("-u".equals(args[i])) {
                this.apiUrl = args[i + 1].trim();
            } else if ("-g".equals(args[i])) {
                this.apiUrl = args[i + 1].trim();
            } else if ("-d".equals(args[i])) {
                this.artifactsDir = new File(args[i + 1].trim());
            } else if ("-f".equals(args[i])) {
                this.file = new File(args[i + 1].trim());
            } else if ("-t".equals(args[i])) {
                this.apiKey = args[i + 1].trim();
            } else if ("-w".equals(args[i])) {
                this.waitMinutes = Integer.parseInt(args[i + 1].trim());
            } else if ("-s".equals(args[i])) {
                this.scoreThreshold = Integer.parseInt(args[i + 1].trim());
            }
        }
        if (isEmpty(this.group)) {
            this.group = getString("auto.group", "");
        }
        if (isEmpty(this.apiUrl)) {
            this.apiUrl = getString("auto.url", DEFAULT_URL);
        }
        if (isEmpty(this.apiKey)) {
            this.apiKey = getString("auto.token", "");
            if (this.apiKey.length() == 0) {
                this.usage("auto-token is not defined");
            }
        }
        if (file == null) {
            String val = getString("auto.file", "");
            if (val.length() == 0) {
                this.usage("auto-file is not defined");
            }
            this.file = new File(val);
        }
        if (!file.exists()) {
            this.usage("auto-file doesn't exist, please specify full path");
        }

        if (artifactsDir == null) {
            String val = getString("auto.dir", "");
            if (val.length() == 0) {
                this.usage("auto-dir is not defined");
            }
            this.artifactsDir = new File(val);
        }
        if (!artifactsDir.exists()) {
            artifactsDir.mkdirs();
        }
        if (this.waitMinutes == 0) {
            this.waitMinutes = parseInt("auto.wait");
        }
        if (this.scoreThreshold == 0) {
            this.scoreThreshold = parseInt("auto.score");
        }
    }

    @Override
    public void info(Object msg) {
        System.out.println(new Date() + " " + PLUGIN_NAME + " " + msg);
    }

    @Override
    public void error(Object msg) {
        System.err.println(new Date() + " " + PLUGIN_NAME + " " + msg);

    }
}
