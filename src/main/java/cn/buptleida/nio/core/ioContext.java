package cn.buptleida.nio.core;

import cn.buptleida.nio.impl.ioSelectorProvider;

public class ioContext {
    private static ioProvider ioSelector;

    public static ioProvider getIoSelector() {
        return ioSelector;
    }

    public static void setIoSelector(ioProvider ioSelector) {
        ioContext.ioSelector = ioSelector;
    }

    public static void close(){
        ioSelector.close();
    }
}
