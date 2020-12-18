package cn.buptleida.nio.core;

import cn.buptleida.nio.impl.ioSelectorProvider;

public class ioContext {
    private static ioSelectorProvider ioSelector;

    public static ioSelectorProvider getIoSelector() {
        return ioSelector;
    }

    public static void setIoSelector(ioSelectorProvider ioSelector) {
        ioContext.ioSelector = ioSelector;
    }

    public static void close(){
        ioSelector.close();
    }
}
