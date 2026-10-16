package com.example.studymatch;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface RestDbService {

    String API_KEY = "x-apikey: 44d5674bd1ebc2ea68b43181b7a6df8d3b432";

    // GET: Listar vagas
    @Headers({API_KEY, "Content-Type: application/json"})
    @GET("rest/studygroups")
    Call<List<StudyGroup>> getStudyGroups();

    // POST: Criar nova vaga
    @Headers({API_KEY, "Content-Type: application/json"})
    @POST("rest/studygroups")
    Call<StudyGroup> createStudyGroup(@Body StudyGroup group);
}