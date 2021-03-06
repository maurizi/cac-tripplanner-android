package com.gophillygo.app.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.gophillygo.app.data.DestinationViewModel;
import com.gophillygo.app.data.EventViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * Support dependency injection of ViewModels.
 *
 * Based on:
 * https://github.com/googlesamples/android-architecture-components/blob/e33782ba54ebe87f7e21e03542230695bc893818/GithubBrowserSample/app/src/main/java/com/android/example/github/di/ViewModelModule.java
 */

@SuppressWarnings("unused")
@Module
public abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(DestinationViewModel.class)
    abstract ViewModel bindDestinationViewModel(DestinationViewModel destinationViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EventViewModel.class)
    abstract ViewModel bindEventViewModel(EventViewModel eventViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(GpgViewModelFactory factory);
}
