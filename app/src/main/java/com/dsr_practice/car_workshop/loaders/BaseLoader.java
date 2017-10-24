package com.dsr_practice.car_workshop.loaders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;

import com.dsr_practice.car_workshop.rest.ApiClient;
import com.dsr_practice.car_workshop.rest.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

abstract class BaseLoader<T> extends Loader<T> {

    ApiInterface apiInterface;

    @Nullable
    private T responseObject;

    BaseLoader(Context context) {
        super(context);
        this.apiInterface = ApiClient.getApi();
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (responseObject != null)
            deliverResult(responseObject);
        else
            forceLoad();
    }

    /**
     * Base object for implementation callbacks for data loading
     */
    Callback<T> baseCallbacks = new Callback<T>() {
        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            responseObject = response.body();
            deliverResult(responseObject);
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            deliverResult(null);
        }
    };
}
