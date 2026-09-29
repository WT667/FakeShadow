# FakeShadow
[![GitHub stars](https://img.shields.io/github/stars/WT667/FakeShadow?style=social)](https://github.com/WT667/FakeShadow/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/WT667/FakeShadow?style=social)](https://github.com/WT667/FakeShadow/network/members)
[![GitHub issues](https://img.shields.io/github/issues/WT667/FakeShadow)](https://github.com/WT667/FakeShadow/issues)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue)](https://github.com/WT667/FakeShadow/blob/main/LICENSE)

---

## 项目简介

`FakeShadow` 是一个**纯学习用途**的 Android 虚拟定位模块，基于 **LSPosed / Xposed** 框架，通过 Hook 系统定位服务伪造 GPS 坐标，帮助理解虚拟定位的工作原理。

**重要声明**：本项目仅用于技术研究和个人学习，**严禁**用于任何违规、作弊、绕过平台风控的场景。

## 功能

- 一键开关虚拟定位
- 自定义经纬度（带坐标合法性校验）
- 按包名指定目标应用（留空 = 全局生效）
- Hook `LocationManager.getLastKnownLocation()` + `Location`  getter 链
- 自动补全 providers 列表，避免目标应用检测到 GPS 缺失
- 通过 `XSharedPreferences` 与 Hook 进程通信

## 项目结构

```
app/                          # 唯一模块（UI + Xposed Hook 同包）
├── build.gradle
└── src/main/
    ├── AndroidManifest.xml    # Xposed 模块声明
    ├── assets/xposed_init     # Hook 入口声明
    ├── java/com/fakeshadow/
    │   ├── MainActivity.java  # 控制面板
    │   └── XposedHook.java    # 核心 Hook 逻辑
    └── res/
        ├── layout/activity_main.xml
        └── values/            # strings / themes / colors
```

## 构建与使用

1. Android Studio Hedgehog+ 打开本项目
2. `./gradlew :app:assembleDebug` 编译
3. 安装 APK 到 rooted 设备
4. 在 LSPosed 管理器中启用模块，勾选目标作用域
5. 打开 FakeShadow，输入坐标、选择目标应用，保存并开启开关
6. 重启目标应用使 Hook 生效

## 技术要点

| Hook 点 | 作用 |
|---------|------|
| `LocationManager.getLastKnownLocation` | 返回伪造 Location 对象 |
| `Location.getLatitude/Longitude` | 所有 Location 读取返回假坐标 |
| `Location.getAccuracy/getTime/getElapsedRealtimeNanos` | 伪造精度和时间戳，避免时间不一致检测 |
| `LocationManager.getProviders` | 确保 GPS / NETWORK provider 始终存在 |
| `LocationManager.requestLocationUpdates` | 不阻断调用，由 getter hook 统一转换 |

## 参考资料

- [LSPosed](https://github.com/LSPosed/LSPosed)
- [Xposed API](https://github.com/rovo89/XposedBridge)
- [FakeLocation (Lerist)](https://github.com/Lerist/FakeLocation)
