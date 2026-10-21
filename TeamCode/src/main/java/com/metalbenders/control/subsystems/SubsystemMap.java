package com.metalbenders.control.subsystems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubsystemMap {

    private final Map<Class<? extends CloseableSubsystem>, CloseableSubsystem> registry = new HashMap<>();

    public <T extends CloseableSubsystem> void register(Class<T> clazz, T instance) {
        registry.put(clazz, instance);
    }

    public <T extends CloseableSubsystem> T get(Class<T> clazz) {
        return clazz.cast(registry.get(clazz));
    }

    public List<CloseableSubsystem> getAllSubsystems() {
        return new ArrayList<>(registry.values());
    }
}