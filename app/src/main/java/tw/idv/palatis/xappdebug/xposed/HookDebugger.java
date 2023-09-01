package tw.idv.palatis.xappdebug.xposed;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static tw.idv.palatis.xappdebug.BuildConfig.APPLICATION_ID;
import static tw.idv.palatis.xappdebug.Constants.LOG_TAG;

import android.os.Bundle;
import android.os.Debug;
import android.util.Log;

import androidx.annotation.Keep;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@Keep
public class HookDebugger implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (APPLICATION_ID.equals(lpparam.packageName))
            return;

        _hookActivityConstructor(lpparam);
    }

    private void _hookActivityConstructor(XC_LoadPackage.LoadPackageParam lpparam) {
        findAndHookConstructor(
                "android.app.Activity",
                lpparam.classLoader,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(LOG_TAG + ": (" + lpparam.packageName + ") Waiting for a debugger...");
                        Debug.waitForDebugger();
                    }
                }
        );
    }
}
