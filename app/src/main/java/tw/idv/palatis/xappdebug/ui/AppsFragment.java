package tw.idv.palatis.xappdebug.ui;

import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;
import static android.content.pm.ApplicationInfo.FLAG_SYSTEM;
import static com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT;
import static tw.idv.palatis.xappdebug.Constants.PREF_KEY_SHOW_DEBUG;
import static tw.idv.palatis.xappdebug.Constants.PREF_KEY_SHOW_DEBUGGABLE_FIRST;
import static tw.idv.palatis.xappdebug.Constants.PREF_KEY_SHOW_SYSTEM;
import static tw.idv.palatis.xappdebug.Constants.PREF_KEY_SORT_ORDER;
import static tw.idv.palatis.xappdebug.Constants.SORT_ORDER_INSTALL_TIME;
import static tw.idv.palatis.xappdebug.Constants.SORT_ORDER_LABEL;
import static tw.idv.palatis.xappdebug.Constants.SORT_ORDER_PACKAGE_NAME;
import static tw.idv.palatis.xappdebug.Constants.SORT_ORDER_UPDATE_TIME;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.collection.SparseArrayCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import tw.idv.palatis.xappdebug.Configuration;
import tw.idv.palatis.xappdebug.R;

public class AppsFragment extends Fragment {

