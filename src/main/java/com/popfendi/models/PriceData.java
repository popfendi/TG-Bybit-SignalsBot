package com.popfendi.models;

import com.popfendi.repository.DataManager;

import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;

// price data update that comes from bybit
public class PriceData {
    private String topic;
    private String symbol;
    private BigDecimal price;
    private long ts;

    private PropertyChangeSupport ps = new PropertyChangeSupport(this);

    public PriceData() {
    }

    public PriceData(String topic, String symbol, BigDecimal price, long ts) {
        this.topic = topic;
        this.symbol = symbol;
        this.price = price;
        this.ts = ts;
        ps.addPropertyChangeListener(new DataManager().getInstance());
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        BigDecimal oldVal = getPrice();
        ps.firePropertyChange("price", oldVal, price); // sends price change event to observer
        this.price = price;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }
}
