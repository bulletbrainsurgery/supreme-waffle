/*
 * Decompiled with CFR 0.150.
 */
package com.socket;

public enum SocketLog {
    INFO("<col=008000>"),
    ERROR("<col=b4281e>");

    private String prefix;

    SocketLog(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }
}

