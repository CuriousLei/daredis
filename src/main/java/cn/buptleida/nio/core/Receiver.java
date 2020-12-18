package cn.buptleida.nio.core;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver extends Closeable {
    void setReceiveListener(ioArgs.IoArgsEventProcessor processor);
    boolean postReceiveAsync() throws IOException;
}
