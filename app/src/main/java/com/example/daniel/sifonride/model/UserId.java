package com.example.daniel.sifonride.model;

/**
 * Create d by DANIEL on 1/29/2018.
 */

public class UserId {
    public String userId;

    public <T extends UserId>T withId(final String id){
        this.userId = id;
        return (T)this;
    }
}
