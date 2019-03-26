package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

import java.io.Serializable;

public class Page implements Serializable {
    private String previous;
    private String current;
    private String next;

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }
}
