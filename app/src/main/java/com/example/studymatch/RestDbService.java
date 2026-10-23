package com.example.studymatch;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RestDbService {

    String API_KEY = "x-apikey";
    String API_KEY_VALUE = "44d5674bd1ebc2ea68b43181b7a6df8d3b432";

    @Headers({
            API_KEY + ": " + API_KEY_VALUE,
            "Content-Type: application/json"
    })
    @GET("rest/studygroups")
    Call<List<StudyGroup>> getStudyGroups();

    @Headers({
            API_KEY + ": " + API_KEY_VALUE,
            "Content-Type: application/json"
    })
    @POST("rest/studygroups")
    Call<StudyGroup> createStudyGroup(@Body StudyGroup group);

    @Headers({
            API_KEY + ": " + API_KEY_VALUE,
            "Content-Type: application/json"
    })
    @retrofit2.http.DELETE("rest/studygroups/{id}")
    Call<Void> deleteStudyGroup(@retrofit2.http.Path("id") String id);
}