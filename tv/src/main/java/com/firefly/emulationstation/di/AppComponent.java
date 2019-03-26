package com.firefly.emulationstation.di;

import android.app.Application;

import com.firefly.emulationstation.ESApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

/**
 * Created by rany on 17-10-23.
 */

@Singleton
@Component(modules = {
        AndroidInjectionModule.class,
        AppModule.class,
        DataModule.class,
        ActivityBindingModule.class,
        FragmentBindingModule.class,
        OtherBindingModule.class
})
public interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);
        Builder appModule(AppModule appModule);
        AppComponent build();
    }
    void inject(ESApplication app);
}