    private AppsViewModel mAppsViewModel;
    private SharedPreferences mSharedPreferences;
    private InstalledPackageAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());

        mAppsViewModel = new ViewModelProvider(requireActivity()).get(AppsViewModel.class);
        final View root = inflater.inflate(R.layout.fragment_apps, container, false);
        final RecyclerView packages = root.findViewById(R.id.packages);
        mAdapter = new InstalledPackageAdapter(requireContext().getPackageManager(), mSharedPreferences);
        packages.setAdapter(mAdapter);

        mAppsViewModel.getInstalledPackages().observe(
                getViewLifecycleOwner(),
                mAdapter::updateInstalledPackages
        );
        mAppsViewModel.updatePackageList(requireContext());
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.apps, menu);
        final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.setFilterQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.setFilterQuery(newText);
                return true;
            }
        });
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
        menu.findItem(R.id.action_show_debug)
                .setChecked(mSharedPreferences.getBoolean(PREF_KEY_SHOW_DEBUG, false));
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
            case R.id.action_show_debug:
                mSharedPreferences.edit()
                        .putBoolean(PREF_KEY_SHOW_DEBUG, !mSharedPreferences.getBoolean(PREF_KEY_SHOW_DEBUG, false))
                        .apply();
                requireActivity().invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class InstalledPackageAdapter extends RecyclerView.Adapter<InstalledPackageAdapter.ViewHolder>
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

        private static class PackageInfoCache {
            public PackageInfo pkg;

            private CharSequence mLabel = null;
            private Drawable mIcon = null;

            public PackageInfoCache(PackageInfo pkg) {
                this.pkg = pkg;
            }

            public Drawable getIcon(final PackageManager pm) {
                if (mIcon == null)
                    mIcon = pm.getApplicationIcon(pkg.applicationInfo);
                return mIcon;
            }

            public CharSequence getLabel(final PackageManager pm) {
                if (mLabel == null)
                    mLabel = pm.getApplicationLabel(pkg.applicationInfo);
                return mLabel;
            }

            public String getPackageName() {
                return pkg.packageName;
            }

            public boolean hasFlag(int flag) {
                return (pkg.applicationInfo.flags & flag) == flag;
            }

            public boolean isDebugApp() {
                return hasFlag(FLAG_DEBUGGABLE);
            }

            public boolean isSystemApp() {
                return hasFlag(FLAG_SYSTEM);
            }

            public boolean isDebuggable() {
                return Configuration.isEnabled(getPackageName());
            }

            public long getInstallTime() {
                return pkg.firstInstallTime;
            }

            public long getUpdateTime() {
                return pkg.lastUpdateTime;
            }
        }

        private final SharedPreferences mSharedPreferences;
        private final PackageManager mPackageManager;
        private List<PackageInfoCache> mInstalledPackages = new ArrayList<>();
        private final List<PackageInfoCache> mFilteredPackages = new ArrayList<>();
        private String mFilterQuery = "";

        InstalledPackageAdapter(PackageManager pkgMgr, SharedPreferences prefs) {
            mPackageManager = pkgMgr;
            mSharedPreferences = prefs;
            setHasStableIds(true);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
            final View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
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
            for (PackageInfo pkg1 : packages) {
                final PackageInfoCache pkg2 = map.containsKey(pkg1.packageName.hashCode()) ?
                        map.get(pkg1.packageName.hashCode()) :
                        new PackageInfoCache(pkg1);
                pkg2.pkg = pkg1;
                mInstalledPackages.add(pkg2);
            }

            filterAndSort();
            notifyDataSetChanged();
        }

        public void setFilterQuery(String query) {
            mFilterQuery = query;
            filterAndSort();
            notifyDataSetChanged();
        }

        private List<PackageInfoCache> filterSystem(List<PackageInfoCache> target) {
            if (mSharedPreferences.getBoolean(PREF_KEY_SHOW_SYSTEM, false))
                return target;

            final List<PackageInfoCache> filtered = new ArrayList<>(target.size());
            for (final PackageInfoCache pkg : target)
                if (!pkg.isSystemApp())
                    filtered.add(pkg);
            return filtered;
        }

        private List<PackageInfoCache> filterDebuggable(List<PackageInfoCache> target) {
            if (mSharedPreferences.getBoolean(PREF_KEY_SHOW_DEBUG, false))
                return target;

            final List<PackageInfoCache> filtered = new ArrayList<>(target.size());
            for (final PackageInfoCache pkg : target)
                if (!pkg.isDebugApp())
                    filtered.add(pkg);
            return filtered;
        }

        private List<PackageInfoCache> filterQuery(List<PackageInfoCache> target) {
            if (TextUtils.isEmpty(mFilterQuery))
                return target;

            final List<PackageInfoCache> filtered = new ArrayList<>(target.size());
            for (final PackageInfoCache pkg : target)
                if (pkg.getLabel(mPackageManager).toString().toLowerCase().contains(mFilterQuery) ||
                        pkg.getPackageName().toLowerCase().contains(mFilterQuery))
                    filtered.add(pkg);
            return filtered;
        }

        private void filterAndSort() {
            List<PackageInfoCache> filtered;
            filtered = filterSystem(mInstalledPackages);
            filtered = filterDebuggable(filtered);
            filtered = filterQuery(filtered);

            Comparator<PackageInfoCache> orderComparator = null;
            switch (mSharedPreferences.getInt(PREF_KEY_SORT_ORDER, SORT_ORDER_LABEL)) {
                case SORT_ORDER_LABEL:
                    orderComparator = Comparator.comparing(pkg -> pkg.getLabel(mPackageManager).toString());
                    break;
                case SORT_ORDER_PACKAGE_NAME:
                    orderComparator = Comparator.comparing(PackageInfoCache::getPackageName);
                    break;
                case SORT_ORDER_INSTALL_TIME:
                    orderComparator = Comparator.comparingLong(PackageInfoCache::getInstallTime).reversed();
                    break;
                case SORT_ORDER_UPDATE_TIME:
                    orderComparator = Comparator.comparingLong(PackageInfoCache::getUpdateTime).reversed();
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
                filtered.sort(debuggableComparator.reversed().thenComparing(orderComparator));
            } else {
                filtered.sort(orderComparator);
            }

            mFilteredPackages.clear();
            mFilteredPackages.addAll(filtered);
        }

        private static class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
            private final AppCompatImageView icon;
            private final AppCompatTextView applicationLabel;
            private final AppCompatTextView packageName;
            private final SwitchCompat toggle;

            private PackageInfoCache pkg = null;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                applicationLabel = itemView.findViewById(R.id.application_label);
                packageName = itemView.findViewById(R.id.package_name);
                toggle = itemView.findViewById(R.id.toggle);

                itemView.setOnClickListener(this);
            }

            public void bind(final PackageInfoCache pkg) {
                this.pkg = pkg;
                final Context context = itemView.getContext();
                final PackageManager pm = context.getPackageManager();

                icon.setImageDrawable(pkg.getIcon(pm));
                applicationLabel.setText(pkg.getLabel(pm));
                applicationLabel.setTypeface(null, getTypeFace(pkg.isSystemApp(), pkg.isDebugApp()));
                packageName.setText(pkg.getPackageName());
                toggle.setOnCheckedChangeListener(null);
                toggle.setChecked(Configuration.isEnabled(pkg.getPackageName()));
                toggle.setOnCheckedChangeListener(this);
            }

            @Override
            public void onCheckedChanged(CompoundButton button, boolean isChecked) {
                if (isChecked)
                    Configuration.add(pkg.getPackageName());
                else
                    Configuration.remove(pkg.getPackageName());
                Snackbar snackbar = Snackbar.make(itemView, R.string.restart_required, LENGTH_SHORT);
                snackbar.setAction(R.string.dismiss, v -> snackbar.dismiss());
                snackbar.show();
            }

            @Override
            public void onClick(View view) {
                toggle.toggle();
            }

            private static int getTypeFace(boolean system, boolean debug) {
                if (system && debug)
                    return Typeface.BOLD_ITALIC;
                if (system)
                    return Typeface.BOLD;
                if (debug)
                    return Typeface.ITALIC;
                return Typeface.NORMAL;
            }
        }
    }
}
