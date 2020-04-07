package tw.idv.palatis.xappdebug;

import android.os.Process;
import android.system.ErrnoException;
import android.system.Os;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static tw.idv.palatis.xappdebug.Constants.CONFIG_PATH_FORMAT;

public final class Configuration {
    private Configuration() {
    }

    public static void add(String packageName) {
        try {
            final String path = String.format(
                    Locale.getDefault(),
                    CONFIG_PATH_FORMAT,
                    Process.myUserHandle().hashCode(), packageName
            );
            final File file = new File(path);
            final File parent1 = new File(file.getParent());
            final File parent2 = new File(parent1.getParent());
            parent1.mkdirs();
            Os.chmod(parent1.getPath(), 00755);
            Os.chmod(parent2.getPath(), 00755);
            file.createNewFile();
        } catch (ErrnoException | IOException ignored) {
        }
    }

    public static void remove(String packageName) {
        final String path = String.format(
                Locale.getDefault(),
                CONFIG_PATH_FORMAT,
                Process.myUserHandle().hashCode(), packageName
        );
        new File(path).delete();
    }

    public static boolean isEnabled(String packageName) {
        final String path = String.format(
                Locale.getDefault(),
                CONFIG_PATH_FORMAT,
                Process.myUserHandle().hashCode(), packageName
        );
        return new File(path).exists();
    }
}
