package cn.buptleida.dataCoreObj.underObj;

import cn.buptleida.dataCoreObj.enumerate.IntEnc;
import cn.buptleida.dataCoreObj.base.RedisObj;
import cn.buptleida.dataCoreObj.enumerate.Status;
import cn.buptleida.dataCoreObj.enumerate.ZLNodeEnc;

import java.util.Arrays;

class zlentry {
    //前置節點長度
    int prevrawlen;
    //記錄前置節點長度的字節數
    int prevrawlensize;

    //编码值
    int encoding;
    //編碼encoding的字节数
    int encodingSize;

    //頭部長度
    int headerSize;

    //content的起始位置
    int contentPos;
    //content的字節數
    int contentSize;
}

public class ZipList implements RedisObj {

    private byte[] elementData;


    public ZipList() {
        elementData = new byte[11];
        writeInt(elementData, 11, 0, IntEnc.INT_32.LEN());
        writeInt(elementData, 9, 4, IntEnc.INT_32.LEN());//zlTail初始设置为9
        writeInt(elementData, 0, 8, IntEnc.INT_16.LEN());
        writeInt(elementData, 255, 10, IntEnc.INT_8.LEN());
        elementData[10] = (byte) 255;
    }

    /**
     * 根据index获取结点；
     */
    public zlentry getEntryByIndex(int index) {
        int len = zlLen();
        if (index < 0 || index >= len) return null;

        int pos = zlTail();
        int preLen = readPreLen(pos);
        for (int i = 0; i < len - index - 1; ++i) {
            preLen = readPreLen(pos -= preLen);
        }
        return getEntry(pos);
    }

    /**
     * 插入整型数值；
     * 将节点压入表头或者表尾；
     * where是0代表表头，1代表表尾
     */
    public int push(long val, int where) {
        long encoding = ZLNodeEnc.getEncoding_Int(val);

        int contentSize = ZLNodeEnc.getConLen(encoding);
        byte[] valArr = new byte[contentSize];
        writeInt(valArr,val,0,contentSize);

        return pushEntry(valArr, where, encoding);
    }

    /**
     * 插入字节数组；
     * 将节点压入表头或者表尾；
     * where是0代表表头，1代表表尾
     */
    public int push(byte[] byteArr, int where) {
        long encoding = ZLNodeEnc.getEncoding_ByteArr(byteArr.length);
        return pushEntry(byteArr, where, encoding);
    }

    private int pushEntry(byte[] content, int where, long encoding) {

        if (where == 1) {
            return insertAfter(zlTail(), content, encoding);
        }

        byte[] entry = packEntry(0, content, encoding);
        insert(10, entry);
        return Status.SUCCESS;
    }

    /**
     * 插入整型结点；
     * 将新结点插入到index的位置
     */
    public int insertAt(int index, long val) {
        long encoding = ZLNodeEnc.getEncoding_Int(val);

        int contentSize = ZLNodeEnc.getConLen(encoding);
        byte[] valArr = new byte[contentSize];
        writeInt(valArr,val,0,contentSize);

        return insertAtIndex(index, valArr, encoding);
    }

    /**
     * 插入字节数组结点；
     * 将新结点插入到index的位置
     */
    public int insertAt(int index, byte[] val) {
        long encoding = ZLNodeEnc.getEncoding_ByteArr(val.length);

        return insertAtIndex(index, val, encoding);
    }

    private int insertAtIndex(int index, byte[] content, long encoding) {
        int len = zlLen();
        if (index < 0 || index > len) return Status.ERROR;

        if (index == 0) return pushEntry(content, 0, encoding);
        index--;

        int pos = zlTail();
        int preLen = readPreLen(pos);
        for (int i = 0; i < len - index - 1; ++i) {
            preLen = readPreLen(pos -= preLen);
        }

        return insertAfter(pos, content, encoding);
    }

    /**
     * 将新结点插入到pos指向结点的后置位
     */
    private int insertAfter(int pos, byte[] val, long encoding) {
        int len = zlLen();
        if (len == 0) return Status.ERROR;

        int bytesLen = zlBytes();
        //pos不合法
        if (pos < 10 || pos >= bytesLen - 1) return Status.ERROR;

        //插入结点的前置结点信息
        zlentry desEntry = getEntry(pos);
        int desEntryLen = desEntry.headerSize + desEntry.contentSize;
        int desPos = pos + desEntryLen;

        byte[] entry = packEntry(desEntryLen, val, encoding);
        insert(desPos, entry);

        return Status.SUCCESS;
    }

