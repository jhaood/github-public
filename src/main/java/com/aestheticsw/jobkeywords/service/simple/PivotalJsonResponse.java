/*
 * Copyright 2015 Jim Alexander, Aesthetic Software, Inc. (jhaood@gmail.com)
 * Apache Version 2 license: http://www.apache.org/licenses/LICENSE-2.0
 */
package com.aestheticsw.jobkeywords.service.simple;

/**
 * This data model class lets the SimpleRestService experiment with JSON desearialization.<p/>
 * 
 * This class also allows the SimpleRestController experiment with JSON and XML serialization.
 * 
 * @author Jim Alexander (jhaood@gmail.com)
 */
public class PivotalJsonResponse {

    private String name;
    private String about;
    private String phone;
    private String website;

    public String getName() {
        return name;
    }

    public String getAbout() {
        return about;
    }

    public String getPhone() {
        return phone;
    }

    public String getWebsite() {
        return website;
    }

}
