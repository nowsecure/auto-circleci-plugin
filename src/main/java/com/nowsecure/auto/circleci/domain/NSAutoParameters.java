package com.nowsecure.auto.circleci.domain;

import java.io.File;

public interface NSAutoParameters {

    String getApiUrl();

    String getApiKey();

    String getGroup();

    File getArtifactsDir();

    File getFile();

    int getWaitMinutes();

    int getScoreThreshold();

    void info(Object obj);

    void error(Object obj);

}