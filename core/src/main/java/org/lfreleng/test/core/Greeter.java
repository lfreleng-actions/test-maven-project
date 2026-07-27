/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: 2026 The Linux Foundation
 */

package org.lfreleng.test.core;

/**
 * Builds greeting messages.
 *
 * <p>Deliberately small, but with more than one branch so coverage
 * reports contain something meaningful to measure.
 */
public final class Greeter {

    private static final String DEFAULT_NAME = "world";

    private final String name;

    /**
     * Creates a greeter.
     *
     * @param name the name to greet; blank or null falls back to a default
     */
    public Greeter(final String name) {
        if (name == null || name.isBlank()) {
            this.name = DEFAULT_NAME;
        } else {
            this.name = name.strip();
        }
    }

    /**
     * Returns the greeting for the configured name.
     *
     * @return the greeting message
     */
    public String greet() {
        return "Hello, " + name + "!";
    }

    /**
     * Returns the name this greeter addresses.
     *
     * @return the resolved name
     */
    public String name() {
        return name;
    }
}
