package com.fbsharebx.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class InfoActivity extends AppCompatActivity {

    public static final String EXTRA_PAGE = "page";
    public static final int PAGE_FEATURES = 1;
    public static final int PAGE_DEVELOPER = 2;
    public static final int PAGE_ABOUT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applySavedTheme(this);
        setContentView(R.layout.activity_info);

        MaterialToolbar bar = findViewById(R.id.infoToolbar);
        LinearLayout container = findViewById(R.id.infoContainer);
        bar.setNavigationOnClickListener(v -> finish());

        int page = getIntent().getIntExtra(EXTRA_PAGE, PAGE_ABOUT);
        switch (page) {
            case PAGE_FEATURES:
                bar.setTitle(getString(R.string.title_features));
                buildFeatures(container);
                break;
            case PAGE_DEVELOPER:
                bar.setTitle(getString(R.string.title_developer));
                buildDeveloper(container);
                break;
            default:
                bar.setTitle(getString(R.string.title_about));
                buildAbout(container);
        }
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private int onSurface() {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, tv, true);
        return tv.data;
    }

    private TextView heading(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(20);
        t.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        t.setTextColor(0xFFFFFFFF);
        t.setLetterSpacing(0.04f);
        return t;
    }

    private TextView subtitle(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(12);
        t.setTextColor(0xCCFFFFFF);
        return t;
    }

    private LinearLayout heroCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_header);
        card.setPadding(dp(18), dp(20), dp(18), dp(20));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(lp);
        return card;
    }

    private View row(int iconRes, String title, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(lp);
        row.setElevation(dp(2));

        // Icon tile
        LinearLayout tile = new LinearLayout(this);
        tile.setBackgroundResource(R.drawable.bg_metric);
        tile.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(dp(44), dp(44));
        tlp.setMarginEnd(dp(12));
        tile.setLayoutParams(tlp);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setImageTintList(ColorStateList.valueOf(getColor(R.color.brand_primary)));
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(dp(22), dp(22));
        icon.setLayoutParams(ilp);
        tile.addView(icon);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(14);
        t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        t.setTextColor(onSurface());

        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextSize(12);
        d.setTextColor(0xFF8A92A6);
        d.setLineSpacing(dp(2), 1f);

        col.addView(t);
        col.addView(d);
        row.addView(tile);
        row.addView(col);
        return row;
    }

    private void buildFeatures(LinearLayout c) {
        LinearLayout hero = heroCard();
        hero.addView(heading("Everything inside"));
        hero.addView(subtitle("A clean, focused toolkit for boosting your post shares."));
        c.addView(hero);

        c.addView(row(R.drawable.ic_rocket,    "Auto Sharing",
                "Send hundreds of shares with a single tap, powered by the Vern API."));
        c.addView(row(R.drawable.ic_tune,      "Custom Limit",
                "Choose any share count you want, from a few to several thousand."));
        c.addView(row(R.drawable.ic_moon,      "Light and Dark Mode",
                "Flip the switch in the menu and the whole app changes instantly."));
        c.addView(row(R.drawable.ic_refresh,   "Automatic Updates",
                "The app checks GitHub on launch and prompts you when a new build is ready."));
        c.addView(row(R.drawable.ic_clipboard, "Readable Results",
                "Responses are parsed into a friendly card with status, count, and message."));
        c.addView(row(R.drawable.ic_save,      "Saved Preferences",
                "Your theme choice is remembered between launches."));
        c.addView(row(R.drawable.ic_shield,    "Private by Default",
                "Nothing leaves your device except the request you send to the API."));
    }

    private void buildDeveloper(LinearLayout c) {
        LinearLayout hero = heroCard();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.mipmap.ic_launcher_round);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(dp(56), dp(56));
        alp.setMargins(0, 0, 0, dp(10));
        avatar.setLayoutParams(alp);
        hero.addView(avatar);

        TextView name = new TextView(this);
        name.setText("notfound500");
        name.setTextSize(18);
        name.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        name.setTextColor(0xFFFFFFFF);
        name.setGravity(Gravity.CENTER);
        hero.addView(name);

        TextView role = new TextView(this);
        role.setText("Developer  -  Designer  -  Maintainer");
        role.setTextSize(11);
        role.setTextColor(0xCCFFFFFF);
        role.setGravity(Gravity.CENTER);
        hero.addView(role);
        c.addView(hero);

        c.addView(row(R.drawable.ic_person,  "Name",       "notfound500"));
        c.addView(row(R.drawable.ic_link,    "Facebook",   "facebook.com/notfound500"));
        c.addView(row(R.drawable.ic_code,    "Repository", "github.com/vinababi15/apkk"));
        c.addView(row(R.drawable.ic_sparkle, "Built With", "Java, Material 3, OkHttp"));

        MaterialButton fb = new MaterialButton(this);
        fb.setText("Open Facebook Profile");
        fb.setCornerRadius(dp(14));
        fb.setAllCaps(false);
        fb.setTextSize(13);
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        flp.setMargins(0, dp(6), 0, 0);
        fb.setLayoutParams(flp);
        fb.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.facebook.com/notfound500"))));
        c.addView(fb);
    }

    private void buildAbout(LinearLayout c) {
        LinearLayout hero = heroCard();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView ic = new ImageView(this);
        ic.setImageResource(R.mipmap.ic_launcher);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(dp(48), dp(48));
        alp.setMargins(0, 0, 0, dp(10));
        ic.setLayoutParams(alp);
        hero.addView(ic);

        TextView name = new TextView(this);
        name.setText(getString(R.string.app_name));
        name.setTextSize(18);
        name.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        name.setTextColor(0xFFFFFFFF);
        name.setGravity(Gravity.CENTER);
        hero.addView(name);

        TextView ver = new TextView(this);
        ver.setText("Version " + BuildConfig.VERSION_NAME);
        ver.setTextSize(11);
        ver.setTextColor(0xCCFFFFFF);
        ver.setGravity(Gravity.CENTER);
        hero.addView(ver);
        c.addView(hero);

        c.addView(row(R.drawable.ic_book,    "About",
                "FB SHARE BX is a clean, modern Android client for the vern-rest-api auto share endpoint."));
        c.addView(row(R.drawable.ic_balance, "License",
                "Provided as is for educational purposes. Use responsibly."));
        c.addView(row(R.drawable.ic_chat,    "Support",
                "Reach the developer through the Facebook profile listed on the Developer page."));
        c.addView(row(R.drawable.ic_refresh, "Auto Update",
                "The app polls GitHub releases for new versions and prompts you to install."));
    }
}
