package com.example.daniel.sifonride.GoogleDirectionApi;

/**
 * Create  d by DANIEL on 2/9/2018.
 */

public class Distance {
    private int value;
    private String text;

    public Distance() {
    }

    public Distance(String text, int value) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
