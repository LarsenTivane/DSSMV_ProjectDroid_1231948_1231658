package com.example.studymatch;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RestDbService {

    String API_KEY_HEADER = "x-apikey: 44d5674bd1ebc2ea68b43181b7a6df8d3b432";
    String CONTENT_TYPE = "Content-Type: application/json";

    // 1. GET: Listar todos os grupos de estudo
    @Headers({"x-apikey: 44d5674bd1ebc2ea68b43181b7a6df8d3b432", "Content-Type: application/json"})
    @GET("rest/studygroups")
    Call<List<StudyGroup>> getStudyGroups();

    // 2. POST: Criar uma nova vaga/grupo de estudo
    @Headers({"x-apikey: 44d5674bd1ebc2ea68b43181b7a6df8d3b432", "Content-Type: application/json"})
    @POST("rest/studygroups")
    Call<StudyGroup> createStudyGroup(@Body StudyGroup group);

    // 3. PUT: Atualizar os dados de uma vaga existente (ex.: decrementar vagas)
    @Headers({"x-apikey: 44d5674bd1ebc2ea68b43181b7a6df8d3b432", "Content-Type: application/json"})
    @PUT("rest/studygroups/{id}")
    Call<StudyGroup> updateStudyGroup(@Path("id") String id, @Body StudyGroup group);

    // 4. DELETE: Fechar/eliminar uma vaga pelo seu ID único
    @Headers({"x-apikey: 44d5674bd1ebc2ea68b43181b7a6df8d3b432", "Content-Type: application/json"})
    @DELETE("rest/studygroups/{id}")
    Call<Void> deleteStudyGroup(@Path("id") String id);
}