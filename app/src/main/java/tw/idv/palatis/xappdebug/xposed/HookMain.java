package tw.idv.palatis.xappdebug.xposed;

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.util.Log.getStackTraceString;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static tw.idv.palatis.xappdebug.Constants.CONFIG_PATH_FORMAT;
import static tw.idv.palatis.xappdebug.Constants.LOG_TAG;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.StrictMode;
import android.os.UserHandle;

import androidx.annotation.Keep;

import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.SELinuxHelper;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@Keep
public class HookMain implements IXposedHookLoadPackage {

    // taken from Zygote.java
    // https://android.googlesource.com/platform/frameworks/base.git/+/master/core/java/com/android/internal/os/Zygote.java
    private static final int DEBUG_ENABLE_JDWP = 1;

    private static final String PACKAGE_MANAGER_SERVICE_CLASS = "com.android.server.pm.PackageManagerService";

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"android".equals(lpparam.packageName))
            return;

        findAndHookMethod(
                PACKAGE_MANAGER_SERVICE_CLASS,
                lpparam.classLoader,
                "getPackageInfo",
                String.class, int.class, int.class, /* packageName, flags, userId */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            final int userId = (int) param.args[2];
                            final PackageInfo info = (PackageInfo) param.getResult();
                            if (info == null)
                                return;
                            if (!isDebuggable(info.packageName, userId))
                                return;
                            info.applicationInfo.flags |= FLAG_DEBUGGABLE;
                        } catch (Exception e) {
                            XposedBridge.log(LOG_TAG + ": " + getStackTraceString(e));
                        }
                    }
                }
        );

        findAndHookMethod(
                PACKAGE_MANAGER_SERVICE_CLASS,
                lpparam.classLoader,
                "getApplicationInfo",
                String.class, int.class, int.class, /* packageName, flags, userId */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            final int userId = (int) param.args[2];
                            final ApplicationInfo appInfo = (ApplicationInfo) param.getResult();
                            if (appInfo == null)
                                return;
                            if (!isDebuggable(appInfo.packageName, userId))
                                return;
                            appInfo.flags |= FLAG_DEBUGGABLE;
                        } catch (Exception e) {
                            XposedBridge.log(LOG_TAG + ": " + getStackTraceString(e));
                        }
                    }
                }
        );

        findAndHookMethod(
                PACKAGE_MANAGER_SERVICE_CLASS,
                lpparam.classLoader,
                "getInstalledApplicationsListInternal",
                int.class, int.class, int.class, /* flags, userId, callingUid */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            final int userId = (int) param.args[1];
                            final List<ApplicationInfo> infos = (List<ApplicationInfo>) param.getResult();
                            if (infos == null)
                                return;
                            for (ApplicationInfo info : infos) {
                                if (isDebuggable(info.packageName, userId))
                                    info.flags |= FLAG_DEBUGGABLE;
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

        final StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        boolean state = SELinuxHelper.getAppDataFileService().checkFileExists(path);
        StrictMode.setThreadPolicy(oldPolicy);
        return state;
    }

}
