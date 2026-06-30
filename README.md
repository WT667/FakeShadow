# FakeShadow
[![GitHub stars](https://img.shields.io/github/stars/WT667/FakeShadow?style=social)](https://github.com/WT667/FakeShadow/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/WT667/FakeShadow?style=social)](https://github.com/WT667/FakeShadow/network/members)
[![GitHub issues](https://img.shields.io/github/issues/WT667/FakeShadow)](https://github.com/WT667/FakeShadow/issues)
[![GitHub license](https://img.shields.io/github/license/WT667/FakeShadow)](https://github.com/WT667/FakeShadow/blob/main/LICENSE)

---
## 思路概览
- **Hook 系统定位服务**：通过 LSPosed/Xposed 框架，拦截并修改 Android 系统的 GPS、Fused Location 等定位服务接口。
- **伪造 Wi-Fi / 基站信息**：实现对 Wi-Fi 和基站信息的伪造，以增强虚拟定位的真实性，提高防检测能力。
- **简单 UI 选点 + 控制**：未来计划开发一个简洁的用户界面，允许用户直观地选择伪造位置，并对虚拟定位功能进行开启、关闭和参数调整。
- **模块化设计**：项目分为 `app` (UI 控制) 和 `xposed_module` (核心 Hook 逻辑) 两个模块，便于开发和维护。

---
## 当前进度
- [x] 创建仓库 + 基本 README
- [x] 添加项目结构（app / xposed 模块）
- [x] 核心 Hook 代码示例 (参见 `xposed_module/src/main/java/com/fakeshadow/XposedHook.java`)
- [ ] UI 选地图界面
- [ ] 测试与日志

---
## 如何参与 / 使用
我们欢迎所有对 Android 逆向工程和虚拟定位技术感兴趣的开发者参与贡献。你可以通过以下方式加入我们：
1. Fork 本仓库。
2. Clone 到本地：`git clone https://github.com/你的用户名/FakeShadow.git`。
3. 在 Android Studio 中打开项目，进行学习、修改或功能扩展。
4. 欢迎提交 Pull Request（PR）分享你的改进、新功能或 Bug 修复！

### 贡献方式
我们热忱欢迎各种形式的贡献，包括但不限于：
- **功能开发**：实现新的 Hook 点、UI 界面或防检测机制。
- **代码优化**：改进现有代码的性能、可读性或稳定性。
- **文档完善**：优化项目说明、使用指南或技术文档。
- **Bug 报告与修复**：提交 Bug 报告，或提供 Bug 修复方案。

---
## 参考资料
- LSPosed 官网：[https://github.com/LSPosed/LSPosed](https://github.com/LSPosed/LSPosed)
- Xposed API：[https://github.com/rovo89/XposedBridge](https://github.com/rovo89/XposedBridge)
- 类似开源项目：FakeLocation ([https://github.com/Lerist/FakeLocation](https://github.com/Lerist/FakeLocation))

---
## 联系方式
如果你有任何问题、建议或合作意向，欢迎通过以下方式联系项目维护者：
- GitHub Issues: [在 FakeShadow 仓库中提交 Issue](https://github.com/WT667/FakeShadow/issues)
- GitHub Profile: [WT667](https://github.com/WT667)
我们期待与你共同学习和进步！
