# FakeShadow
学习安卓虚拟定位模块的开源练习项目（基于 LSPosed）

# FakeShadow 学习项目

这是一个**纯学习用途**的开源练习项目，尝试基于 **LSPosed / Xposed** 框架实现类似“Shadow (影子)”模块的虚拟定位功能。

**重要声明**：仅用于技术研究和个人学习，**严禁**用于任何违规、作弊、绕过平台风控的场景（如企业打卡、游戏外挂等）。一切后果自负。

## 项目目标
- Hook 系统定位服务（GPS、Fused Location 等）
- 伪造 Wi-Fi / 基站信息（防检测）
- 简单 UI 选点 + 开始模拟
- 参考闭源模块 Shadow 的部分思路，自己从零实现

## 当前进度
- [x] 创建仓库 + 基本 README
- [ ] 添加项目结构（app / xposed 模块）
- [ ] 核心 Hook 代码示例
- [ ] UI 选地图界面
- [ ] 测试与日志

## 如何参与 / 使用
1. Fork 本仓库
2. Clone 到本地：`git clone https://github.com/你的用户名/FakeShadow.git`
3. 在 Android Studio 打开，学习/修改代码
4. 欢迎提交 PR（Pull Request）分享你的改进！

## 参考资料
- LSPosed 官网：https://github.com/LSPosed/LSPosed
- Xposed API：https://github.com/rovo89/XposedBridge
- 类似开源项目：FakeLocation (https://github.com/Lerist/FakeLocation)

有问题？欢迎在 Issues 里提问～