package cn.buptleida.nio.box;

import cn.buptleida.dataCoreObj.underObj.ZipList;
import cn.buptleida.nio.core.SendPacket;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class StringArraySendPacket extends SendPacket<ByteArrayInputStream> {
    private final byte[] bytes;

    public StringArraySendPacket(String[] msg) {
        // 将string数组转化为压缩列表
        ZipList zipList = new ZipList();
        for (String str : msg) {
            zipList.push(str.getBytes(StandardCharsets.UTF_16BE), 1);
        }
        this.bytes = zipList.getElementData();
        this.length = bytes.length;
    }

    @Override
    protected ByteArrayInputStream createStream() {
        return new ByteArrayInputStream(bytes);
    }

}
