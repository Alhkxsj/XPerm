# XPerm

XPerm是一个基于Android平台的权限管理服务应用，主要功能是为其他应用提供root权限执行命令的能力。该项目通过创建一个前台服务来管理和执行需要root权限的命令，允许客户端通过网络接口发送命令请求，并获得执行结果。

## 功能特性

- **后台服务运行**：XPerm以一个前台服务的形式运行，确保应用在后台持续工作
- **Root权限管理**：应用需要root权限才能正常工作，通过`su -c`命令执行特权操作
- **网络通信**：通过TCP Socket（端口38388）接收外部命令请求并返回执行结果
- **Shell权限授权**：为第三方应用提供shell命令执行权限
- **无线调试配对**：提供界面化操作，启用设备的无线ADB调试功能
- **权限管理**：与Magisk等root管理工具集成，管理应用的权限
- **Shizuku兼容**：提供与Shizuku相似的API接口，兼容现有的Shizuku应用

## 安装要求

- Android 6.0+ (API level 23+)
- Root权限或Shizuku/Magisk环境
- 已安装并启用Magisk或类似root管理工具

## 使用方法

### 服务启动
1. 安装XPerm应用
2. 授予必要的权限（包括root权限）
3. 启动XPerm服务

### 客户端使用
客户端应用可以通过连接到端口38388与XPerm服务通信，支持以下命令：

- `PING` - 测试连接
- `VERSION` - 获取服务版本
- `EXEC:command` - 执行root命令
- `CHECK_AUTH:packageName` - 检查应用授权状态
- `GRANT:packageName` - 授权应用
- `REVOKE:packageName` - 撤销应用授权

## 架构设计

### 主要组件
- **MainActivity** - 应用主界面，提供服务控制和配置功能
- **XPermService** - 核心服务，启动Socket服务器并处理命令
- **CommandExecutor** - 命令执行工具类，处理root命令执行
- **PermissionManager** - 权限管理器，管理应用授权状态
- **Shizuku兼容API** - 提供与Shizuku相似的接口

## 许可证

本项目采用GNU通用公共许可证v3.0 (GPLv3)。

```
XPerm - Android权限管理服务
Copyright (C) 2025

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## 安全说明

本应用设计用于需要高级系统访问权限的场景。使用时请注意：
- 仅在必要时授予应用root权限
- 仔细管理哪些应用被授权使用XPerm服务
- 定期检查和清理授权应用列表
- 了解每个使用XPerm服务的应用的权限需求

## 贡献

欢迎提交Issue和Pull Request来改进XPerm。在提交代码前，请确保：
- 代码符合项目风格
- 所有更改都经过充分测试
- 文档已更新

## 隐私政策

XPerm不收集、存储或传输任何用户数据。所有操作都在设备本地执行，不连接到任何外部服务器。