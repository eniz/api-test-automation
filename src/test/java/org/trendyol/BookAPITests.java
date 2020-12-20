package org.trendyol;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;

public class BookAPITests extends ApiTestCase {

    @Test(priority = 1, description = "Verify that the API starts with an empty store.")
    public void checkEmptyStoreAtAPIStarts() {
        request
                .when()
                .get("/books")
                .then()
                .body("$", Matchers.hasSize(0));
    }

    @Test(description = "Verify that title and author are required fields.")
    public void checkTitleAndAuthorRequired(){
        JSONObject authorParam = new JSONObject();
        authorParam.put("author", "Sabahattin Ali");

        request
                .contentType("application/json")
                .body(authorParam.toString())
                .when()
                .put("/books")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .assertThat()
                .body(equalTo("{\"error\":\"Field 'title' is required\"}"));

        JSONObject titleParam = new JSONObject();
        titleParam.put("title", "İçimizdeki Şeytan");

        request
                .contentType("application/json")
                .body(titleParam.toString())
                .when()
                .put("/books")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .assertThat()
                .body(equalTo("{\"error\":\"Field 'author' is required\"}"));

    }

    @Test(description = "Verify that title and author cannot be empty.")
    public void checkTitleAndAuthorNotEmpty(){
        JSONObject titleEmptyParams = new JSONObject();
        titleEmptyParams.put("title", "");
        titleEmptyParams.put("author","Orhan Pamuk");

        request
                .contentType("application/json")
                .body(titleEmptyParams.toString())
                .when()
                .put("/books")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .assertThat()
                .body(equalTo("{\"error\":\"Field 'title' cannot be empty.\"}"));

        JSONObject authorEmptyParams = new JSONObject();
        authorEmptyParams.put("title", "Kuyucaklı Yusuf");
        authorEmptyParams.put("author", "");

        request
                .contentType("application/json")
                .body(authorEmptyParams.toString())
                .when()
                .put("/books")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .assertThat()
                .body(equalTo("{\"error\":\"Field 'author' cannot be empty.\"}"));
    }


    @Test(description = "Verify that the id field is read−only.")
    public void checkIdReadOnly() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("id", 10);
        requestParams.put("title", "Kumarbaz");
        requestParams.put("author", "Dostoyevski");

                request
                        .contentType("application/json")
                        .body(requestParams.toString())
                        .when()
                        .put("/books")
                        .then()
                        .statusCode(HttpStatus.SC_BAD_REQUEST)
                        .assertThat()
                        .body(equalTo("{\"error\":\"Field 'id' is readonly.\"}"));
    }

    @Test(description = "Verify that can create a new book via PUT.")
    public void checkCreateNewBook() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("title", "1984");
        requestParams.put("author", "George Orwell");

        String putResponseID;
        String putResponseTitle;
        String putResponseAuthor;

        Response putResponse =
                request
                        .contentType("application/json")
                        .body(requestParams.toString())
                        .when()
                        .put("/books");

        Assert.assertEquals(HttpStatus.SC_CREATED, putResponse.getStatusCode());

        //Read PUT response
        JSONObject putResponseParams = new JSONObject(putResponse.asString());
        putResponseID = putResponseParams.get("id").toString();
        putResponseTitle = putResponseParams.get("title").toString();
        putResponseAuthor = putResponseParams.get("author").toString();

        //Assert response values
        Assert.assertEquals(putResponseTitle, "1984");
        Assert.assertEquals(putResponseAuthor, "George Orwell");

        //STEP 2: GET the created book

        String getResponseID;
        String getResponseTitle;
        String getResponseAuthor;

        Response getResponse =
                request
                        .contentType("application/json")
                        .when()
                        .get("/books" + "/" + putResponseID);

        //Make sure it was received
        Assert.assertEquals(200,getResponse.getStatusCode());

        //Read GET response
        JSONObject getResponseParams = new JSONObject(getResponse.asString());
        getResponseID = getResponseParams.get("id").toString();
        getResponseTitle = getResponseParams.get("title").toString();
        getResponseAuthor = getResponseParams.get("author").toString();

        //Assertions
        Assert.assertEquals(putResponseID, getResponseID);
        Assert.assertEquals(putResponseAuthor, getResponseAuthor);
        Assert.assertEquals(putResponseTitle, getResponseTitle);
    }

    @Test(description = "Verify that cannot create a duplicate book.")
    public void checkDuplicateBook(){
        String putRequestTitle = "Kar";
        String putRequestAuthor = "Orhan Pamuk";

        JSONObject putRequestParams = new JSONObject();
        putRequestParams.put("title", putRequestTitle);
        putRequestParams.put("author",putRequestAuthor);

        request
                .contentType("application/json")
                .body(putRequestParams.toString())
                .when()
                .put("/books")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        request
                .contentType("application/json")
                .body(putRequestParams.toString())
                .when()
                .put("/books")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .assertThat()
                .body(equalTo("{\"error\":\"Another book with similar\n" +
                        "title and author already exists.\"}"));
    }
}
