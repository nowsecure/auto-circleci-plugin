package com.nowsecure.auto.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Message {

    public static List<String> fromJson(String json) throws ParseException, IOException {
        if (json.startsWith("{")) {
            throw new IOException("Failed to find message " + json);
        }
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(json);
        //
        List<String> msgs = new ArrayList<String>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String msg = (String) jsonObject.get("message");
            if (msg != null) {
                msgs.add(msg);
            }
        }

        return msgs;
    }

}