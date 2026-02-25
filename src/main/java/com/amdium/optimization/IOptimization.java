package com.amdium.optimization;

public interface IOptimization {

    String getName();

    String getDescription();

    void apply();

    default void tick() {}

    default void periodicUpdate() {}

    boolean isActive();

    void disable();
}