    /**
     * 给定插入位置，向element中插入节点
     */
    private void insert(int pos, byte[] entry) {
        copyInto(pos, entry);

        int bytesLen = elementData.length;
        int zlTail = zlTail();
        int zlLen = zlLen();
        int newEntryLen = entry.length;

        //更新压缩列表首部两个字段，zlTail和zlLen
        int newZlTail = pos > zlTail ? (bytesLen - 1 - newEntryLen) : (zlTail + newEntryLen);
        writeInt(elementData, newZlTail, 4, 4);
        writeInt(elementData, zlLen + 1, 8, 2);
        //writeInt(elementData, bytesLen + newEntryLen, 0, 4);

        chainUpdate(pos + newEntryLen, newEntryLen);
    }

    private void copyInto(int pos, byte[] byteArr){
        int zlLen = elementData.length;
        int newLen = byteArr.length;
        elementData = Arrays.copyOf(elementData, elementData.length + newLen);
        System.arraycopy(elementData, pos, elementData, pos + newLen, zlLen - pos);
        System.arraycopy(byteArr, 0, elementData, pos, newLen);

        //更新首部压缩列表长度字段
        writeInt(elementData, zlLen + newLen, 0, 4);
    }

    /**
     * 给定位置，在element中删除节点
     */
    private void delete(int pos){
        zlentry entry = getEntry(pos);
        int entryLen = entry.headerSize+entry.contentSize;
        delFrom(pos, pos + entryLen);

        int zlTail = zlTail();
        int zlLen = zlLen();

        //更新压缩列表首部两个字段，zlTail和zlLen
        int newZlTail = pos == zlTail ? (zlTail - entry.prevrawlen) : (zlTail - entryLen);
        writeInt(elementData, newZlTail, 4, 4);
        writeInt(elementData, zlLen - 1, 8, 2);
    }

    private void delFrom(int from, int to){
        int zlLen = elementData.length;
        int deleteLen = to -from;
        System.arraycopy(elementData, to, elementData, from, zlLen - to);
        elementData = Arrays.copyOf(elementData,zlLen-deleteLen);

        //更新首部压缩列表长度字段
        writeInt(elementData, zlLen - deleteLen, 0, 4);
    }
    /**
     * 连锁更新
     */
    private void chainUpdate(int pos, int newPreLen) {
        zlentry entry = getEntry(pos);

        if (entry == null) return;
        int entrySize = entry.headerSize+entry.contentSize;

        if(entry.prevrawlen<254){
            if(newPreLen<254) {
                writeInt(elementData, newPreLen, pos, 1);
            }
            else{
                writeInt(elementData, 254, pos, 1);
                byte[] newPreLenByteArr = new byte[4];
                writeInt(newPreLenByteArr,newPreLen,0,4);
                copyInto(pos, newPreLenByteArr);
                chainUpdate(pos + entrySize + 4, entrySize+4);
            }
        }else{
            if(newPreLen<254) {
                writeInt(elementData, newPreLen, pos, 1);
                delFrom(pos+1,pos+5);
                chainUpdate(pos + entrySize - 4, entrySize-4);
            }
            else{
                writeInt(elementData, newPreLen, pos+1, 4);
            }
        }

    }

    /**
     * 根据字节数组中的位置获取entry结点信息
     */
    private zlentry getEntry(int pos) {
        if (pos < 10) return null;//pos必须大于10
        int flagByte = (int) readInt(elementData, pos, 1);
        if (flagByte == 255) return null;//此时表示读取压缩列表末端标识位
        zlentry entry = new zlentry();
        if (flagByte == 254) {
            entry.prevrawlen = (int)readInt(elementData, pos + 1, 4);
            entry.prevrawlensize = 5;
            pos += 5;
        } else {
            entry.prevrawlen = (int)readInt(elementData, pos, 1);
            entry.prevrawlensize = 1;
            pos += 1;
        }
        int encByte = (int) readInt(elementData, pos, 1);
        if (encByte >= 192) {
            entry.encodingSize = 1;
            entry.encoding = encByte;
            entry.contentSize = ZLNodeEnc.getConLen(encByte);
            pos += 1;
        } else if (encByte < 64) {
            entry.encodingSize = 1;
            entry.contentSize = entry.encoding = (int)readInt(elementData, pos, 1);

            pos += 1;
        } else if (encByte < 128) {
            entry.encodingSize = 2;
            entry.encoding = (int)readInt(elementData, pos, 2);
            entry.contentSize = entry.encoding - 0b0100000000000000;
            pos += 2;
        } else {
            entry.encodingSize = 5;
            entry.contentSize = entry.encoding = (int)readInt(elementData, pos+1, 4);
            pos += 5;
        }
        entry.headerSize = entry.prevrawlensize + entry.encodingSize;
        entry.contentPos = pos;
        return entry;
    }

    /**
     * 根据pos获取content内容
     */
    public byte[] getContent(int pos) {
        zlentry entry = getEntry(pos);
        return Arrays.copyOfRange(elementData, entry.contentPos, entry.contentPos + entry.contentSize);
    }


