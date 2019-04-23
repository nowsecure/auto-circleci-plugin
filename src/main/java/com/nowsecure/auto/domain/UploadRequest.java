package com.nowsecure.auto.domain;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UploadRequest extends MetadataRequest {
    private boolean analyzed;

    public UploadRequest() {
    }

    public static UploadRequest fromJson(String json) throws ParseException, IOException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json);
        // for error message
        String name = (String) jsonObject.get("name");
        String message = (String) jsonObject.get("message");
        if (name != null && message != null) {
            throw new IOException(name + " " + message);
        }
        //
        UploadRequest request = new UploadRequest();
        if (jsonObject.get("title") != null) {
            request.setApplication((String) jsonObject.get("title"));
        } else if (jsonObject.get("name") != null) {
            request.setApplication((String) jsonObject.get("name"));
        }
        request.setPlatform((String) jsonObject.get("platform"));
        request.setPackageId((String) jsonObject.get("package"));
        request.setBinary((String) jsonObject.get("digest"));
        if (jsonObject.get("analyzed") != null) {
            request.setAnalyzed((Boolean) jsonObject.get("analyzed"));
        }
        //
        // if (request.getPackageId() == null ||
        // request.getPackageId().isEmpty()) {
        // throw new IOException("Binary file is not valid, package-id could not
        // be found in:\n" + json + "\n");
        // }
        //
        if (request.getBinary() == null || request.getBinary().isEmpty()) {
            throw new IOException("Binary file is not valid, digest could not be found in:\n" + json + "\n");
        }
        return request;
    }

    public boolean isAnalyzed() {
        return analyzed;
    }

    public void setAnalyzed(boolean analyzed) {
        this.analyzed = analyzed;
    }

    @Override
    public String toString() {
        return "AssessmentRequest [analyzed=" + analyzed + ", app=" + getApplication() + ", version=" + getVersion()
               + ", platform=" + getPlatform() + ", package=" + getPackageId() + ", binary=" + getBinary() + "]";
    }

}
