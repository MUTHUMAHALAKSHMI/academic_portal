package com.coetusstudio.academicportal;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class AcademicPortalApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dusb3151z");
        config.put("secure", true);
        MediaManager.init(this, config);
    }
}
