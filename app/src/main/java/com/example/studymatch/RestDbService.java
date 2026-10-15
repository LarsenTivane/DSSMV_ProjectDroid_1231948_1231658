package com.example.studymatch;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface RestDbService {
    @Headers({
            "x-apikey: 44d5674bd1ebc2ea68b43181b7a6df8d3b432",
            "Content-Type: application/json"
    })
    @GET("rest/studygroups")
    Call<List<StudyGroup>> getStudyGroups();
}