    /**
     * 封装字节数组结点
     *
     * @param preEntryLen 记录前一个节点的长度
     * @param content     数据
     */
    private byte[] packEntry(int preEntryLen, byte[] content, long encoding) {
        int encSize = ZLNodeEnc.getEncSize(encoding);

        int preLenSize = preEntryLen < 254 ? 1 : 5;
        int contentSize = content.length;

        byte[] entry = new byte[encSize + preLenSize + contentSize];
        if (preLenSize == 5) {
            writeInt(entry, 254, 0, 1);
            writeInt(entry, preEntryLen, 1, 4);
        } else {
            writeInt(entry, preEntryLen, 0, 1);
        }
        writeInt(entry, encoding, preLenSize, encSize);

        System.arraycopy(content, 0, entry, preLenSize + encSize, contentSize);

        return entry;
    }


    /**
     * 给定位置，向byte数组写入一个整型数值；
     * 可以为int、short、byte
     *
     * @param val       待写入的数值
     * @param pos       写入的位置
     * @param byteCount 待写入的数值的字节数
     */
    private void writeInt(byte[] arr, long val, int pos, int byteCount) {
        if (pos + byteCount > elementData.length) return;

        for (int i = pos + byteCount - 1; i >= pos; --i) {
            arr[i] = (byte) (val & 255);
            val >>= 8;
        }
    }

    /**
     * 读取一个整型数值，都返回long;
     * int、short、byte（根据byteCount来决定，byteCount可为1,2,3,4,8）
     */
    private long readInt(byte[] arr, int pos, int byteCount) {
        long val = 0;
        for (int i = pos; i < byteCount + pos; i++) {
            val <<= 8;
            val += arr[i] & 255;
        }
        return val;
    }

    /**
     * 获取preLen字段的值
     */
    private int readPreLen(int pos) {
        long res = readInt(elementData, pos, 1);
        if (res == 254) {
            res = readInt(elementData, pos + 1, 4);
        }
        return (int) res;
    }

    public static boolean isIntVal(zlentry entry) {
        if (entry.encodingSize > 1) return false;
        if ((entry.encoding & 192) == 0) return false;

        return true;
    }

    private int zlBytes() {
        return (int) readInt(elementData, 0, IntEnc.INT_32.LEN());
    }

    private int zlTail() {
        return (int) readInt(elementData, 4, IntEnc.INT_32.LEN());
    }

    private int zlLen() {
        return (int) readInt(elementData, 8, IntEnc.INT_16.LEN());
    }

    //test
    public static void main(String[] args) {
        String str = "1111111";
        byte[] byteArr = str.getBytes();
        ZipList zipList = new ZipList();
        zipList.push(111, 0);
        zipList.push(8388603, 1);
        zipList.insertAt(2, 234567333123L);
        zipList.push(byteArr, 0);
        zipList.insertAt(2, byteArr);
        print(zipList);

        zipList.delete(31);
        zipList.delete(10);
        print(zipList);

        // printEntryInfo(zipList, zipList.getEntryByIndex(0));
        // printEntryInfo(zipList, zipList.getEntryByIndex(1));
        // printEntryInfo(zipList, zipList.getEntryByIndex(2));
    }

    public static void print(ZipList zipList) {
        int len = zipList.zlLen();
        System.out.println("zlBytes:" + zipList.zlBytes() + ",zlTail:" + zipList.zlTail() + ",zlLen:" + len);
        int pos = 10;
        for (int i = 0; i < len; ++i) {
            zlentry entry = zipList.getEntry(pos);
            if (ZipList.isIntVal(entry)) {
                long content = zipList.readInt(zipList.elementData, pos + entry.headerSize, entry.contentSize);
                System.out.println("pos: " + pos + ",preLen: " + entry.prevrawlen + ",encoding: " + entry.encoding + "," +
                        "content: " + content);
            } else {
                byte[] arr = Arrays.copyOfRange(zipList.elementData, entry.contentPos, entry.contentPos + entry.contentSize);
                System.out.println("pos: " + pos + ",preLen: " + entry.prevrawlen + ",encoding: " + entry.encoding + "," +
                        "content: " + Arrays.toString(arr));
            }

            pos += entry.headerSize + entry.contentSize;
        }
        System.out.println(zipList.readInt(zipList.elementData, pos, 1));
    }

    public static void printEntryInfo(ZipList zipList, zlentry entry) {
        if (ZipList.isIntVal(entry)) {
            long content = zipList.readInt(zipList.elementData, entry.contentPos, entry.contentSize);
            System.out.println(content);
        } else {
            byte[] arr = Arrays.copyOfRange(zipList.elementData, entry.contentPos, entry.contentPos + entry.contentSize);
            System.out.println(Arrays.toString(arr));
        }
    }
}
