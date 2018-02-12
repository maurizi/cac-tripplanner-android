package com.gophillygo.app.di;

import android.app.Application;
import android.arch.persistence.room.Room;

import com.gophillygo.app.data.DestinationDao;
import com.gophillygo.app.data.DestinationWebservice;
import com.gophillygo.app.data.GpgDatabase;
import com.gophillygo.app.data.networkresource.LiveDataCallAdapterFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Inject singleton dependencies for use across the app.
 *
 * Based on:
 * https://github.com/googlesamples/android-architecture-components/blob/e33782ba54ebe87f7e21e03542230695bc893818/GithubBrowserSample/app/src/main/java/com/android/example/github/di/AppModule.java
 */

@Module(includes = ViewModelModule.class)
public class AppModule {
    @Singleton
    @Provides
    DestinationWebservice provideDestinationWebservice() {
        return new Retrofit.Builder()
                .baseUrl("https://gophillygo.org/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build()
                .create(DestinationWebservice.class);
    }

    @Singleton
    @Provides
    GpgDatabase provideDatabase(Application app) {
        return Room.databaseBuilder(app, GpgDatabase.class, "gpg-database").build();
    }

    @Singleton
    @Provides
    DestinationDao provideDestinationDao(GpgDatabase db) {
        return db.destinationDao();
    }
}
