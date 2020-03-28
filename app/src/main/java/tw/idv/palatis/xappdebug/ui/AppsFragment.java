package tw.idv.palatis.xappdebug.ui;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.collection.SparseArrayCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import tw.idv.palatis.xappdebug.Configuration;
import tw.idv.palatis.xappdebug.R;

import static android.content.pm.ApplicationInfo.FLAG_SYSTEM;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;
import static tw.idv.palatis.xappdebug.Constants.PREF_KEY_SHOW_DEBUGGABLE_FIRST;
import static tw.idv.palatis.xappdebug.Constants.PREF_KEY_SHOW_SYSTEM;
import static tw.idv.palatis.xappdebug.Constants.PREF_KEY_SORT_ORDER;
import static tw.idv.palatis.xappdebug.Constants.SORT_ORDER_INSTALL_TIME;
import static tw.idv.palatis.xappdebug.Constants.SORT_ORDER_LABEL;
import static tw.idv.palatis.xappdebug.Constants.SORT_ORDER_PACKAGE_NAME;
import static tw.idv.palatis.xappdebug.Constants.SORT_ORDER_UPDATE_TIME;

public class AppsFragment extends Fragment {

    private AppsViewModel mAppsViewModel;
    private SharedPreferences mSharedPreferences;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        mAppsViewModel = new ViewModelProvider(requireActivity()).get(AppsViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_apps, container, false);
        final InstalledPackageAdapter adapter = new InstalledPackageAdapter();
        final RecyclerView packages = root.findViewById(R.id.packages);
        packages.setAdapter(adapter);

