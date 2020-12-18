package cn.buptleida.nio.box;

import cn.buptleida.nio.core.SendPacket;

import java.io.ByteArrayInputStream;

public class StringSendPacket extends SendPacket<ByteArrayInputStream> {
    private final byte[] bytes;

    public StringSendPacket(String msg) {
        this.bytes = msg.getBytes();
        this.length = bytes.length;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }
}
