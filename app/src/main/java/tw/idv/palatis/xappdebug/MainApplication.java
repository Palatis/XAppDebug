package tw.idv.palatis.xappdebug;

import static tw.idv.palatis.xappdebug.Constants.LOG_TAG;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Keep;

public class MainApplication extends Application {
    @Keep
    public static int getActiveXposedVersion() {
        Log.d(LOG_TAG, "Xposed framework is inactive.");
        return -1;
    }
}
