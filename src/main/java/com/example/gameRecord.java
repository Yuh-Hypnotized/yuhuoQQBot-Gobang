package com.example;

public class gameRecord {
    public long player1;
    public long player2;
    public long winner;
    public gameRecord() {}
    public gameRecord(long p1, long p2, long win) {
        player1 = p1;
        player2 = p2;
        winner = win;
    }
}
