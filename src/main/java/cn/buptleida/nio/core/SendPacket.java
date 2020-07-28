package cn.buptleida.nio.core;

import java.io.InputStream;

/**
 * 发送包的定义
 */
public abstract class SendPacket<T extends InputStream> extends Packet<T> {
    //标记是否已结束
    private boolean isCanceled;

    //public abstract byte[] bytes();

    public boolean isCanceled() {
        return isCanceled;
    }


}
