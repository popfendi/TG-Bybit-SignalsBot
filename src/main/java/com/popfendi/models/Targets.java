package com.popfendi.models;

public class Targets {
    private boolean tp1;

    private boolean tp2;

    private boolean tp3;

    private boolean sl;

    public Targets() {
        this.tp1 = false;
        this.tp2 = false;
        this.tp3 = false;
        this.sl = false;
    }

    public boolean isSl() {
        return sl;
    }

    public void setSl(boolean sl) {
        this.sl = sl;
    }

    public boolean isTp1() {
        return tp1;
    }

    public void setTp1(boolean tp1) {
        this.tp1 = tp1;
    }

    public boolean isTp2() {
        return tp2;
    }

    public void setTp2(boolean tp2) {
        this.tp2 = tp2;
    }

    public boolean isTp3() {
        return tp3;
    }

    public void setTp3(boolean tp3) {
        this.tp3 = tp3;
    }

    public void hitAllTps(){
        setTp1(true);
        setTp2(true);
        setTp3(true);
    }
}
