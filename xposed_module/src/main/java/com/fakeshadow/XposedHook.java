package com.fakeshadow;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

public class XposedHook implements IXposedHookLoadPackage {

    private static final String TAG = "FakeShadowXposed";
    private static final double FAKE_LATITUDE = 34.052235; // 示例：洛杉矶纬度
    private static final double FAKE_LONGITUDE = -118.243683; // 示例：洛杉矶经度
    private static final float FAKE_ACCURACY = 3.0f; // 示例：精度

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // 仅对目标应用进行 Hook，这里以所有应用为例，实际应根据包名过滤
        // if (!lpparam.packageName.equals("com.example.targetapp")) {
        //     return;
        // }

        XposedBridge.log(TAG + ": Hooking package: " + lpparam.packageName);

        // Hook LocationManager.getLastKnownLocation
        XposedHelpers.findAndHookMethod(LocationManager.class, "getLastKnownLocation", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log(TAG + ": Hooked getLastKnownLocation for " + lpparam.packageName);
                Location originalLocation = (Location) param.getResult();
                Location fakeLocation = createFakeLocation(originalLocation != null ? originalLocation.getProvider() : LocationManager.GPS_PROVIDER);
                param.setResult(fakeLocation);
                XposedBridge.log(TAG + ": Faked getLastKnownLocation to: " + fakeLocation.getLatitude() + ", " + fakeLocation.getLongitude());
            }
        });

        // Hook LocationManager.requestLocationUpdates (各种重载方法)
        // 示例：Hook requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener)
        XposedHelpers.findAndHookMethod(LocationManager.class, "requestLocationUpdates",
                String.class, long.class, float.class, android.location.LocationListener.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(TAG + ": Hooked requestLocationUpdates for " + lpparam.packageName);
                        // 可以选择不调用原始方法，直接返回，或者修改参数
                        // param.setResult(null); // 阻止原始调用
                    }
                });

        // 更多 LocationManager 的 requestLocationUpdates 重载方法可以类似地 Hook
        // 例如：requestLocationUpdates(String provider, long minTime, float minDistance, PendingIntent callbackIntent)
        XposedHelpers.findAndHookMethod(LocationManager.class, "requestLocationUpdates",
                String.class, long.class, float.class, android.app.PendingIntent.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(TAG + ": Hooked requestLocationUpdates (PendingIntent) for " + lpparam.packageName);
                    }
                });

        // Hook Location.getLatitude 和 Location.getLongitude
        XposedHelpers.findAndHookMethod(Location.class, "getLatitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                // 只有当 Location 对象是我们的目标时才进行修改，避免影响其他正常的 Location 对象
                // 实际应用中需要更复杂的逻辑来判断是否是需要伪造的 Location 对象
                // 这里为了演示，直接修改所有 getLatitude 的返回值
                param.setResult(FAKE_LATITUDE);
            }
        });

        XposedHelpers.findAndHookMethod(Location.class, "getLongitude", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(FAKE_LONGITUDE);
            }
        });

        XposedHelpers.findAndHookMethod(Location.class, "getAccuracy", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                param.setResult(FAKE_ACCURACY);
            }
        });

        // 针对 Android 10 (API 29) 及更高版本，可能需要 Hook Location.getElapsedRealtimeNanos()
        // 以确保伪造位置的时间戳合理
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            XposedHelpers.findAndHookMethod(Location.class, "getElapsedRealtimeNanos", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // 伪造一个合理的时间戳，例如当前时间
                    param.setResult(System.nanoTime());
                }
            });
        }
    }

    private Location createFakeLocation(String provider) {
        Location fakeLocation = new Location(provider);
        fakeLocation.setLatitude(FAKE_LATITUDE);
        fakeLocation.setLongitude(FAKE_LONGITUDE);
        fakeLocation.setAccuracy(FAKE_ACCURACY);
        fakeLocation.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            fakeLocation.setElapsedRealtimeNanos(System.nanoTime());
        }
        return fakeLocation;
    }
}
