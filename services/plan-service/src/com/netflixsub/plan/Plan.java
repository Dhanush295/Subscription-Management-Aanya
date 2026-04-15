package com.netflixsub.plan;

import com.netflixsub.common.Json;

public class Plan {
    public final String code;
    public final String name;
    public final double monthlyInr;
    public final double yearlyInr;
    public final int screens;
    public final String quality;
    public final int trialDays;
    public final int yearlyDiscountPct;

    public Plan(String code, String name, double m, double y, int screens, String quality, int trialDays, int yearlyDiscountPct) {
        this.code = code;
        this.name = name;
        this.monthlyInr = m;
        this.yearlyInr = y;
        this.screens = screens;
        this.quality = quality;
        this.trialDays = trialDays;
        this.yearlyDiscountPct = yearlyDiscountPct;
    }

    @Override
    public String toString() {
        return "{\"code\":\"" + code + "\","
             + "\"name\":\"" + Json.esc(name) + "\","
             + "\"monthlyInr\":" + monthlyInr + ","
             + "\"yearlyInr\":" + yearlyInr + ","
             + "\"screens\":" + screens + ","
             + "\"quality\":\"" + quality + "\","
             + "\"trialDays\":" + trialDays + ","
             + "\"yearlyDiscountPct\":" + yearlyDiscountPct + "}";
    }
}