        mAppsViewModel.getInstalledPackages().observe(
                getViewLifecycleOwner(),
                adapter::updateInstalledPackages
        );
        mAppsViewModel.updatePackageList(requireContext());
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.apps, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        switch (mSharedPreferences.getInt(PREF_KEY_SORT_ORDER, SORT_ORDER_LABEL)) {
            case SORT_ORDER_LABEL:
                menu.findItem(R.id.action_sort_label).setChecked(true);
                break;
            case SORT_ORDER_PACKAGE_NAME:
                menu.findItem(R.id.action_sort_package_name).setChecked(true);
                break;
            case SORT_ORDER_INSTALL_TIME:
                menu.findItem(R.id.action_sort_install_time).setChecked(true);
                break;
            case SORT_ORDER_UPDATE_TIME:
                menu.findItem(R.id.action_sort_update_time).setChecked(true);
                break;
        }
        menu.findItem(R.id.action_debuggable_first)
                .setChecked(mSharedPreferences.getBoolean(PREF_KEY_SHOW_DEBUGGABLE_FIRST, false));
        menu.findItem(R.id.action_show_system)
                .setChecked(mSharedPreferences.getBoolean(PREF_KEY_SHOW_SYSTEM, false));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mAppsViewModel.updatePackageList(requireContext());
                return true;
            case R.id.action_sort_label:
                mSharedPreferences.edit().putInt(PREF_KEY_SORT_ORDER, SORT_ORDER_LABEL).apply();
                requireActivity().invalidateOptionsMenu();
                return true;
            case R.id.action_sort_package_name:
                mSharedPreferences.edit().putInt(PREF_KEY_SORT_ORDER, SORT_ORDER_PACKAGE_NAME).apply();
                requireActivity().invalidateOptionsMenu();
                return true;
            case R.id.action_sort_install_time:
                mSharedPreferences.edit().putInt(PREF_KEY_SORT_ORDER, SORT_ORDER_INSTALL_TIME).apply();
                requireActivity().invalidateOptionsMenu();
                return true;
            case R.id.action_sort_update_time:
                mSharedPreferences.edit().putInt(PREF_KEY_SORT_ORDER, SORT_ORDER_UPDATE_TIME).apply();
                requireActivity().invalidateOptionsMenu();
                return true;
            case R.id.action_debuggable_first:
                mSharedPreferences.edit()
                        .putBoolean(PREF_KEY_SHOW_DEBUGGABLE_FIRST, !mSharedPreferences.getBoolean(PREF_KEY_SHOW_DEBUGGABLE_FIRST, false))
                        .apply();
                requireActivity().invalidateOptionsMenu();
                return true;
            case R.id.action_show_system:
                mSharedPreferences.edit()
                        .putBoolean(PREF_KEY_SHOW_SYSTEM, !mSharedPreferences.getBoolean(PREF_KEY_SHOW_SYSTEM, false))
                        .apply();
                requireActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class InstalledPackageAdapter extends RecyclerView.Adapter<InstalledPackageAdapter.ViewHolder>
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PREF_KEY_SORT_ORDER:
                case PREF_KEY_SHOW_DEBUGGABLE_FIRST:
                case PREF_KEY_SHOW_SYSTEM:
                    filterAndSort();
                    notifyDataSetChanged();
                    break;
            }
        }

        private class PackageInfoCache {
            public PackageInfo raw;
            private CharSequence mLabel = null;
            private Drawable mIcon = null;

            public PackageInfoCache(PackageInfo pkg) {
                raw = pkg;
            }

            public Drawable getIcon(final PackageManager pm) {
                if (mIcon == null)
                    mIcon = pm.getApplicationIcon(raw.applicationInfo);
                return mIcon;
            }

            public CharSequence getLabel(final PackageManager pm) {
                if (mLabel == null)
                    mLabel = pm.getApplicationLabel(raw.applicationInfo);
                return mLabel;
            }

            public String getPackageName() {
                return raw.packageName;
            }

            public boolean isDebuggable() {
                return Configuration.isEnabled(getPackageName());
            }

            public boolean isSystemApp() {
                return (raw.applicationInfo.flags & FLAG_SYSTEM) == FLAG_SYSTEM;
            }

            public long getInstallTime() {
                return raw.firstInstallTime;
            }

            public long getUpdateTime() {
                return raw.lastUpdateTime;
            }
        }

        private List<PackageInfoCache> mInstalledPackages = new ArrayList<>();
        private List<PackageInfoCache> mFilteredPackages = new ArrayList<>();

        InstalledPackageAdapter() {
            setHasStableIds(true);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(mFilteredPackages.get(position));
        }

        @Override
        public int getItemCount() {
            return mFilteredPackages.size();
        }

        @Override
        public long getItemId(int position) {
            return mFilteredPackages.get(position).getPackageName().hashCode();
        }

        @Override
        public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
            mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
            mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }

        public void updateInstalledPackages(List<PackageInfo> packages) {
            final SparseArrayCompat<PackageInfoCache> map = new SparseArrayCompat<>(mInstalledPackages.size());
            for (PackageInfoCache pkg : mInstalledPackages)
                map.put(pkg.getPackageName().hashCode(), pkg);

            mInstalledPackages = new ArrayList<>(packages.size());
            for (PackageInfo pkg : packages) {
                final PackageInfoCache pkg2 = map.containsKey(pkg.packageName.hashCode()) ?
                        map.get(pkg.packageName.hashCode()) :
                        new PackageInfoCache(pkg);
                pkg2.raw = pkg;
                mInstalledPackages.add(pkg2);
            }

            filterAndSort();
            notifyDataSetChanged();
        }

        private void filterAndSort() {
            mFilteredPackages.clear();
            if (mSharedPreferences.getBoolean(PREF_KEY_SHOW_SYSTEM, false)) {
                mFilteredPackages.addAll(mInstalledPackages);
            } else {
                for (PackageInfoCache pkg : mInstalledPackages)
                    if (!pkg.isSystemApp())
                        mFilteredPackages.add(pkg);
            }

            final PackageManager pm = requireContext().getPackageManager();
            Comparator<PackageInfoCache> orderComparator = null;
            switch (mSharedPreferences.getInt(PREF_KEY_SORT_ORDER, SORT_ORDER_LABEL)) {
                case SORT_ORDER_LABEL:
                    orderComparator = (pkg1, pkg2) -> pkg1.getLabel(pm).toString().compareTo(pkg2.getLabel(pm).toString());
                    break;
                case SORT_ORDER_PACKAGE_NAME:
                    orderComparator = (pkg1, pkg2) -> pkg1.getPackageName().compareTo(pkg2.getPackageName());
                    break;
                case SORT_ORDER_INSTALL_TIME:
                    orderComparator = (pkg1, pkg2) -> Long.compare(pkg1.getInstallTime(), pkg2.getInstallTime());
                    orderComparator = orderComparator.reversed();
                    break;
                case SORT_ORDER_UPDATE_TIME:
                    orderComparator = (pkg1, pkg2) -> Long.compare(pkg1.getUpdateTime(), pkg2.getUpdateTime());
                    orderComparator = orderComparator.reversed();
                    break;
            }

            if (mSharedPreferences.getBoolean(PREF_KEY_SHOW_DEBUGGABLE_FIRST, false)) {
                final Comparator<PackageInfoCache> debuggableComparator = (pkg1, pkg2) -> {
                    if (pkg1.isDebuggable() && !pkg2.isDebuggable())
                        return 1;
                    if (!pkg1.isDebuggable() && pkg2.isDebuggable())
                        return -1;
                    return 0;
                };
                mFilteredPackages.sort(debuggableComparator.reversed().thenComparing(orderComparator));
            } else {
                mFilteredPackages.sort(orderComparator);
            }
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final AppCompatImageView icon;
            private final MaterialTextView applicationLabel;
            private final MaterialTextView packageName;
            private final SwitchMaterial toggle;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                applicationLabel = itemView.findViewById(R.id.application_label);
                packageName = itemView.findViewById(R.id.package_name);
                toggle = itemView.findViewById(R.id.toggle);
            }

            public void bind(final PackageInfoCache pkg) {
                final PackageManager pm = itemView.getContext().getPackageManager();
                icon.setImageDrawable(pkg.getIcon(pm));
                applicationLabel.setText(pkg.getLabel(pm));
                packageName.setText(pkg.getPackageName());
                toggle.setOnCheckedChangeListener(null);
                toggle.setChecked(Configuration.isEnabled(pkg.getPackageName()));
                toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked)
                        Configuration.add(pkg.getPackageName());
                    else
                        Configuration.remove(pkg.getPackageName());
                    Snackbar snackbar = Snackbar.make(getView(), R.string.restart_required, LENGTH_SHORT);
                    snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
                    snackbar.show();
                });

                if (pkg.isSystemApp())
                    itemView.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.material_on_primary_emphasis_high_type, itemView.getContext().getTheme()));
                else
                    itemView.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.transparent, itemView.getContext().getTheme()));
            }
        }
    }
}
