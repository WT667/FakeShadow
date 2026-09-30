package com.fakeshadow;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * FakeShadow core hook — intercepts location calls inside the target app process
 * and returns a spoofed GPS fix.
 *
 * <p>Configuration is read from {@code XSharedPreferences("com.fakeshadow", "fakeshadow_prefs")}:
 * <ul>
 *   <li>{@code enabled} (boolean) — master switch</li>
 *   <li>{@code latitude} (String)  — e.g. "34.052235"</li>
 *   <li>{@code longitude} (String) — e.g. "-118.243683"</li>
 *   <li>{@code accuracy} (float)  — default 5.0f</li>
 *   <li>{@code target_package} (String) — empty = hook all user apps</li>
 * </ul>
 */
public class XposedHook implements IXposedHookLoadPackage {

    private static final String TAG = "FakeShadow";
    private static final String PKG = "com.fakeshadow";
    private static final String PREFS = "fakeshadow_prefs";

    private XSharedPreferences prefs;
    private double fakeLat;
    private double fakeLng;
    private float fakeAccuracy = 5.0f;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Never hook the system_server process or our own UI app
        if (lpparam.packageName.equals("android")
                || lpparam.packageName.equals(PKG)
                || lpparam.packageName.equals("com.android.server")) {
            return;
        }

        prefs = new XSharedPreferences(PKG, PREFS);
        prefs.makeWorldReadable();

        if (!loadConfig(lpparam.packageName)) {
            return;
        }

        XposedBridge.log(TAG + ": active in " + lpparam.packageName
                + " -> lat=" + fakeLat + " lng=" + fakeLng);

        hookLocationManager(lpparam);
        hookLocationGetters();
    }

    // ------------------------------------------------------------------
    // Config
    // ------------------------------------------------------------------

    /** @return true if the hook should be installed in this process. */
    private boolean loadConfig(String packageName) {
        prefs.reload();

        if (!prefs.getBoolean("enabled", false)) {
            return false;
        }

        // Per-app scope: empty target means "all apps except system"
        String target = prefs.getString("target_package", "").trim();
        if (!target.isEmpty() && !target.equals(packageName)) {
            return false;
        }

        try {
            fakeLat = Double.parseDouble(prefs.getString("latitude", "39.9892"));
            fakeLng = Double.parseDouble(prefs.getString("longitude", "116.3975"));
        } catch (NumberFormatException e) {
            XposedBridge.log(TAG + ": invalid lat/lng in prefs, using defaults");
            fakeLat = 39.9892;
            fakeLng = 116.3975;
        }
        fakeAccuracy = prefs.getFloat("accuracy", 5.0f);
        return true;
    }

    // ------------------------------------------------------------------
    // Hook points
    // ------------------------------------------------------------------

    private void hookLocationManager(XC_LoadPackage.LoadPackageParam lpparam) {
        // 1) getLastKnownLocation(String provider)
        XposedHelpers.findAndHookMethod(LocationManager.class, "getLastKnownLocation",
                String.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        String provider = (String) param.args[0];
                        param.setResult(buildFakeLocation(provider));
                    }
                });

        // 2) requestLocationUpdates(String, long, float, LocationListener)
        //    — let the call through; our Location getter hooks will transform
        //      every Location delivered to the listener.
        XposedHelpers.findAndHookMethod(LocationManager.class, "requestLocationUpdates",
                String.class, long.class, float.class,
                android.location.LocationListener.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        XposedBridge.log(TAG + ": requestLocationUpdates ok in "
                                + lpparam.packageName);
                    }
                });

        // 3) requestLocationUpdates(String, long, float, PendingIntent)
        XposedHelpers.findAndHookMethod(LocationManager.class, "requestLocationUpdates",
                String.class, long.class, float.class,
                android.app.PendingIntent.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        // no-op; delivery goes through Location objects
                    }
                });

        // 4) getCurrentLocation (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Class<?> locationConsumerCls =
                        XposedHelpers.findClass("android.location.LocationConsumer",
                                lpparam.classLoader);
                XposedHelpers.findAndHookMethod(LocationManager.class, "getCurrentLocation",
                        String.class, java.util.concurrent.Executor.class,
                        locationConsumerCls, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) {
                                // LocationConsumer.accept(Location) will be hooked via
                                // the Location getter hooks below.
                            }
                        });
            } catch (Throwable t) {
                XposedBridge.log(TAG + ": getCurrentLocation hook skipped: " + t.getMessage());
            }
        }

        // 5) getAllProviders / getProviders — return all providers so the app
        //    doesn't detect that GPS is missing
        XposedHelpers.findAndHookMethod(LocationManager.class, "getProviders",
                boolean.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        @SuppressWarnings("unchecked")
                        List<String> providers = (List<String>) param.getResult();
                        if (providers != null && !providers.contains(LocationManager.GPS_PROVIDER)) {
                            providers.add(LocationManager.GPS_PROVIDER);
                        }
                        if (providers != null && !providers.contains(LocationManager.NETWORK_PROVIDER)) {
                            providers.add(LocationManager.NETWORK_PROVIDER);
                        }
                    }
                });
    }

    /**
     * Hook Location getters.  Because we already filtered by package name in
     * {@link #handleLoadPackage}, these only fire inside the target app process —
     * every Location object (real or fake) returns our spoofed coordinates.
     */
    private void hookLocationGetters() {
        XposedHelpers.findAndHookMethod(Location.class, "getLatitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                param.setResult(fakeLat);
            }
        });

        XposedHelpers.findAndHookMethod(Location.class, "getLongitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                param.setResult(fakeLng);
            }
        });

        XposedHelpers.findAndHookMethod(Location.class, "getAccuracy", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                param.setResult(fakeAccuracy);
            }
        });

        XposedHelpers.findAndHookMethod(Location.class, "getTime", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                param.setResult(System.currentTimeMillis());
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            XposedHelpers.findAndHookMethod(Location.class, "getElapsedRealtimeNanos",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            param.setResult(SystemClock.elapsedRealtimeNanos());
                        }
                    });
        }

        // API 33+ : getBearing / getSpeed already return 0 by default; leave as-is
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Location buildFakeLocation(String provider) {
        Location loc = new Location(
                provider != null ? provider : LocationManager.GPS_PROVIDER);
        loc.setLatitude(fakeLat);
        loc.setLongitude(fakeLng);
        loc.setAccuracy(fakeAccuracy);
        loc.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        loc.setBearing(0.0f);
        loc.setSpeed(0.0f);
        return loc;
    }
}
