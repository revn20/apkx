package com.fbsharebx.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private EditText inputCookie, inputLink, inputLimit;
    private ProgressBar progress;
    private Button btnStart;
    private View resultCard;
    private ImageView resultIcon;
    private TextView resultTitle, resultSubtitle, resultBadge, resultMessage;
    private TextView statDownloads, statVersion;
    private LinearLayout metricsRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applySavedTheme(this);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawerLayout);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        NavigationView nav = findViewById(R.id.navView);
        SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        inputCookie = findViewById(R.id.inputCookie);
        inputLink = findViewById(R.id.inputLink);
        inputLimit = findViewById(R.id.inputLimit);
        progress = findViewById(R.id.progressShare);
        btnStart = findViewById(R.id.btnStart);

        resultCard = findViewById(R.id.resultCard);
        resultIcon = findViewById(R.id.resultIcon);
        resultTitle = findViewById(R.id.resultTitle);
        resultSubtitle = findViewById(R.id.resultSubtitle);
        resultBadge = findViewById(R.id.resultBadge);
        resultMessage = findViewById(R.id.resultMessage);
        metricsRow = findViewById(R.id.metricsRow);
        statDownloads = findViewById(R.id.statDownloads);
        statVersion = findViewById(R.id.statVersion);

        statVersion.setText("v" + BuildConfig.VERSION_NAME);

        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        nav.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // already home
            } else if (id == R.id.nav_features) {
                openInfo(InfoActivity.PAGE_FEATURES);
            } else if (id == R.id.nav_developer) {
                openInfo(InfoActivity.PAGE_DEVELOPER);
            } else if (id == R.id.nav_about) {
                openInfo(InfoActivity.PAGE_ABOUT);
            } else if (id == R.id.nav_update) {
                UpdateChecker.check(this, true);
                refreshStats();
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        btnStart.setOnClickListener(v -> startShare());
        swipe.setOnRefreshListener(() -> {
            swipe.setRefreshing(false);
            refreshStats();
            UpdateChecker.check(this, false);
        });

        refreshStats();
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> UpdateChecker.check(this, false), 1500);
    }

    private void refreshStats() {
        UpdateChecker.fetchStats((version, total) -> {
            statVersion.setText(version);
            statDownloads.setText(formatNumber(total));
        });
    }

    private static String formatNumber(long n) {
        if (n < 1000) return String.valueOf(n);
        if (n < 1_000_000) {
            double k = n / 1000.0;
            return (k >= 10 ? String.valueOf((int) k) : String.format("%.1f", k)) + "K";
        }
        double m = n / 1_000_000.0;
        return (m >= 10 ? String.valueOf((int) m) : String.format("%.1f", m)) + "M";
    }

    private void openInfo(int page) {
        Intent i = new Intent(this, InfoActivity.class);
        i.putExtra(InfoActivity.EXTRA_PAGE, page);
        startActivity(i);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void startShare() {
        final String cookie = inputCookie.getText().toString().trim();
        final String link = inputLink.getText().toString().trim();
        final String limit = inputLimit.getText().toString().trim();

        if (TextUtils.isEmpty(cookie) || TextUtils.isEmpty(link) || TextUtils.isEmpty(limit)) {
            Toast.makeText(this, "Please fill in cookie, link and limit", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnStart.setEnabled(false);
        resultCard.setVisibility(View.GONE);

        new Thread(() -> {
            String body;
            int httpCode = -1;
            String err = null;
            try {
                ApiClient.Result r = ApiClient.shareWithCode(cookie, link, limit);
                body = r.body;
                httpCode = r.code;
            } catch (Exception e) {
                body = null;
                err = e.getMessage();
            }
            final String fbody = body;
            final int fcode = httpCode;
            final String ferr = err;
            new Handler(Looper.getMainLooper()).post(() -> {
                progress.setVisibility(View.GONE);
                btnStart.setEnabled(true);
                renderResult(fcode, fbody, ferr, limit, link);
            });
        }).start();
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    private void renderResult(int httpCode, String body, String err, String limit, String link) {
        resultCard.setVisibility(View.VISIBLE);
        metricsRow.removeAllViews();

        if (err != null) {
            applyError("Connection Failed",
                    "We couldn't reach the server",
                    "Offline",
                    "Please check your internet connection and try again. The server might also be temporarily unavailable.");
            return;
        }

        boolean success = httpCode >= 200 && httpCode < 300;
        Integer countShared = null;
        String humanMsg = null;

        Object parsed = parse(body);
        if (parsed instanceof JSONObject) {
            JSONObject o = (JSONObject) parsed;
            String statusText = firstString(o, "status", "result", "state");
            if (statusText != null) {
                String s = statusText.toLowerCase();
                if (s.contains("fail") || s.contains("error") || s.contains("invalid") || s.contains("expired")) success = false;
                if (s.contains("success") || s.equals("ok") || s.equals("true")) success = true;
            }
            if (o.has("success")) success = o.optBoolean("success", success);
            countShared = firstInt(o, "count", "shared", "total", "amount", "shares", "success_count");
            humanMsg = firstString(o, "message", "msg", "description", "info", "detail", "error");
        } else if (parsed instanceof JSONArray) {
            countShared = ((JSONArray) parsed).length();
        }

        if (success) {
            applySuccess(httpCode, countShared, limit, humanMsg);
        } else {
            String niceErr = humanMsg;
            if (TextUtils.isEmpty(niceErr)) {
                niceErr = "The server rejected your request. This usually means the cookie is expired or the post link is invalid. Please double-check both and try again.";
            }
            applyError("Sharing Failed",
                    "The server didn't accept the request",
                    httpCode > 0 ? ("HTTP " + httpCode) : "Error",
                    niceErr);
        }
    }

    private void applySuccess(int httpCode, Integer count, String limit, String detail) {
        resultIcon.setImageResource(R.drawable.ic_check_circle);
        resultTitle.setText("Sharing Started");
        resultSubtitle.setText("Your job is now running on the server");
        resultBadge.setText("Success");
        resultBadge.setBackgroundResource(R.drawable.bg_pill_success);
        resultBadge.setTextColor(0xFF16A34A);

        addMetric("Shares", count != null ? String.valueOf(count) : (limit != null ? limit : "—"));
        addMetric("Status", "Active");
        addMetric("Mode", "Auto");

        String msg;
        if (!TextUtils.isEmpty(detail) && !looksLikeJson(detail)) {
            msg = capitalize(detail.trim());
        } else if (count != null) {
            msg = "Your post is now being shared " + count + " times. You can close the app, the job will keep running on the server.";
        } else {
            msg = "Your share request has been accepted. The shares will appear on your post in a few moments.";
        }
        resultMessage.setText(msg);
        resultMessage.setTextColor(getOnSurface());
    }

    private void applyError(String title, String subtitle, String badge, String detail) {
        resultIcon.setImageResource(R.drawable.ic_error_circle);
        resultTitle.setText(title);
        resultSubtitle.setText(subtitle);
        resultBadge.setText(badge);
        resultBadge.setBackgroundResource(R.drawable.bg_pill_error);
        resultBadge.setTextColor(0xFFDC2626);
        addMetric("Result", "Failed");
        addMetric("Action", "Retry");

        String msg = (!TextUtils.isEmpty(detail) && !looksLikeJson(detail))
                ? capitalize(detail.trim())
                : "Please verify your cookie, the post link, and the limit value, then try again.";
        resultMessage.setText(msg);
        resultMessage.setTextColor(getOnSurface());
    }

    private static boolean looksLikeJson(String s) {
        if (s == null) return false;
        String t = s.trim();
        return t.startsWith("{") || t.startsWith("[");
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private int getOnSurface() {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, tv, true);
        return tv.data;
    }

    private void addMetric(String label, String value) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setBackgroundResource(R.drawable.bg_metric);
        col.setPadding(dp(12), dp(12), dp(12), dp(12));
        col.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginEnd(dp(8));
        col.setLayoutParams(lp);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(17);
        v.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        v.setTextColor(getOnSurface());
        v.setGravity(Gravity.CENTER);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(11);
        l.setTextColor(0xFF8A92A6);
        l.setGravity(Gravity.CENTER);

        col.addView(v);
        col.addView(l);
        metricsRow.addView(col);
    }

    private static Object parse(String s) {
        if (s == null) return null;
        s = s.trim();
        try {
            if (s.startsWith("{")) return new JSONObject(s);
            if (s.startsWith("[")) return new JSONArray(s);
        } catch (Exception ignored) {}
        return null;
    }

    private static String firstString(JSONObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k)) {
                Object v = o.opt(k);
                if (v != null && !(v instanceof JSONObject) && !(v instanceof JSONArray)) {
                    String s = String.valueOf(v).trim();
                    if (!s.isEmpty() && !s.equalsIgnoreCase("null")) return s;
                }
            }
        }
        Iterator<String> it = o.keys();
        while (it.hasNext()) {
            String k = it.next();
            Object v = o.opt(k);
            if (v instanceof String && ((String) v).length() < 200 && !((String) v).trim().isEmpty()) return (String) v;
        }
        return null;
    }

    private static Integer firstInt(JSONObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k)) {
                int v = o.optInt(k, Integer.MIN_VALUE);
                if (v != Integer.MIN_VALUE) return v;
            }
        }
        return null;
    }
}
