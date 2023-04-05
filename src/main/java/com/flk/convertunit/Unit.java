package com.flk.convertunit;

/**
 * 单位数据
 */
public class Unit {
    public String name;
    public float step;

    public Unit(String name, float step) {
        this.name = name;
        this.step = step;
    }

    public static Unit create(String name, float step ){
        return new Unit(name, step);
    }
}
