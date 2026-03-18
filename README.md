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
├── GomokuGame.java   # 主程序源码（Swing 窗体 + 棋盘逻辑）
└── README.md         # 项目说明
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

