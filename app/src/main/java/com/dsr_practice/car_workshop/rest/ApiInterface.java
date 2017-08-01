package com.dsr_practice.car_workshop.rest;

import com.dsr_practice.car_workshop.models.post.CloseJobPost;
import com.dsr_practice.car_workshop.models.common.Job;
import com.dsr_practice.car_workshop.models.common.Mark;
import com.dsr_practice.car_workshop.models.common.Model;
import com.dsr_practice.car_workshop.models.common.Task;
import com.dsr_practice.car_workshop.models.post.TaskPost;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiInterface {
    // GET requests
    @GET("marks")
    Call<List<Mark>> getMarks();

    @GET("models")
    Call<List<Model>> getModels();

    @GET("jobs")
    Call<List<Job>> getJobs();

    @GET("tasks")
    Call<List<Task>> getTasks();

    // POST requests
    @POST("task/create")
    Call<ResponseBody> createTask(@Body TaskPost taskPost);

    @POST("task/close")
    Call<ResponseBody> closeTask(@Body Integer id);

    @POST("job/close")
    Call<ResponseBody> closeJobInTask(@Body CloseJobPost closeJobPost);
}
