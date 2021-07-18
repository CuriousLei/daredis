package cn.buptleida.nio.box;

import cn.buptleida.dataCoreObj.underObj.ZipList;
import cn.buptleida.dataCoreObj.underObj.zlentry;
import cn.buptleida.nio.core.ReceivePacket;
import com.sun.xml.internal.fastinfoset.util.StringArray;

import javax.transaction.xa.Xid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StringArrayReceivePacket extends ReceivePacket<ByteArrayOutputStream> {
    private String[] strArr;

    public StringArrayReceivePacket(int len) {
        length = len;
    }

    public String toString() {
        return String.join(" ", strArr);
    }

    public String[] getStrArr() {
        return strArr;
    }

    @Override
    protected void closeStream(ByteArrayOutputStream stream) throws IOException {
        super.closeStream(stream);
        // 通过stream得到字节数组以得到压缩列表，遍历压缩列表得到strArr
        ZipList zipList = new ZipList(stream.toByteArray());
        int len = zipList.zlLen();
        strArr = new String[len];
        int pos = 0;
        zlentry entry = zipList.getEntry(10);
        while (entry != null) {
            strArr[pos++] = new String(zipList.getNodeVal_ByteArr(entry), StandardCharsets.UTF_16BE);
            entry = zipList.getNextEntry(entry);
        }
    }

    @Override
    protected ByteArrayOutputStream createStream() {
        return new ByteArrayOutputStream((int) length);
    }

}
