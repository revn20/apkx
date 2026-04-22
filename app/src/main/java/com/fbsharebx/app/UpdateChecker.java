package com.fbsharebx.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class UpdateChecker {
    private static final String RELEASES_LATEST =
        "https://api.github.com/repos/vinababi15/apkk/releases/latest";
    private static final String RELEASES_ALL =
        "https://api.github.com/repos/vinababi15/apkk/releases";

    public interface StatsListener {
        void onStats(String version, long totalDownloads);
    }

    public static void check(Context ctx, boolean userTriggered) {
        new Thread(() -> {
            try {
                String json = ApiClient.fetchUrl(RELEASES_LATEST);
                JSONObject obj = new JSONObject(json);
                String body = obj.optString("body", "");
                String name = obj.optString("name", "");
                String html = obj.optString("html_url", "https://github.com/vinababi15/apkk/releases");
                String current = BuildConfig.VERSION_NAME.replace("-debug", "");

                // Parse "version=X.Y.Z" from release body, fallback to release name "v X.Y.Z"
                String tag = extractVersion(body);
                if (tag == null) tag = extractVersion(name);
                if (tag == null) tag = "";

                boolean isNewer = !tag.isEmpty() && !tag.equalsIgnoreCase(current);
                if (isNewer) {
                    new Handler(Looper.getMainLooper()).post(() ->
                        new AlertDialog.Builder(ctx)
                            .setTitle("Update Available")
                            .setMessage("A new version (v" + tag + ") of FB SHARE BX is ready.\n\nYou are currently on v" + current + ".\n\nDownload now to get the latest features and fixes.")
                            .setPositiveButton("Download Now", (d, w) -> {
                                ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(html)));
                            })
                            .setNegativeButton("Later", null)
                            .setCancelable(false)
                            .show());
                } else if (userTriggered) {
                    new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(ctx, "You are up to date (v" + current + ")", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                if (userTriggered) {
                    new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(ctx, "Update check failed", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    private static String extractVersion(String text) {
        if (text == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
                "(?:version\\s*=\\s*|v)([0-9]+\\.[0-9]+\\.[0-9]+)",
                java.util.regex.Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) return m.group(1);
        return null;
    }

    /** Fetches total APK download count across all releases plus the latest version tag. */
    public static void fetchStats(StatsListener listener) {
        new Thread(() -> {
            String version = "v" + BuildConfig.VERSION_NAME;
            long total = 0;
            try {
                String json = ApiClient.fetchUrl(RELEASES_ALL);
                JSONArray arr = new JSONArray(json);
                String latestTag = null;
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject rel = arr.getJSONObject(i);
                    String tag = rel.optString("tag_name", "");
                    if (latestTag == null) {
                        String v = extractVersion(rel.optString("body", ""));
                        if (v == null) v = extractVersion(rel.optString("name", ""));
                        if (v != null) latestTag = v;
                        else if (!tag.equalsIgnoreCase("latest")) latestTag = tag;
                    }
                    JSONArray assets = rel.optJSONArray("assets");
                    if (assets != null) {
                        for (int j = 0; j < assets.length(); j++) {
                            JSONObject a = assets.getJSONObject(j);
                            String name = a.optString("name", "");
                            if (name.toLowerCase().endsWith(".apk")) {
                                total += a.optLong("download_count", 0);
                            }
                        }
                    }
                }
                if (latestTag != null && !latestTag.isEmpty()) {
                    version = latestTag.startsWith("v") ? latestTag : ("v" + latestTag);
                }
            } catch (Exception ignored) {}
            final String fv = version;
            final long ft = total;
            new Handler(Looper.getMainLooper()).post(() -> listener.onStats(fv, ft));
        }).start();
    }
}
