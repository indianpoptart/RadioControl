package com.nikhilparanjape.radiocontrol.rootUtils;

/**
 * Created by admin on 8/2/2017.
 */

import android.graphics.Color;

import java.util.Random;

/**
 * Random color generator
 *
 * @author manolovn
 */
public class RandomColorGenerator implements ColorGenerator {

    private Random rand = new Random();

    public RandomColorGenerator() {

    }

    @Override
    public int nextColor() {
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);

        return Color.rgb(r, g, b);
    }

    @Override
    public void setCount(int count) {
        // not necessary
    }
}