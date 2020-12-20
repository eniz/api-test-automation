package org.trendyol;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeSuite;

public class ApiTestCase {

    // Mock Api BaseURL (You can modify your rest api's baseUrl)
    private final static String API_ROOT = "/api";

    RequestSpecification request;

    @BeforeSuite
    public void beforeSuite(){
        RestAssured.baseURI = API_ROOT;
        request = RestAssured.given();
    }
}
