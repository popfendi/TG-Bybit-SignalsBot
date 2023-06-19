package com.popfendi.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.popfendi.repository.DataManager;

import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.logging.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Signal {
    private String pair;
    private Direction direction;
    private BigDecimal entry;
    private BigDecimal tp1;
    private BigDecimal tp2;
    private BigDecimal tp3;

    private BigDecimal sl;

    private boolean openPosition;
    private Targets targetsHit;

    private Date timestamp;

    private String reqId;

    private BigDecimal profitPercentage;

    private PropertyChangeSupport ps = new PropertyChangeSupport(this);

    private static final Logger LOGGER = Logger.getLogger( Signal.class.getName() );

    public Signal() {
        ps.addPropertyChangeListener(DataManager.getInstance());
    }

    public Signal(String pair, Direction direction, BigDecimal entry, BigDecimal tp1, BigDecimal tp2, BigDecimal tp3, BigDecimal sl, boolean openPosition, Targets targetsHit, Date timestamp, String reqId) {
        this.pair = pair;
        this.direction = direction;
        this.entry = entry;
        this.tp1 = tp1;
        this.tp2 = tp2;
        this.tp3 = tp3;
        this.sl = sl;
        this.openPosition = openPosition;
        this.targetsHit = targetsHit;
        this.timestamp = timestamp;
        this.reqId = reqId;
        this.profitPercentage = null;
        ps.addPropertyChangeListener(DataManager.getInstance());
    }

    // checks targets against price and sends target events.
    public void checkTargets(BigDecimal price){
        if(openPosition){
            switch (direction) {
                case Long -> {
                    if (price.compareTo(tp3) >= 0) {
                        targetsHit.hitAllTps();
                        closeTrade();
                        tp3HitEvent();
                    } else if (price.compareTo(tp2) >= 0 && !targetsHit.isTp2()) {
                        targetsHit.setTp2(true);
                        targetsHit.setTp1(true);
                        calculateProfit();
                        tp2HitEvent();
                    } else if (price.compareTo(tp1) >= 0 && !targetsHit.isTp1()) {
                        targetsHit.setTp1(true);
                        calculateProfit();
                        tp1HitEvent();
                    } else if (price.compareTo(sl) < 0 && targetsHit.isTp1() && targetsHit.isTp2()) {
                        targetsHit.setSl(true);
                        closeTrade();
                        tp2ThenSlHitEvent();
                    } else if (price.compareTo(sl) < 0 && targetsHit.isTp1()) {
                        targetsHit.setSl(true);
                        closeTrade();
                        tp1ThenSlHitEvent();
                    } else if (price.compareTo(sl) < 0) {
                        targetsHit.setSl(true);
                        closeTrade();
                        slHitEvent();
                    }
                }
                case Short -> {
                    if (price.compareTo(tp3) <= 0) {
                        targetsHit.hitAllTps();
                        closeTrade();
                        tp3HitEvent();
                    } else if (price.compareTo(tp2) <= 0 && !targetsHit.isTp2()) {
                        targetsHit.setTp2(true);
                        targetsHit.setTp1(true);
                        calculateProfit();
                        tp2HitEvent();
                    } else if (price.compareTo(tp1) <= 0 && !targetsHit.isTp1()) {
                        targetsHit.setTp1(true);
                        calculateProfit();
                        tp1HitEvent();
                    } else if (price.compareTo(sl) > 0 && targetsHit.isTp1() && targetsHit.isTp2()) {
                        targetsHit.setSl(true);
                        closeTrade();
                        tp2ThenSlHitEvent();
                    } else if (price.compareTo(sl) > 0 && targetsHit.isTp1()) {
                        targetsHit.setSl(true);
                        closeTrade();
                        tp1ThenSlHitEvent();
                    } else if (price.compareTo(sl) > 0) {
                        targetsHit.setSl(true);
                        closeTrade();
                        slHitEvent();
                    }
                }
            }
        }
    }

    private void closeTrade(){
        setOpenPosition(false);
        calculateProfit();
        LOGGER.info("Closing trade: " + getPair() + " " + getDirection());
    }

    public void calculateProfit() {
        BigDecimal ONE_HUNDRED = new BigDecimal(100);
        BigDecimal tp3Difference = tp3.compareTo(entry) < 0 ?
                entry.multiply(ONE_HUNDRED).divide(tp3, 3, RoundingMode.HALF_UP).subtract(ONE_HUNDRED)  : tp3.multiply(ONE_HUNDRED).divide(entry, 3, RoundingMode.HALF_UP).subtract(ONE_HUNDRED) ;
        BigDecimal tp2Difference = tp2.compareTo(entry) < 0 ?
                entry.multiply(ONE_HUNDRED).divide(tp2, 3, RoundingMode.HALF_UP).subtract(ONE_HUNDRED)  : tp2.multiply(ONE_HUNDRED).divide(entry, 3, RoundingMode.HALF_UP).subtract(ONE_HUNDRED) ;
        BigDecimal tp1Difference = tp1.compareTo(entry) < 0 ?
                entry.multiply(ONE_HUNDRED).divide(tp1, 3, RoundingMode.HALF_UP).subtract(ONE_HUNDRED)  : tp1.multiply(ONE_HUNDRED).divide(entry, 3, RoundingMode.HALF_UP).subtract(ONE_HUNDRED) ;
        BigDecimal slDifference = sl.compareTo(entry) > 0 ?
                entry.multiply(ONE_HUNDRED).divide(sl, 3, RoundingMode.HALF_UP).subtract(ONE_HUNDRED) : sl.multiply(ONE_HUNDRED).divide(entry, 3, RoundingMode.HALF_UP).subtract(ONE_HUNDRED) ;


        if(targetsHit.isTp3()){
            setProfitPercentage(tp3Difference);
        } else if (targetsHit.isTp2() && targetsHit.isTp1() && !targetsHit.isSl()) {
            setProfitPercentage(tp2Difference);
        } else if (targetsHit.isTp1() && !targetsHit.isSl()) {
            setProfitPercentage(tp1Difference);
        } else if (targetsHit.isTp2() && targetsHit.isTp1() && targetsHit.isSl()) {
            setProfitPercentage(tp2Difference);
        } else if (targetsHit.isTp1() && targetsHit.isSl()) {
            setProfitPercentage(tp1Difference);
        } else if (targetsHit.isSl()) {
            setProfitPercentage(slDifference);
        }
    }


    private void slHitEvent(){
        ps.firePropertyChange("sl", false, true);
    }

    private void tp1HitEvent(){
        ps.firePropertyChange("tp1", false, true);
    }

    private void tp2HitEvent(){
        ps.firePropertyChange("tp2", false, true);
    }

    private void tp3HitEvent(){
        ps.firePropertyChange("tp3", false, true);
    }

    private void tp2ThenSlHitEvent(){
        ps.firePropertyChange("tp2ThenSl", false, true);
    }

    private void tp1ThenSlHitEvent(){
        ps.firePropertyChange("tp1ThenSl", false, true);
    }

    public String calulatePeriod(){
        long timestampInSeconds = timestamp.getTime() / 1000L;
        long nowInSeconds = new Date().getTime() / 1000L;
        long difference = nowInSeconds - timestampInSeconds;

        long hours = difference / 3600;
        long minutes = (difference % 3600) / 60;

        if (hours > 0){
            return String.format("%02d hours %02d minutes", hours, minutes);
        } else {
            return String.format("%02d minutes", minutes);
        }
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public BigDecimal getEntry() {
        return entry;
    }

    public void setEntry(BigDecimal entry) {
        this.entry = entry;
    }

    public BigDecimal getTp1() {
        return tp1;
    }

    public void setTp1(BigDecimal tp1) {
        this.tp1 = tp1;
    }

    public BigDecimal getTp2() {
        return tp2;
    }

    public void setTp2(BigDecimal tp2) {
        this.tp2 = tp2;
    }

    public BigDecimal getTp3() {
        return tp3;
    }

    public void setTp3(BigDecimal tp3) {
        this.tp3 = tp3;
    }

    public BigDecimal getSl() {
        return sl;
    }

    public void setSl(BigDecimal sl) {
        this.sl = sl;
    }

    public boolean isOpenPosition() {
        return openPosition;
    }

    public void setOpenPosition(boolean openPosition) {
        this.openPosition = openPosition;
    }

    public Targets getTargetsHit() {
        return targetsHit;
    }

    public void setTargetsHit(Targets targetsHit) {
        this.targetsHit = targetsHit;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public BigDecimal getProfitPercentage() {
        return profitPercentage;
    }

    public void setProfitPercentage(BigDecimal profitPercentage) {
        this.profitPercentage = profitPercentage;
    }
}
