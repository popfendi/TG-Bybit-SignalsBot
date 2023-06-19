package com.popfendi.models;

public class Stats {
    private int total;
    private int wins;
    private int open;
    private int sl;
    private int tp1;
    private int tp2;
    private int tp3;

    public Stats(int total, int wins, int open, int sl, int tp1, int tp2, int tp3) {
        this.total = total;
        this.wins = wins;
        this.open = open;
        this.sl = sl;
        this.tp1 = tp1;
        this.tp2 = tp2;
        this.tp3 = tp3;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    public int getSl() {
        return sl;
    }

    public void setSl(int sl) {
        this.sl = sl;
    }

    public int getTp1() {
        return tp1;
    }

    public void setTp1(int tp1) {
        this.tp1 = tp1;
    }

    public int getTp2() {
        return tp2;
    }

    public void setTp2(int tp2) {
        this.tp2 = tp2;
    }

    public int getTp3() {
        return tp3;
    }

    public void setTp3(int tp3) {
        this.tp3 = tp3;
    }
}
