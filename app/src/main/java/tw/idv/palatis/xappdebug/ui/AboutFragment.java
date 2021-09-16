package tw.idv.palatis.xappdebug.ui;

import static tw.idv.palatis.xappdebug.BuildConfig.VERSION_NAME;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import tw.idv.palatis.xappdebug.MainApplication;
import tw.idv.palatis.xappdebug.R;

public class AboutFragment extends Fragment {
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_about, container, false);
        final Button button = root.findViewById(R.id.github);
        button.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_project_page)))));

        final TextView version = root.findViewById(R.id.version);
        version.setText(getString(R.string.app_version, VERSION_NAME));

        int xposed = MainApplication.getActiveXposedVersion();
        if (xposed != -1) {
            final TextView text = root.findViewById(R.id.xposed);
            text.setText(getString(R.string.xposed_version, xposed));
        }
        return root;
    }
}
