package com.example.studymatch;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RestDbService {

    String API_KEY = "3547617a1ef1d0de6383bc2ef680f748eeaa8";

    @GET("rest/studygroups")
    Call<List<StudyGroup>> getStudyGroups();

    @GET("rest/studygroups/{id}")
    Call<StudyGroup> getStudyGroup(@Path("id") String id);

    @POST("rest/studygroups")
    Call<StudyGroup> createStudyGroup(@Body StudyGroup group);

    @PUT("rest/studygroups/{id}")
    Call<StudyGroup> updateStudyGroup(@Path("id") String id, @Body StudyGroup group);

    @DELETE("rest/studygroups/{id}")
    Call<Void> deleteStudyGroup(@Path("id") String id);
}