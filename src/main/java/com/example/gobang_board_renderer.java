package com.example;

import kotlinx.serialization.descriptors.StructureKind;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;

public class gobang_board_renderer {
    private static final int CELL_SIZE = 30; // 每个格子像素
    private static final int BOARD_SIZE = 15;
    private static final int BOARD_MARGIN = 20; // 边距
    private static final int LABEL_MARGIN = 30; //标签额外边距
    private static final Color BACKGROUND_COLOR = new Color(245, 209, 158); // 棋盘背景色
    private static final Color LINE_COLOR = Color.BLACK; // 线条颜色
    private static final Color PLAYER1_COLOR = Color.BLACK; // 玩家1棋子颜色
    private static final Color PLAYER2_COLOR = Color.WHITE; // 玩家2棋子颜色

    public static File generateChessBoardImage(List<List<Integer>> board) throws IOException {
        // 棋盘区域的尺寸：棋盘网格实际大小 = CELL_SIZE * (BOARD_SIZE-1) 加上两侧的内边距
        int boardPixelSize = CELL_SIZE * (BOARD_SIZE - 1) + BOARD_MARGIN * 2;
        // 整体图片的尺寸：左侧和上侧各预留LABEL_MARGIN，其余右侧和下侧保持BOARD_MARGIN
        int imageSize = LABEL_MARGIN + boardPixelSize;

        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 开启抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 绘制背景
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(0, 0, imageSize, imageSize);

        // 棋盘的起始点（左上角），整体偏移LABEL_MARGIN（左侧、上侧保留用于标号）
        int boardStart = LABEL_MARGIN;

        // 绘制棋盘网格
        g2d.setColor(LINE_COLOR);
        for (int i = 0; i < BOARD_SIZE; i++) {
            // 计算每一条线的位置：从左侧内边距开始
            int pos = boardStart + BOARD_MARGIN + i * CELL_SIZE;
            // 绘制横线
            g2d.drawLine(boardStart + BOARD_MARGIN, pos, boardStart + boardPixelSize - BOARD_MARGIN, pos);
            // 绘制竖线
            g2d.drawLine(pos, boardStart + BOARD_MARGIN, pos, boardStart + boardPixelSize - BOARD_MARGIN);
        }

        // 设置字体，用于绘制标号
        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        FontMetrics fm = g2d.getFontMetrics();

        // 绘制左侧行标号（0~14），使得标号居中显示于每一行对应的棋盘线上
        for (int i = 0; i < BOARD_SIZE; i++) {
            String label = String.valueOf(i);
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getAscent();
            // 水平位置：在LABEL_MARGIN区域中水平居中
            int x = (LABEL_MARGIN - labelWidth) / 2;
            // 垂直位置：对应棋盘线上，稍微调整一下使其居中
            int y = boardStart + BOARD_MARGIN + i * CELL_SIZE + labelHeight / 2;
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, x, y);
        }

        // 绘制上侧列标号（0~14）
        for (int i = 0; i < BOARD_SIZE; i++) {
            String label = String.valueOf(i);
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getAscent();
            // 水平位置：棋盘的第i列，居中对齐
            int x = boardStart + BOARD_MARGIN + i * CELL_SIZE - labelWidth / 2;
            // 垂直位置：在LABEL_MARGIN区域中居中
            int y = (LABEL_MARGIN + labelHeight) / 2;
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, x, y);
        }

        // 绘制棋子（注意：棋子坐标也需要加上偏移）
        for (int x = 0; x < BOARD_SIZE; x++) {
            for (int y = 0; y < BOARD_SIZE; y++) {
                int cellValue = board.get(x).get(y);
                if (cellValue != 0) {
                    int centerX = boardStart + BOARD_MARGIN + y * CELL_SIZE;
                    int centerY = boardStart + BOARD_MARGIN + x * CELL_SIZE;
                    drawChessPiece(g2d, centerX, centerY, cellValue);
                }
            }
        }

        g2d.dispose();

        // 保存为临时文件
        File tempFile = File.createTempFile("chessboard-", ".jpg");
        ImageIO.write(image, "JPEG", tempFile);
        return tempFile;
    }

    private static void drawChessPiece(Graphics2D g2d, int x, int y, int player) {
        Color color = (player == 1) ? PLAYER1_COLOR : PLAYER2_COLOR;
        int radius = CELL_SIZE / 2 - 2;

        // 绘制棋子
        g2d.setColor(color);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // 绘制边框（可选）
        g2d.setColor(Color.BLACK);
        g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);
    }
}