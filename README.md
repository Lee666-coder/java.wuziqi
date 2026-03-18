# Java 五子棋游戏（Gomoku）

这是一个使用 **Java Swing** 编写的简单五子棋游戏，棋盘采用木纹风格背景，支持黑白棋对战，自动判断五连胜利，并在游戏结束时弹出获胜播报。

## 功能特性

- 15 x 15 棋盘，木制背景效果
- 黑白双方轮流落子
- 自动检测横、竖、斜方向的五子连线
- 游戏结束后弹出 **“黑棋/白棋获胜”** 或 **平局** 提示
- 支持一键重新开始新对局

## 环境要求

- 已安装 **JDK 8+**（推荐 11 或 17）
- 已将 `javac`、`java` 加入环境变量 `PATH`

## 项目结构

```text
java.wuziqi/
├── GomokuGame.java        # 本地双人对战版（同屏对战）
├── OnlineGomokuGame.java  # 联机对战版（局域网，两人一主一客）
└── README.md              # 项目说明
```

## 编译与运行

在项目根目录（包含 `GomokuGame.java` 的目录）执行：

```bash
javac GomokuGame.java
java GomokuGame
```

在 Windows PowerShell 中可以这样：

```powershell
cd "d:\Vibe coding CURSOR\java.wuziqi"
javac GomokuGame.java
java GomokuGame
```

## 使用说明

- 鼠标左键点击棋盘交叉点即可落子
- 窗口左上角会显示当前轮到的棋方（黑棋/白棋）
- 当任意一方连成五子时，弹出对话框播报获胜方，并询问是否重新开始
- 若棋盘下满且无人五连，则判定为平局，并可选择是否重开一局

## 联机对战（OnlineGomokuGame）

### 编译与运行

```bash
javac OnlineGomokuGame.java
java OnlineGomokuGame
```

或在 Windows PowerShell 中：

```powershell
cd "d:\Vibe coding CURSOR\java.wuziqi"
javac OnlineGomokuGame.java
java OnlineGomokuGame
```

### 联机规则

- 该模式基于 **TCP Socket**，适合在同一局域网内两台电脑对战。
- 启动后会弹出模式选择：
  - **房主（Host）**：作为黑棋，先手，同时开启服务器等待对手连接。
  - **加入（Client）**：作为白棋，后手，需要输入房主的 IP 和端口。
- 默认端口为 `5000`，如无特殊需要，可直接回车使用默认值。

### 对战步骤示例

1. **房主电脑**：
   - 运行 `java OnlineGomokuGame`，选择 “房主（Host）”；
   - 确认/修改端口（默认 5000）后，会提示“等待对手连接”。
2. **加入方电脑**：
   - 运行 `java OnlineGomokuGame`，选择 “加入（Client）”；
   - 输入房主电脑在局域网中的 IP 地址（如 `192.168.x.x`）和端口（与房主一致）。
3. 连接成功后：
   - 房主为 **黑棋先手**，客户端为 **白棋后手**；
   - 双方的落子会通过网络同步显示在各自棋盘上；
   - 胜负和平局会在双方界面同步播报。


