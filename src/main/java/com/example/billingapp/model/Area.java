package com.example.billingapp.model;



public enum Area {
    DKI("DKI"),
    BANTEN("Banten"),
    JABAR("Jabar"),
    JATENG("Jateng"),
    JATIM("Jatim"),
    SUMBAGSUL("Sumbagsul"),
    SUMBAGSEL("Sumbagsel"),
    SUMBAGUT("Sumbagut"),
    EASTERN("Eastern");

    private final String displayName;

    Area(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}