package tw.idv.palatis.xappdebug.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import tw.idv.palatis.xappdebug.R;

import static tw.idv.palatis.xappdebug.BuildConfig.VERSION_NAME;
import static tw.idv.palatis.xappdebug.Constants.LOG_TAG;

public class AboutFragment extends Fragment {

    @Keep
    private static int getActiveXposedVersion() {
        Log.d(LOG_TAG, "Xposed framework is inactive.");
        return -1;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_about, container, false);
        final Button button = root.findViewById(R.id.github);
        button.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_project_page)))));

        final TextView version = root.findViewById(R.id.version);
        version.setText(getString(R.string.app_version, VERSION_NAME));

        int xposed = getActiveXposedVersion();
        if (xposed != -1) {
            final TextView text = root.findViewById(R.id.xposed);
            text.setText(getString(R.string.xposed_version, xposed));
        }
        return root;
    }
}
