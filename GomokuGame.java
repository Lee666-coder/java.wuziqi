import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GomokuGame extends JFrame {

    // 棋盘参数
    private static final int BOARD_SIZE = 15;       // 15x15
    private static final int CELL_SIZE = 40;        // 每格像素
    private static final int PADDING = 40;          // 边缘留白
    private static final int STONE_RADIUS = 16;     // 棋子半径

    // 棋子枚举
    private enum Stone {
        EMPTY, BLACK, WHITE
    }

    private Stone[][] board = new Stone[BOARD_SIZE][BOARD_SIZE];
    private boolean blackTurn = true;   // true 黑棋回合，false 白棋
    private boolean gameOver = false;

    public GomokuGame() {
        initBoard();
        setTitle("五子棋 - Java Gomoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        BoardPanel panel = new BoardPanel();
        setContentPane(panel);

        int size = PADDING * 2 + CELL_SIZE * (BOARD_SIZE - 1);
        setSize(size + 16, size + 39); // 适当加上窗口边框
        setLocationRelativeTo(null);
    }

    // 初始化棋盘为空
    private void initBoard() {
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = Stone.EMPTY;
            }
        }
    }

    // 检查是否五连
    private boolean checkWin(int lastRow, int lastCol) {
        Stone s = board[lastRow][lastCol];
        if (s == Stone.EMPTY) return false;

        // 四个方向：水平、垂直、主对角线、副对角线
        int[][] dirs = {
                {0, 1},  // 水平
                {1, 0},  // 垂直
                {1, 1},  // 主对角
                {1, -1}  // 副对角
        };

        for (int[] d : dirs) {
            int dr = d[0];
            int dc = d[1];
            int count = 1;

            // 正向
            int r = lastRow + dr;
            int c = lastCol + dc;
            while (inBounds(r, c) && board[r][c] == s) {
                count++;
                r += dr;
                c += dc;
            }

            // 反向
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

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE;
    }

    // 检查是否平局（棋盘无空位）
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

    // 重开一局
    private void restartGame() {
        initBoard();
        blackTurn = true;
        gameOver = false;
        repaint();
    }

    // 棋盘绘制面板
    private class BoardPanel extends JPanel {

        public BoardPanel() {
            setPreferredSize(new Dimension(
                    PADDING * 2 + CELL_SIZE * (BOARD_SIZE - 1),
                    PADDING * 2 + CELL_SIZE * (BOARD_SIZE - 1)
            ));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (gameOver) return;

                    int x = e.getX();
                    int y = e.getY();

                    // 计算点击的行列
                    int col = Math.round((x - PADDING) / (float) CELL_SIZE);
                    int row = Math.round((y - PADDING) / (float) CELL_SIZE);

                    if (!inBounds(row, col)) {
                        return;
                    }

                    if (board[row][col] != Stone.EMPTY) {
                        return; // 已有棋子
                    }

                    board[row][col] = blackTurn ? Stone.BLACK : Stone.WHITE;
                    repaint();

                    // 判断输赢
                    if (checkWin(row, col)) {
                        gameOver = true;
                        String winner = blackTurn ? "黑棋" : "白棋";
                        JOptionPane.showMessageDialog(
                                GomokuGame.this,
                                "游戏结束，" + winner + " 获胜！",
                                "获胜播报",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        int option = JOptionPane.showConfirmDialog(
                                GomokuGame.this,
                                "是否再来一局？",
                                "重新开始",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (option == JOptionPane.YES_OPTION) {
                            restartGame();
                        }
                        return;
                    }

                    if (isDraw()) {
                        gameOver = true;
                        JOptionPane.showMessageDialog(
                                GomokuGame.this,
                                "棋盘已满，平局！",
                                "平局",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        int option = JOptionPane.showConfirmDialog(
                                GomokuGame.this,
                                "是否再来一局？",
                                "重新开始",
                                JOptionPane.YES_NO_OPTION
                        );
                        if (option == JOptionPane.YES_OPTION) {
                            restartGame();
                        }
                        return;
                    }

                    // 交换回合
                    blackTurn = !blackTurn;
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            // 抗锯齿
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // 绘制“木制”背景：偏棕色渐变
            Color lightWood = new Color(222, 184, 135);
            Color darkWood = new Color(160, 120, 80);
            GradientPaint wood = new GradientPaint(
                    0, 0, lightWood,
                    width, height, darkWood
            );
            g2.setPaint(wood);
            g2.fillRect(0, 0, width, height);

            // 绘制棋盘网格
            g2.setColor(new Color(80, 50, 20));
            for (int i = 0; i < BOARD_SIZE; i++) {
                int x = PADDING + i * CELL_SIZE;
                int y = PADDING + i * CELL_SIZE;
                // 竖线
                g2.drawLine(x, PADDING, x, PADDING + CELL_SIZE * (BOARD_SIZE - 1));
                // 横线
                g2.drawLine(PADDING, y, PADDING + CELL_SIZE * (BOARD_SIZE - 1), y);
            }

            // 星位
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

            // 画棋子
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

            // 状态文本
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
            String status;
            if (gameOver) {
                status = "游戏结束";
            } else {
                status = blackTurn ? "当前轮到：黑棋" : "当前轮到：白棋";
            }
            g2.drawString(status, 10, 20);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        // 使用系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            GomokuGame game = new GomokuGame();
            game.setVisible(true);
        });
    }
}

