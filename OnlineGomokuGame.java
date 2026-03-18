import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 支持联机对战的五子棋（局域网，两人一主一客）
 * 一方选择 Host 作为房主（黑棋），另一方选择 Join 输入 IP 作为客户端（白棋）。
 */
public class OnlineGomokuGame extends JFrame {

    private static final int BOARD_SIZE = 15;
    private static final int CELL_SIZE = 40;
    private static final int PADDING = 40;
    private static final int STONE_RADIUS = 16;
    private static final int DEFAULT_PORT = 5000;

    private enum Stone {
        EMPTY, BLACK, WHITE
    }

    private enum Mode {
        HOST, CLIENT
    }

    private final Stone[][] board = new Stone[BOARD_SIZE][BOARD_SIZE];
    private boolean gameOver = false;

    private Mode mode;
    private Stone myStone;
    private Stone opponentStone;
    private boolean myTurn;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public OnlineGomokuGame() {
        initBoard();
        chooseModeAndConnect();

        setTitle("联机五子棋 - Online Gomoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        BoardPanel panel = new BoardPanel();
        setContentPane(panel);

        int size = PADDING * 2 + CELL_SIZE * (BOARD_SIZE - 1);
        setSize(size + 16, size + 39);
        setLocationRelativeTo(null);
    }

    private void initBoard() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = Stone.EMPTY;
            }
        }
    }

    private void chooseModeAndConnect() {
        Object[] options = {"房主（Host）", "加入（Client）"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "请选择联机模式：\n房主作为黑棋等待连接；加入者作为白棋输入房主 IP。",
                "选择联机模式",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            mode = Mode.HOST;
            myStone = Stone.BLACK;
            opponentStone = Stone.WHITE;
            myTurn = true;
            startHost();
        } else if (choice == 1) {
            mode = Mode.CLIENT;
            myStone = Stone.WHITE;
            opponentStone = Stone.BLACK;
            myTurn = false;
            startClient();
        } else {
            System.exit(0);
        }
    }

    private void startHost() {
        String portStr = JOptionPane.showInputDialog(
                this,
                "输入监听端口（默认 " + DEFAULT_PORT + "）：",
                String.valueOf(DEFAULT_PORT)
        );
        int port = DEFAULT_PORT;
        try {
            if (portStr != null && !portStr.trim().isEmpty()) {
                port = Integer.parseInt(portStr.trim());
            }
        } catch (NumberFormatException ignored) {
        }

        int finalPort = port;
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(finalPort)) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                OnlineGomokuGame.this,
                                "房主已启动，端口 " + finalPort + "。\n等待对手连接...",
                                "房主模式",
                                JOptionPane.INFORMATION_MESSAGE
                        )
                );
                socket = serverSocket.accept();
                setupStreams();
                startReceiveLoop();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                OnlineGomokuGame.this,
                                "对手已连接，游戏开始！你是黑棋，先手。",
                                "连接成功",
                                JOptionPane.INFORMATION_MESSAGE
                        )
                );
            } catch (IOException e) {
                showErrorAndExit("房主启动失败：" + e.getMessage());
            }
        }, "HostThread").start();
    }

    private void startClient() {
        String host = JOptionPane.showInputDialog(
                this,
                "请输入房主 IP 地址：",
                "127.0.0.1"
        );
        if (host == null || host.trim().isEmpty()) {
            System.exit(0);
        }
        String portStr = JOptionPane.showInputDialog(
                this,
                "输入房主端口（默认 " + DEFAULT_PORT + "）：",
                String.valueOf(DEFAULT_PORT)
        );
        int port = DEFAULT_PORT;
        try {
            if (portStr != null && !portStr.trim().isEmpty()) {
                port = Integer.parseInt(portStr.trim());
            }
        } catch (NumberFormatException ignored) {
        }
        int finalPort = port;
        new Thread(() -> {
            try {
                socket = new Socket(host.trim(), finalPort);
                setupStreams();
                startReceiveLoop();
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(
                                OnlineGomokuGame.this,
                                "已连接到房主！你是白棋，后手。",
                                "连接成功",
                                JOptionPane.INFORMATION_MESSAGE
                        )
                );
            } catch (IOException e) {
                showErrorAndExit("连接房主失败：" + e.getMessage());
            }
        }, "ClientThread").start();
    }

    private void setupStreams() throws IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    private void startReceiveLoop() {
        Thread t = new Thread(() -> {
            try {
                while (!gameOver && !socket.isClosed()) {
                    int row = in.readInt();
                    int col = in.readInt();
                    SwingUtilities.invokeLater(() -> handleRemoteMove(row, col));
                }
            } catch (IOException e) {
                if (!gameOver) {
                    showErrorAndExit("连接中断：" + e.getMessage());
                }
            }
        }, "ReceiveThread");
        t.setDaemon(true);
        t.start();
    }

    private void handleRemoteMove(int row, int col) {
        if (gameOver || !inBounds(row, col) || board[row][col] != Stone.EMPTY) {
            return;
        }
        board[row][col] = opponentStone;
        repaint();

        if (checkWin(row, col)) {
            gameOver = true;
            JOptionPane.showMessageDialog(
                    this,
                    "游戏结束，对手获胜！",
                    "对局结果",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        if (isDraw()) {
            gameOver = true;
            JOptionPane.showMessageDialog(
                    this,
                    "棋盘已满，平局！",
                    "对局结果",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        myTurn = true;
    }

    private void sendMove(int row, int col) {
        if (out == null) return;
        try {
            out.writeInt(row);
            out.writeInt(col);
            out.flush();
        } catch (IOException e) {
            showErrorAndExit("发送落子失败：" + e.getMessage());
        }
    }

    private void showErrorAndExit(String msg) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    OnlineGomokuGame.this,
                    msg,
                    "错误",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        });
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE;
    }

    private boolean checkWin(int lastRow, int lastCol) {
        Stone s = board[lastRow][lastCol];
        if (s == Stone.EMPTY) return false;

        int[][] dirs = {
                {0, 1},
                {1, 0},
                {1, 1},
                {1, -1}
        };

        for (int[] d : dirs) {
            int dr = d[0];
            int dc = d[1];
            int count = 1;

            int r = lastRow + dr;
            int c = lastCol + dc;
            while (inBounds(r, c) && board[r][c] == s) {
                count++;
                r += dr;
                c += dc;
            }

            r = lastRow - dr;
            c = lastCol - dc;
            while (inBounds(r, c) && board[r][c] == s) {
                count++;
                r -= dr;
                c -= dc;
            }

            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    private boolean isDraw() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                if (board[r][c] == Stone.EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }

    private class BoardPanel extends JPanel {

        public BoardPanel() {
            setPreferredSize(new Dimension(
                    PADDING * 2 + CELL_SIZE * (BOARD_SIZE - 1),
                    PADDING * 2 + CELL_SIZE * (BOARD_SIZE - 1)
            ));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (gameOver || !myTurn) return;

                    int x = e.getX();
                    int y = e.getY();
                    int col = Math.round((x - PADDING) / (float) CELL_SIZE);
                    int row = Math.round((y - PADDING) / (float) CELL_SIZE);

                    if (!inBounds(row, col)) return;
                    if (board[row][col] != Stone.EMPTY) return;

                    board[row][col] = myStone;
                    repaint();
                    sendMove(row, col);

                    if (checkWin(row, col)) {
                        gameOver = true;
                        JOptionPane.showMessageDialog(
                                OnlineGomokuGame.this,
                                "游戏结束，你获胜！",
                                "对局结果",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }
                    if (isDraw()) {
                        gameOver = true;
                        JOptionPane.showMessageDialog(
                                OnlineGomokuGame.this,
                                "棋盘已满，平局！",
                                "对局结果",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }
                    myTurn = false;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            Color lightWood = new Color(222, 184, 135);
            Color darkWood = new Color(160, 120, 80);
            GradientPaint wood = new GradientPaint(
                    0, 0, lightWood,
                    width, height, darkWood
            );
            g2.setPaint(wood);
            g2.fillRect(0, 0, width, height);

            g2.setColor(new Color(80, 50, 20));
            for (int i = 0; i < BOARD_SIZE; i++) {
                int x = PADDING + i * CELL_SIZE;
                int y = PADDING + i * CELL_SIZE;
                g2.drawLine(x, PADDING, x, PADDING + CELL_SIZE * (BOARD_SIZE - 1));
                g2.drawLine(PADDING, y, PADDING + CELL_SIZE * (BOARD_SIZE - 1), y);
            }

            g2.setColor(new Color(80, 50, 20));
            int[] starPos = {3, 7, 11};
            int starRadius = 4;
            for (int r : starPos) {
                for (int c : starPos) {
                    int cx = PADDING + c * CELL_SIZE;
                    int cy = PADDING + r * CELL_SIZE;
                    g2.fillOval(cx - starRadius, cy - starRadius, starRadius * 2, starRadius * 2);
                }
            }

            for (int r = 0; r < BOARD_SIZE; r++) {
                for (int c = 0; c < BOARD_SIZE; c++) {
                    Stone s = board[r][c];
                    if (s == Stone.EMPTY) continue;
                    int cx = PADDING + c * CELL_SIZE;
                    int cy = PADDING + r * CELL_SIZE;
                    int d = STONE_RADIUS * 2;
                    int x = cx - STONE_RADIUS;
                    int y = cy - STONE_RADIUS;
                    if (s == Stone.BLACK) {
                        g2.setColor(Color.BLACK);
                        g2.fillOval(x, y, d, d);
                        g2.setColor(new Color(220, 220, 220, 80));
                        g2.fillOval(x + 4, y + 4, d - 10, d - 10);
                        g2.setColor(Color.DARK_GRAY);
                        g2.drawOval(x, y, d, d);
                    } else {
                        g2.setColor(Color.WHITE);
                        g2.fillOval(x, y, d, d);
                        g2.setColor(new Color(0, 0, 0, 60));
                        g2.fillOval(x + 5, y + 5, d - 8, d - 8);
                        g2.setColor(Color.GRAY);
                        g2.drawOval(x, y, d, d);
                    }
                }
            }

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
            String status;
            if (gameOver) {
                status = "游戏结束";
            } else {
                String role = (myStone == Stone.BLACK) ? "黑棋" : "白棋";
                status = (myTurn ? "轮到你落子 - " : "轮到对手落子 - ") + role;
            }
            g2.drawString(status, 10, 20);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> {
            OnlineGomokuGame game = new OnlineGomokuGame();
            game.setVisible(true);
        });
    }
}

