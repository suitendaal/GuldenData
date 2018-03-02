package com.example.svenu.guldendata;

/**
 * Created by svenu on 20-2-2018.
 */

public class GuldenData {

    private float factor;
    private long time;

    public GuldenData(float factor, long time) {
        this.factor = factor;
        this.time = time;
    }

    public float getFactor() {
        return this.factor;
    }

    public long getTimeSeconds() {
        return this.time;
    }

    public long getTimeMillis() {
        return 1000 * this.time;
    }

    public float getAmount(int guldens) {
        return this.factor * guldens;
    }
}
