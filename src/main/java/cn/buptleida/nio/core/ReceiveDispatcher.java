package cn.buptleida.nio.core;

import java.io.Closeable;

/**
 *接收数据的调度封装
 * 把一份或者多份IOArgs组合成一份Packet
 */
public interface ReceiveDispatcher extends Closeable {

    void start();

    void stop();

    interface ReceivePacketCallback{
        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
