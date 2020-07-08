package cn.buptleida.dataCoreObj.underObj;

import cn.buptleida.dataCoreObj.base.RedisObj;

import java.util.Arrays;

public class SDS implements RedisObj, Comparable<SDS> {
    public static final int SDS_MAX_PREALLOC = 1024 * 1024;

    //记录buf中已使用的数量
    private int len;

    private int free;

    private char buf[];

    public SDS(char[] init, int initLen) {
        newSds(init, initLen);
    }

    public SDS(char[] init) {
        newSds(init, init.length);
    }

    public SDS() {
        len = 0;
        free = 0;
        buf = new char[0];
    }

    /**
     * 新建一个SDS字符串
     */
    private void newSds(char[] init, int initLen) {
        len = initLen;
        buf = Arrays.copyOf(init, initLen);
        free = initLen > init.length ? (initLen - init.length) : 0;
    }

    /**
     * 对sds的buf空间进行扩展
     */
    private void makeRoom(int addLen) {
        if (free >= addLen) return;

        int newLen = len + addLen;
        if (newLen < SDS_MAX_PREALLOC) {
            newLen *= 2;
        } else {
            newLen += SDS_MAX_PREALLOC;
        }

        buf = Arrays.copyOf(buf, newLen);
    }

    /**
     * 拼接字符串到末尾
     */
    public SDS append(char[] str, int offset, int length) {
        if (len > 0) makeRoom(length);

        System.arraycopy(str, offset, buf, this.len, length);
        len += length;
        free = buf.length - len;
        return this;
    }

    /**
     * 将新字符串复制进sds中覆盖buf
     */
    public SDS copyFrom(char[] str, int offset, int length) {
        int totLen = free + len;
        if (length > totLen) makeRoom(length - len);

        System.arraycopy(str, offset, buf, 0, length);
        len = length;
        free = buf.length - len;
        return this;
    }

    /**
     * 与另一个sds字符串比较，是否相等
     */
    public boolean equals(SDS another) {
        if (len != another.len()) return false;

        char[] v1 = buf;
        char[] v2 = another.getBuf();
        int i = 0, n = len;
        while (n-- > 0) {
            if (v1[i] != v2[i])
                return false;
            i++;
        }
        return true;
    }

    public int len() {
        return len;
    }

    public int free() {
        return free;
    }

    public char[] getBuf() {
        return buf;
    }

    public char[] getArray() {
        return Arrays.copyOfRange(buf, 0, len);
    }

    public int hashCode() {
        int h = 0;
        char[] val = getArray();
        for (int i = 0; i < len; ++i) {
            h = 31 * h + val[i];
        }
        return h;
    }

    @Override
    public String toString() {
        return new String(getArray());
    }

    @Override
    public int compareTo(SDS anotherStr) {
        int len1 = len;
        int len2 = anotherStr.len();
        int lim = Math.min(len1, len2);
        char[] arr1 = buf;
        char[] arr2 = anotherStr.getBuf();

        for (int i = 0; i < lim; ++i) {
            char val1 = arr1[i];
            char val2 = arr2[i];
            if (val1 != val2) {
                return val1 - val2;
            }
        }
        return len1 - len2;
    }

    public static void main(String[] args) {

        SDS sds = new SDS(new char[]{'q', 'w', 'e'}, 2);
        sds.append(new char[]{'q', 'w', 'e'}, 0, 3);
        System.out.println(Arrays.toString(sds.buf));
        System.out.println(sds.len + " " + sds.free);
        sds.copyFrom(new char[]{'1', '2', '3', '4', '1', '2', '3', '4', '1', '2', '3', '4'}, 0, 12);
        System.out.println(Arrays.toString(sds.buf));

        SDS str1 = new SDS(new char[]{'1', '2', '3', '4', '1', '2', '3', '4', '1', '2', '3', '4'}, 12);
        SDS str2 = new SDS(new char[]{'1', '2', '3', '4', '1', '2', '3', '4', '1', '2', '3'}, 11);
        SDS str3 = new SDS(new char[]{'1', '2', '3', '4', '1', '2', '3', '4', '1', '2', '3', '0'}, 12);
        System.out.println(sds.equals(str1) + " " + sds.equals(str2) + " " + sds.equals(str3));

    }
}
