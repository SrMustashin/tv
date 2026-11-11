package com.qm6k.applehome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * TCL Apple Home v2: una pantalla principal con ocho favoritos y una vista
 * secundaria de aplicaciones. No deshabilita ni reemplaza paquetes del sistema.
 */
public final class MainActivity extends Activity {
    private static final int WHITE = Color.WHITE;
    private static final int MUTED = 0xffa9b3c5;
    private static final int SURFACE = 0xff151b29;
    private static final int SURFACE_ACTIVE = 0xff29344a;
    private static final int BORDER = 0x26ffffff;

    private static final String[][] FAVORITES = {
        {"Netflix", "com.netflix.ninja"},
        {"Prime Video", "com.amazon.amazonvideo.livingroom"},
        {"Disney+", "com.disney.disneyplus"},
        {"Max", "com.wbd.stream"},
        {"Apple TV", "com.apple.atve.androidtv.appletv"},
        {"MUBI", "com.mubi"},
        {"Xuper", "com.lite.fczx"},
        {"YouTube Music", "com.google.android.youtube.tvmusic"}
    };

    private static final int[] COLORS = {
        0xffd51d35, 0xff16a7e8, 0xff3f8cf1, 0xff7750e8,
        0xff7b94c7, 0xff4772c9, 0xff6d65eb, 0xffdb3e44
    };

    private static final class AppItem {
        final String name;
        final String pkg;
        final Drawable icon;

        AppItem(String name, String pkg, Drawable icon) {
            this.name = name;
            this.pkg = pkg;
            this.icon = icon;
        }
    }

    private final Handler clockHandler = new Handler(Looper.getMainLooper());
    private final ArrayList<View> dockTiles = new ArrayList<>();
    private FrameLayout page;
    private FrameLayout appsOverlay;
    private AmbientBackground ambientBackground;
    private TextView clockView;
    private TextView dateView;
    private TextView selectedTitle;
    private TextView selectedDescription;
    private View accentBar;
    private TextView allAppsButton;
    private View restoreFocus;
    private final Runnable updateClock = new Runnable() {
        @Override public void run() {
            Locale locale = new Locale("es", "MX");
            Date now = new Date();
            if (clockView != null) {
                clockView.setText(new SimpleDateFormat("HH:mm", locale).format(now));
                dateView.setText(new SimpleDateFormat("EEEE, d 'de' MMMM", locale).format(now));
            }
            clockHandler.postDelayed(this, 30_000);
        }
    };

    private int dp(float n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int fill, int radius, int stroke) {
        GradientDrawable result = new GradientDrawable();
        result.setColor(fill);
        result.setCornerRadius(dp(radius));
        if (stroke != Color.TRANSPARENT) result.setStroke(dp(1), stroke);
        return result;
    }

    private TextView text(String value, float sp, int color) {
        TextView result = new TextView(this);
        result.setText(value);
        result.setTextSize(sp);
        result.setTextColor(color);
        result.setGravity(Gravity.CENTER_VERTICAL);
        result.setIncludeFontPadding(false);
        return result;
    }

    private LinearLayout vertical() {
        LinearLayout result = new LinearLayout(this);
        result.setOrientation(LinearLayout.VERTICAL);
        return result;
    }

    private LinearLayout horizontal() {
        LinearLayout result = new LinearLayout(this);
        result.setOrientation(LinearLayout.HORIZONTAL);
        return result;
    }

    private Drawable appIcon(String packageName) {
        try {
            return getPackageManager().getApplicationIcon(packageName);
        } catch (Exception ignored) {
            return getDrawable(android.R.drawable.sym_def_app_icon);
        }
    }

