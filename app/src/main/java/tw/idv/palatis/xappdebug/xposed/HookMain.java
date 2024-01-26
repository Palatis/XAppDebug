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
import android.os.Build;
import android.os.Process;
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

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!"android".equals(lpparam.packageName))
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            _hookPostTiramisu(lpparam);
        } else {
            _hookPreTiramisu(lpparam);
        }
    }

    private void _hookPostTiramisu(final XC_LoadPackage.LoadPackageParam lpparam) {
        final String PM_CLASS = "com.android.server.pm.ComputerEngine";

        findAndHookMethod(
                PM_CLASS,
                lpparam.classLoader,
                "getPackageInfo",
                String.class, long.class, int.class, /* packageName, flags, userId */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            PackageInfo packageInfo = (PackageInfo) param.getResult();
                            if (packageInfo != null && packageInfo.applicationInfo != null)
                                checkAndMakeDebuggable(packageInfo.applicationInfo, packageInfo.packageName, (int) param.args[2]);
                        } catch (Exception e) {
                            XposedBridge.log(LOG_TAG + ": " + getStackTraceString(e));
                        }
                    }
                }
        );

        findAndHookMethod(
                PM_CLASS,
                lpparam.classLoader,
                "getApplicationInfo",
                String.class, long.class, int.class, /* packageName, flags, userId */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            ApplicationInfo appInfo = (ApplicationInfo) param.getResult();
                            if (appInfo != null)
                                checkAndMakeDebuggable(appInfo, ((ApplicationInfo) param.getResult()).packageName, (int) param.args[2]);
                        } catch (Exception e) {
                            XposedBridge.log(LOG_TAG + ": " + getStackTraceString(e));
                        }
                    }
                }
        );

        findAndHookMethod(
                PM_CLASS,
                lpparam.classLoader,
                "getInstalledApplications",
                long.class, int.class, int.class, /* flags, userId, callingUid */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            List<ApplicationInfo> infos = (List<ApplicationInfo>) param.getResult();
                            if (infos != null) {
                                for (ApplicationInfo info : infos) {
                                    checkAndMakeDebuggable(info, info.packageName, (int) param.args[1]);
                                }
                            }
                        } catch (Exception e) {
                            XposedBridge.log(LOG_TAG + ": " + getStackTraceString(e));
                        }
                    }
                }
        );

        hookAllMethods(
                Process.class,
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

    private void _hookPreTiramisu(final XC_LoadPackage.LoadPackageParam lpparam) {
        final String PM_CLASS = "com.android.server.pm.PackageManagerService";

        findAndHookMethod(
                PM_CLASS,
                lpparam.classLoader,
                "getPackageInfo",
                String.class, int.class, int.class, /* packageName, flags, userId */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            PackageInfo packageInfo = (PackageInfo) param.getResult();
                            if (packageInfo != null && packageInfo.applicationInfo != null)
                                checkAndMakeDebuggable(packageInfo.applicationInfo, packageInfo.packageName, (int) param.args[2]);
                        } catch (Exception e) {
                            XposedBridge.log(LOG_TAG + ": " + getStackTraceString(e));
                        }
                    }
                }
        );

        findAndHookMethod(
                PM_CLASS,
                lpparam.classLoader,
                "getApplicationInfo",
                String.class, int.class, int.class, /* packageName, flags, userId */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            ApplicationInfo appInfo = (ApplicationInfo) param.getResult();
                            if (appInfo != null)
                                checkAndMakeDebuggable(appInfo, ((ApplicationInfo) param.getResult()).packageName, (int) param.args[2]);
                        } catch (Exception e) {
                            XposedBridge.log(LOG_TAG + ": " + getStackTraceString(e));
                        }
                    }
                }
        );

        findAndHookMethod(
                PM_CLASS,
                lpparam.classLoader,
                "getInstalledApplicationsListInternal",
                int.class, int.class, int.class, /* flags, userId, callingUid */
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            List<ApplicationInfo> infos = (List<ApplicationInfo>) param.getResult();
                            if (infos != null) {
                                for (ApplicationInfo info : infos) {
                                    checkAndMakeDebuggable(info, info.packageName, (int) param.args[1]);
                                }
                            }
                        } catch (Exception e) {
                            XposedBridge.log(LOG_TAG + ": " + getStackTraceString(e));
                        }
                    }
                }
        );

        hookAllMethods(
                Process.class,
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

    private void checkAndMakeDebuggable(ApplicationInfo appInfo, String packageName, int user) {
        if (isDebuggable(packageName, user)) {
            appInfo.flags |= FLAG_DEBUGGABLE;
        }
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
