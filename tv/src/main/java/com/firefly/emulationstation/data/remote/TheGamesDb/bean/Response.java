package com.firefly.emulationstation.data.remote.TheGamesDb.bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public abstract class Response implements Serializable {
    private int code;
    private String status;

    private Page pages;
    @SerializedName("remaining_monthly_allowance")
    private int remainingMonthlyAllowance;
    @SerializedName("extra_allowance")
    private int extraAllowance;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Page getPages() {
        return pages;
    }

    public void setPages(Page pages) {
        this.pages = pages;
    }

    public int getRemainingMonthlyAllowance() {
        return remainingMonthlyAllowance;
    }

    public void setRemainingMonthlyAllowance(int remainingMonthlyAllowance) {
        this.remainingMonthlyAllowance = remainingMonthlyAllowance;
    }

    public int getExtraAllowance() {
        return extraAllowance;
    }

    public void setExtraAllowance(int extraAllowance) {
        this.extraAllowance = extraAllowance;
    }
}