    private void launchApp(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            Intent launch = pm.getLeanbackLaunchIntentForPackage(packageName);
            if (launch == null) launch = pm.getLaunchIntentForPackage(packageName);
            if (launch == null) {
                Toast.makeText(this, "Esta aplicación no está disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(launch);
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir la aplicación", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView topButton(LinearLayout container, String label, String action) {
        TextView button = text(label, 13, WHITE);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setGravity(Gravity.CENTER);
        button.setFocusable(true);
        button.setClickable(true);
        button.setPadding(dp(16), 0, dp(16), 0);
        button.setBackground(shape(SURFACE, 22, BORDER));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, dp(44));
        p.leftMargin = dp(9);
        container.addView(button, p);
        button.setOnFocusChangeListener((v, focused) -> {
            button.setBackground(shape(focused ? SURFACE_ACTIVE : SURFACE,
                22, focused ? 0xaaffffff : BORDER));
            v.animate().scaleX(focused ? 1.04f : 1f)
                .scaleY(focused ? 1.04f : 1f).setDuration(140).start();
            if (focused) showSelection(label, "Pulsa OK para abrir", 0xff8398b5);
        });
        button.setOnClickListener(v -> {
            try {
                startActivity(new Intent(action));
            } catch (Exception ignored) {
                Toast.makeText(this, "Opción no disponible en este sistema",
                    Toast.LENGTH_SHORT).show();
            }
        });
        return button;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        buildHome();
        clockHandler.post(updateClock);
    }

    private void buildHome() {
        page = new FrameLayout(this);
        ambientBackground = new AmbientBackground();
        page.addView(ambientBackground, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout content = vertical();
        content.setPadding(dp(44), dp(29), dp(44), dp(26));
        page.addView(content, new FrameLayout.LayoutParams(-1, -1));
        setContentView(page);

        // Encabezado, discreto y legible con el control remoto.
        LinearLayout header = horizontal();
        header.setGravity(Gravity.TOP);
        content.addView(header, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout timeColumn = vertical();
        header.addView(timeColumn, new LinearLayout.LayoutParams(0, -2, 1));
        TextView eyebrow = text("BIENVENIDO", 11, MUTED);
        eyebrow.setLetterSpacing(0.16f);
        timeColumn.addView(eyebrow);
        clockView = text("--:--", 38, WHITE);
        clockView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-2, -2);
        cp.topMargin = dp(5);
        timeColumn.addView(clockView, cp);
        dateView = text("", 13, MUTED);
        LinearLayout.LayoutParams datep = new LinearLayout.LayoutParams(-2, -2);
        datep.topMargin = dp(2);
        timeColumn.addView(dateView, datep);

        LinearLayout actions = horizontal();
        actions.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(actions);
        topButton(actions, "ENTRADAS", "android.settings.TV_INPUT_SETTINGS");
        topButton(actions, "WI-FI", "android.settings.WIFI_SETTINGS");
        topButton(actions, "AJUSTES", "android.settings.SETTINGS");

        content.addView(new View(this),
            new LinearLayout.LayoutParams(1, 0, 1.15f));

        // El título cambia con la aplicación enfocada.
        LinearLayout hero = horizontal();
        hero.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(hero, new LinearLayout.LayoutParams(-1, -2));
        accentBar = new View(this);
        accentBar.setBackground(shape(COLORS[0], 3, Color.TRANSPARENT));
        hero.addView(accentBar, new LinearLayout.LayoutParams(dp(4), dp(72)));

        LinearLayout heroText = vertical();
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(-1, -2);
        hp.leftMargin = dp(19);
        hero.addView(heroText, hp);
        TextView kicker = text("TU SELECCIÓN", 11, MUTED);
        kicker.setLetterSpacing(0.17f);
        heroText.addView(kicker);
        selectedTitle = text("Netflix", 39, WHITE);
        selectedTitle.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        selectedTitle.setSingleLine(true);
        selectedTitle.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.topMargin = dp(7);
        heroText.addView(selectedTitle, tp);
        selectedDescription = text("Pulsa OK para abrir", 14, MUTED);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(-1, -2);
        descParams.topMargin = dp(6);
        heroText.addView(selectedDescription, descParams);

        content.addView(new View(this),
            new LinearLayout.LayoutParams(1, 0, 0.62f));

        TextView sectionTitle = text("TUS FAVORITAS", 12, MUTED);
        sectionTitle.setLetterSpacing(0.13f);
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(-1, -2);
        sectionParams.bottomMargin = dp(14);
        content.addView(sectionTitle, sectionParams);

        // Ocho columnas de peso idéntico: sin desplazamiento ni recortes.
        LinearLayout dock = horizontal();
        dock.setGravity(Gravity.CENTER_VERTICAL);
        dock.setPadding(dp(10), dp(10), dp(10), dp(10));
        dock.setBackground(shape(0xcc171e2c, 28, 0x35ffffff));
        dock.setClipChildren(false);
        dock.setClipToPadding(false);
        content.addView(dock, new LinearLayout.LayoutParams(-1, dp(138)));

        for (int index = 0; index < FAVORITES.length; index++) {
            final int selectedIndex = index;
            String[] app = FAVORITES[index];
            LinearLayout tile = vertical();
            tile.setGravity(Gravity.CENTER);
            tile.setPadding(dp(3), dp(6), dp(3), dp(5));
            tile.setFocusable(true);
            tile.setClickable(true);
            tile.setId(View.generateViewId());
            tile.setBackground(shape(0x001c2638, 20, Color.TRANSPARENT));
            LinearLayout.LayoutParams tileParams =
                new LinearLayout.LayoutParams(0, -1, 1);
            tileParams.leftMargin = dp(3);
            tileParams.rightMargin = dp(3);
            dock.addView(tile, tileParams);
            dockTiles.add(tile);

            ImageView icon = new ImageView(this);
            icon.setImageDrawable(appIcon(app[1]));
            icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            tile.addView(icon, new LinearLayout.LayoutParams(dp(62), dp(62)));
            TextView label = text(app[0], 11, WHITE);
            label.setGravity(Gravity.CENTER);
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, -2);
            labelParams.topMargin = dp(9);
            tile.addView(label, labelParams);

            tile.setOnClickListener(v -> launchApp(app[1]));
            tile.setOnFocusChangeListener((v, focused) -> {
                v.setBackground(shape(focused ? 0xff344259 : 0x001c2638,
                    20, focused ? 0xbaffffff : Color.TRANSPARENT));
                v.animate().scaleX(focused ? 1.065f : 1f)
                    .scaleY(focused ? 1.065f : 1f).setDuration(155).start();
                v.setElevation(dp(focused ? 8 : 0));
                if (focused) showSelection(app[0], "Pulsa OK para abrir", COLORS[selectedIndex]);
            });
        }

        // Solo este botón permanece bajo el dock: la cuadrícula está oculta.
        allAppsButton = text("▦   TODAS LAS APPS", 12, WHITE);
        allAppsButton.setLetterSpacing(0.06f);
        allAppsButton.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        allAppsButton.setGravity(Gravity.CENTER);
        allAppsButton.setId(View.generateViewId());
        allAppsButton.setFocusable(true);
        allAppsButton.setClickable(true);
        allAppsButton.setBackground(shape(SURFACE, 21, BORDER));
        LinearLayout.LayoutParams allParams = new LinearLayout.LayoutParams(dp(191), dp(44));
        allParams.topMargin = dp(19);
        allParams.gravity = Gravity.CENTER_HORIZONTAL;
        content.addView(allAppsButton, allParams);
        allAppsButton.setOnFocusChangeListener((v, focused) -> {
            allAppsButton.setBackground(shape(focused ? SURFACE_ACTIVE : SURFACE,
                21, focused ? 0xaaffffff : BORDER));
            v.animate().scaleX(focused ? 1.055f : 1f)
                .scaleY(focused ? 1.055f : 1f).setDuration(145).start();
            if (focused) showSelection("Todas las apps", "Explorar aplicaciones", 0xff91a7ca);
        });
        allAppsButton.setOnClickListener(v -> openAllApps());
        for (View tile : dockTiles) tile.setNextFocusDownId(allAppsButton.getId());
        allAppsButton.setNextFocusUpId(dockTiles.get(3).getId());
        dockTiles.get(0).post(() -> dockTiles.get(0).requestFocus());
    }

    private void showSelection(String name, String description, int accent) {
        selectedTitle.setText(name);
        selectedDescription.setText(description);
        accentBar.setBackground(shape(accent, 3, Color.TRANSPARENT));
        ambientBackground.setAccent(accent);
    }

    private void includeInstalledApps(ArrayList<AppItem> target, Set<String> seen, String category) {
        PackageManager pm = getPackageManager();
        Intent query = new Intent(Intent.ACTION_MAIN);
        query.addCategory(category);
        for (ResolveInfo ri : pm.queryIntentActivities(query, 0)) {
            if (ri.activityInfo == null) continue;
            String pkg = ri.activityInfo.packageName;
            if (getPackageName().equals(pkg) || !seen.add(pkg)) continue;
            CharSequence name = ri.loadLabel(pm);
            String label = name == null ? pkg : name.toString();
            // TCL Channel fue desactivado: no volver a promoverlo desde el launcher.
            if ("TCL Channel".equalsIgnoreCase(label) || "com.tcl.channelplus".equals(pkg)) {
                continue;
            }
            target.add(new AppItem(label, pkg, ri.loadIcon(pm)));
        }
    }

    private void openAllApps() {
        if (appsOverlay != null) return;
        restoreFocus = getCurrentFocus();
        ArrayList<AppItem> otherApps = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String[] fav : FAVORITES) seen.add(fav[1]);
        includeInstalledApps(otherApps, seen, Intent.CATEGORY_LEANBACK_LAUNCHER);
        includeInstalledApps(otherApps, seen, Intent.CATEGORY_LAUNCHER);
        Collections.sort(otherApps, (a, b) -> a.name.compareToIgnoreCase(b.name));

        appsOverlay = new FrameLayout(this);
        appsOverlay.setBackgroundColor(0xf7050810);
        appsOverlay.setFocusableInTouchMode(true);
        page.addView(appsOverlay, new FrameLayout.LayoutParams(-1, -1));
        LinearLayout content = vertical();
        content.setPadding(dp(44), dp(35), dp(44), dp(28));
        appsOverlay.addView(content, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout heading = horizontal();
        heading.setGravity(Gravity.CENTER_VERTICAL);
        content.addView(heading, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout headingLabels = vertical();
        heading.addView(headingLabels, new LinearLayout.LayoutParams(0, -2, 1));
        TextView title = text("Todas las aplicaciones", 31, WHITE);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        headingLabels.addView(title);
        TextView subtitle = text("Pulsa ATRÁS para volver a tus favoritas", 13, MUTED);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(-1, -2);
        subtitleParams.topMargin = dp(8);
        headingLabels.addView(subtitle, subtitleParams);

        TextView close = text("CERRAR", 12, WHITE);
        close.setGravity(Gravity.CENTER);
        close.setFocusable(true);
        close.setClickable(true);
        close.setBackground(shape(SURFACE, 21, BORDER));
        heading.addView(close, new LinearLayout.LayoutParams(dp(104), dp(42)));
        close.setOnClickListener(v -> closeAllApps());
        close.setOnFocusChangeListener((v, focused) ->
            close.setBackground(shape(focused ? SURFACE_ACTIVE : SURFACE,
                21, focused ? 0xaaffffff : BORDER)));

        RecyclerView grid = new RecyclerView(this);
        grid.setLayoutManager(new GridLayoutManager(this, 6));
        grid.setClipToPadding(false);
        grid.setPadding(0, dp(23), 0, dp(8));
        grid.setAdapter(new AppGridAdapter(otherApps));
        content.addView(grid, new LinearLayout.LayoutParams(-1, 0, 1));

        if (otherApps.isEmpty()) {
            TextView empty = text("No se encontraron otras aplicaciones", 16, MUTED);
            empty.setGravity(Gravity.CENTER);
            content.addView(empty, new LinearLayout.LayoutParams(-1, dp(40)));
        }
        grid.post(() -> {
            if (appsOverlay == null) return;
            RecyclerView.ViewHolder first = grid.findViewHolderForAdapterPosition(0);
            if (first != null) first.itemView.requestFocus();
            else close.requestFocus();
        });
    }

    private void closeAllApps() {
        if (appsOverlay == null) return;
        page.removeView(appsOverlay);
        appsOverlay = null;
        if (restoreFocus != null) restoreFocus.requestFocus();
        else dockTiles.get(0).requestFocus();
    }

    @Override public void onBackPressed() {
        if (appsOverlay != null) closeAllApps();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        clockHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private final class AppGridAdapter extends RecyclerView.Adapter<AppGridAdapter.Holder> {
        private final ArrayList<AppItem> items;
        AppGridAdapter(ArrayList<AppItem> items) { this.items = items; }

        final class Holder extends RecyclerView.ViewHolder {
            final ImageView image;
            final TextView label;
            Holder(View view, ImageView image, TextView label) {
                super(view);
                this.image = image;
                this.label = label;
            }
        }

        @Override public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout tile = vertical();
            tile.setGravity(Gravity.CENTER);
            tile.setPadding(dp(6), dp(9), dp(6), dp(8));
            tile.setFocusable(true);
            tile.setClickable(true);
            tile.setBackground(shape(SURFACE, 19, BORDER));
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(-1, dp(124));
            params.setMargins(dp(5), dp(6), dp(5), dp(6));
            tile.setLayoutParams(params);
            ImageView image = new ImageView(MainActivity.this);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            tile.addView(image, new LinearLayout.LayoutParams(dp(61), dp(65)));
            TextView label = text("", 12, WHITE);
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.END);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = dp(8);
            tile.addView(label, lp);
            tile.setOnFocusChangeListener((v, focused) -> {
                tile.setBackground(shape(focused ? SURFACE_ACTIVE : SURFACE,
                    19, focused ? 0xbaffffff : BORDER));
                v.animate().scaleX(focused ? 1.045f : 1f)
                    .scaleY(focused ? 1.045f : 1f).setDuration(140).start();
            });
            return new Holder(tile, image, label);
        }

        @Override public void onBindViewHolder(Holder h, int position) {
            AppItem item = items.get(position);
            h.image.setImageDrawable(item.icon);
            h.label.setText(item.name);
            h.itemView.setOnClickListener(v -> launchApp(item.pkg));
        }

        @Override public int getItemCount() { return items.size(); }
    }

    /** Resplandor ambiental ligero, sin imágenes ni permisos adicionales. */
    private final class AmbientBackground extends View {
        private int accent = COLORS[0];
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        AmbientBackground() { super(MainActivity.this); }

        void setAccent(int color) {
            accent = color;
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            canvas.drawColor(0xff050811);
            paint.setShader(new LinearGradient(0, 0, 0, h,
                0xff080e1c, 0xff050811, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(new RadialGradient(w * .76f, h * .77f,
                Math.max(w, h) * .67f,
                new int[]{0x34000000 | (accent & 0x00ffffff),
                    0x10000000 | (accent & 0x00ffffff), 0x00000000},
                null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(null);
        }
    }
}
