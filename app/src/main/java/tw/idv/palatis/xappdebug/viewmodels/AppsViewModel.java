package tw.idv.palatis.xappdebug.viewmodels;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppsViewModel extends ViewModel {
    private final MutableLiveData<List<PackageInfo>> mInstalledPackages = new MutableLiveData<>();

    public AppsViewModel() {
        mInstalledPackages.setValue(new ArrayList<>());
    }

    public LiveData<List<PackageInfo>> getInstalledPackages() {
        return mInstalledPackages;
    }

    public void updatePackageList(final Context context) {
        final PackageManager pm = context.getPackageManager();
        final List<PackageInfo> packages = pm.getInstalledPackages(0);
        mInstalledPackages.setValue(packages);
    }
}