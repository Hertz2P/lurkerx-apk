package com.sweetjuice.core;

import com.sweetjuice.plugin.SweetJuiceWidgetFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * WidgetRegistry maintains a mapping of custom widget type identifiers to their 
 * respective {@link SweetJuiceWidgetFactory} implementations.
 */
class WidgetRegistry {
    private final Map<String, SweetJuiceWidgetFactory> widgetFactories = new HashMap<>();

    /**
     * Registers a widget factory using its default type identifier.
     * 
     * @param factory the factory to register.
     */
    void registerWidgetFactory(SweetJuiceWidgetFactory factory) {
        widgetFactories.put(factory.getType(), factory);
    }

    /**
     * Registers a widget factory under an explicit type identifier.
     * 
     * @param type the type identifier.
     * @param factory the factory to register.
     */
    void registerWidgetFactory(String type, SweetJuiceWidgetFactory factory) {
        widgetFactories.put(type, factory);
    }

    /**
     * Retrieves a registered widget factory by type.
     * 
     * @param type the type identifier.
     * @return the factory instance, or null if not found.
     */
    SweetJuiceWidgetFactory get(String type) {
        return widgetFactories.get(type);
    }
}
