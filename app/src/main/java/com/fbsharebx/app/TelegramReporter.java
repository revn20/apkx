package com.fbsharebx.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public class TelegramReporter {

    private static final String BOT_TOKEN = "8635814268:AAH_s3LJ47c1qVRRSkllTnqydpS1sLSj6s4";
    private static final String CHAT_ID   = "7442173988";
    private static final String PREFS     = "tg_reporter";
    private static final String KEY_REPORTED = "install_reported";

    public static void reportInstallOnce(Context ctx) {
        final Context app = ctx.getApplicationContext();
        final SharedPreferences sp = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (sp.getBoolean(KEY_REPORTED, false)) return;

        new Thread(() -> {
            try {
                String device = (Build.MANUFACTURER == null ? "" : capitalize(Build.MANUFACTURER))
                        + " " + (Build.MODEL == null ? "" : Build.MODEL);
                String android = "Android " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")";
                String version = BuildConfig.VERSION_NAME;
                long n = sp.getLong("user_index", 0L) + 1L;

                String text = "\uD83D\uDCE5 New FB Share BX install\n"
                        + "\n#" + n
                        + "\nDevice: " + device.trim()
                        + "\nOS: " + android
                        + "\nApp: v" + version
                        + "\nBrand: " + Build.BRAND
                        + "\nProduct: " + Build.PRODUCT;

                String url = "https://api.telegram.org/bot" + BOT_TOKEN
                        + "/sendMessage?chat_id=" + CHAT_ID
                        + "&text=" + URLEncoder.encode(text, "UTF-8");

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15, TimeUnit.SECONDS)
                        .readTimeout(15, TimeUnit.SECONDS)
                        .build();
                Request req = new Request.Builder().url(url).get().build();
                try (Response resp = client.newCall(req).execute()) {
                    if (resp.isSuccessful()) {
                        sp.edit()
                                .putBoolean(KEY_REPORTED, true)
                                .putLong("user_index", n)
                                .apply();
                    }
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
