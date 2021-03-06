package com.gophillygo.app.data.networkresource;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Based on:
 *
 * https://github.com/googlesamples/android-architecture-components/blob/e33782ba54ebe87f7e21e03542230695bc893818/GithubBrowserSample/app/src/main/java/com/android/example/github/util/LiveDataCallAdapter.java
 *
 * A Retrofit adapter that converts the Call into a LiveData of ApiResponse.
 * @param <R>
 */
@SuppressWarnings("WeakerAccess")
public class LiveDataCallAdapter<R> implements CallAdapter<R, LiveData<ApiResponse<R>>> {
    private static final String LOG_LABEL = "LiveDataCallAdapter";

    private final Type responseType;
    public LiveDataCallAdapter(Type responseType) {
        this.responseType = responseType;
    }

    @Override
    public Type responseType() {
        return responseType;
    }

    @Override
    public LiveData<ApiResponse<R>> adapt(@NonNull Call<R> call) {
        return new LiveData<ApiResponse<R>>() {
            final AtomicBoolean started = new AtomicBoolean(false);
            @Override
            protected void onActive() {
                Log.d(LOG_LABEL, "onActive");
                super.onActive();
                if (started.compareAndSet(false, true)) {
                    call.enqueue(new Callback<R>() {
                        @Override
                        public void onResponse(@NonNull Call<R> call, @NonNull Response<R> response) {
                            Log.d(LOG_LABEL, "onResponse");
                            postValue(new ApiResponse<>(response));
                        }

                        @Override
                        public void onFailure(@NonNull Call<R> call, @NonNull Throwable throwable) {
                            Log.e(LOG_LABEL, "onFailure");
                            postValue(new ApiResponse<>(throwable));
                        }
                    });
                }
            }
        };
    }
}
