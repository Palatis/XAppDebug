package tw.idv.palatis.xappdebug.xposed;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.os.UserHandle;

import java.util.Locale;

import androidx.annotation.Keep;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.SELinuxHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.util.Log.getStackTraceString;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static tw.idv.palatis.xappdebug.Constants.CONFIG_PATH_FORMAT;
import static tw.idv.palatis.xappdebug.Constants.LOG_TAG;

@Keep
public class HookMain implements IXposedHookLoadPackage {

    // taken from Zygote.java
    // https://android.googlesource.com/platform/frameworks/base.git/+/master/core/java/com/android/internal/os/Zygote.java
    private static final int DEBUG_ENABLE_JDWP = 1;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"android".equals(lpparam.packageName))
            return;

        findAndHookMethod(
                "com.android.server.pm.PackageManagerService",
                lpparam.classLoader,
                "getPackageInfo",
                String.class, int.class, int.class, /* packageName, flags, userId */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            final PackageInfo packageInfo = (PackageInfo) param.getResult();

                            if (packageInfo != null) {
                                if (!isDebuggable(packageInfo.packageName, (int) param.args[2]))
                                    return;

                                packageInfo.applicationInfo.flags |= FLAG_DEBUGGABLE;
                                param.setResult(packageInfo);
                            }
                        } catch (Exception e) {
                            XposedBridge.log(LOG_TAG + ": " + getStackTraceString(e));
                        }
                    }
                }
        );

        hookAllMethods(
                android.os.Process.class,
                "start",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        final String niceName = (String) param.args[1];
                        final int uid = (int) param.args[2];
                        final int runtimeFlags = (int) param.args[5];

                        final int user = UserHandle.getUserHandleForUid(uid).hashCode();
                        if (isDebuggable(niceName, user))
                            param.args[5] = runtimeFlags | DEBUG_ENABLE_JDWP;
                    }
                }
        );
    }

    @SuppressLint("SdCardPath")
    private static boolean isDebuggable(final String packageName, int user) {
        final String path = String.format(
                Locale.getDefault(),
                CONFIG_PATH_FORMAT,
                user, packageName
        );
        return SELinuxHelper.getAppDataFileService().checkFileExists(path);
    }

}
