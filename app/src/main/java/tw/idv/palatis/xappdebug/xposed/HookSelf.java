package tw.idv.palatis.xappdebug.xposed;

import static de.robv.android.xposed.XC_MethodReplacement.returnConstant;
import static de.robv.android.xposed.XposedBridge.getXposedVersion;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static tw.idv.palatis.xappdebug.BuildConfig.APPLICATION_ID;

import androidx.annotation.Keep;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import tw.idv.palatis.xappdebug.MainApplication;

@Keep
public class HookSelf implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!APPLICATION_ID.equals(lpparam.packageName))
            return;

        findAndHookMethod(
                MainApplication.class.getName(),
                lpparam.classLoader,
                "getActiveXposedVersion",
                returnConstant(getXposedVersion())
        );
    }
}
