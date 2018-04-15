package com.martinborjesson.usbmousetoserialconfiguration.data;

import java.io.Serializable;

public class MouseSettings implements Serializable, Cloneable {
    static final long serialVersionUID = 57834579834792194L;
    private boolean power = false;
    private String mouse = null;
    private String protocol = null;
    private String version = null;
    private Double rate;
    private Double x;
    private Double y;
    private Boolean swap;

    public MouseSettings() {}

    public MouseSettings(MouseSettings s) {
        power = s.power;
        mouse = s.mouse;
        version = s.version;
        x = s.x;
        y = s.y;
        swap = s.swap;
    }

    @Override
    public MouseSettings clone() {
        return new MouseSettings(this);
    }

    public MouseSettings setPower(boolean power) {
        this.power = power;
        return this;
    }

    public boolean isPower() {
        return power;
    }

    public MouseSettings setMouse(String mouse) {
        this.mouse = mouse;
        return this;
    }

    public String getMouse() {
        return mouse;
    }

    public MouseSettings setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public MouseSettings setRate(Double rate) {
        this.rate = rate;
        return this;
    }

    public Double getRate() {
        return rate;
    }

    public MouseSettings setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public MouseSettings setX(Double x) {
        this.x = x;
        return this;
    }

    public Double getX() {
        return x;
    }

    public MouseSettings setY(Double y) {
        this.y = y;
        return this;
    }

    public Double getY() {
        return y;
    }

    public MouseSettings setSwap(Boolean swap) {
        this.swap = swap;
        return this;
    }

    public Boolean getSwap() {
        return swap;
    }
}
