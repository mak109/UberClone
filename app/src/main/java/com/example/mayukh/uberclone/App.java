package com.example.mayukh.uberclone;

import com.parse.Parse;
import android.app.Application;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("GaFea4ZA4Qxw2ZhMFyJwba2oBNgf5khwatiYT3lm")
                // if defined
                .clientKey("e9sFIv4i9aQxV0vrosrbjxZnS1O3F4Q3CiRUQVN8")
                .server("https://parseapi.back4app.com/")
                .build()
        );
    }
}