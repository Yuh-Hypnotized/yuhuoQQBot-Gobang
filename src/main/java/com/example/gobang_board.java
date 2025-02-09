package com.example;

import java.util.ArrayList;
import java.util.List;
public class gobang_board {
    public long groupID;
    public long player1 = -1;
    public long player2 = -1;
    public boolean gameFlag = false;
    public int current_player = 1;
    public List<List<Integer>> board;
    public void initBoard (long id) {
        board = new ArrayList<>();
        groupID = id;
        for (int i=0; i<15; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j=0; j<15;j++) {
                row.add(0);
            }
            board.add(row);
        }
    }

    public void clearBoard() {
        for (int i=0; i<15; i++) {
            for (int j=0; j<15; j++) {
                board.get(i).set(j, 0);
            }
        }
    }

    public boolean checkVcitory (int current_player) {
        for (int i=0; i<15; i++) {
            for (int j=0; j<15; j++) {
                if (board.get(i).get(j) == current_player) {
                    if (checkDirection(i,j,current_player,0,1) ||
                    checkDirection(i,j,current_player,1,0) ||
                    checkDirection(i,j,current_player,1,1) ||
                    checkDirection(i,j,current_player,-1,1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkDirection(int i, int j, int current_player, int dx, int dy) {
        int count = 1;
        int x = i;
        int y = j;
        //向(dx, dy)向量出发
        while(true) {
            x += dx;
            y += dy;
            if (x<0 || x>=15 || y<0 || y>=15) {
                break;
            }
            if (board.get(x).get(y) != current_player) {
                break;
            }
            count++;
        }
        //回到起点，然后向(-dx, -dy)向量出发
        x = i;
        y = j;
        while (true) {
            x -= dx;
            y -= dy;
            if (x<0 || x>=15 || y<0 || y>=15) {
                break;
            }
            if (board.get(x).get(y) != current_player) {
                break;
            }
            count++;
        }
        return count >= 5;
    }
}
