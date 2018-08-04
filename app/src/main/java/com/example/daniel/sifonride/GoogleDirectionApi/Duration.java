package com.example.daniel.sifonride.GoogleDirectionApi;

/**
 * Create d by DANIEL on 2/9/2018.
 */

public class Duration {

    public int value;
    public String text;

    public Duration() {
    }

    public Duration(String text, int value) {
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
