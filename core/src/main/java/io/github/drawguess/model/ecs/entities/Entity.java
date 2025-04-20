package io.github.drawguess.model.ecs.entities;

import java.util.HashMap;
import java.util.Map;

public class Entity {
    private final Map<Class<?>, Object> components = new HashMap<>();

    public <T> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    public <T> T getComponent(Class<T> componentType) {
        return componentType.cast(components.get(componentType));
    }

    public <T> boolean hasComponent(Class<T> componentType) {
        return components.containsKey(componentType);
    }
}
