package cn.buptleida.dataCoreObj;

import java.util.ArrayList;
import java.util.Arrays;

public class IntSet {

    private Enc encoding;
    private int length;
    private Number[] contents;

    // private Short[] shortContents;
    // private Integer[] intContents;
    // private Long[] longContents;

    IntSet() {

        this.encoding = Enc.SHORT;
        this.length = 0;
        contents = new Short[0];
    }


    public void add(long val) {
        Enc curEnc = valueEncoding(val);//获取新值需要的编码方式
        if (curEnc.VAL() > encoding.VAL()) {
            encoding = curEnc;//将编码方式修改为新编码
            upgradeAndAdd(curEnc, val);
            return;
        }
        //运行到这里，表示当前编码适合新元素存放
        //查找插入位置，可能已存在
        int pos;
        if ((pos = getInsertIndex(val)) >= 0)
            insert(val, pos);

    }

    public void remove(long val) {
        int index;
        if ((index = getIndex(val)) > -1) {
            System.arraycopy(contents, index + 1, contents, index, length - index - 1);
            contents = Arrays.copyOf(contents, length - 1);
            length--;
        }
    }


    /**
     * 判断元素是否存在，存在返回true，否则false
     */
    public boolean containsVal(long val) {
        float t = binarySearch(val);
        return Math.ceil(t) == t;
    }

    public long get(int index) {
        rangeCheck(index);

        return (long) contents[index];
    }

    /**
     * 获取元素的下标，如果不存在返回-1
     */
    public int getIndex(long val) {
        float t = binarySearch(val);
        return (int) (t + 0.5) == t ? (int) t : -1;
    }

    /**
     * 获取元素的插入位置，如果元素已存在返回-1
     */
    private int getInsertIndex(long val) {
        float t = binarySearch(val);
        return (int) (t + 0.5) == t ? -1 : (int) (t + 0.5);
    }

    /**
     * 二分查找
     */
    private float binarySearch(long val) {
        int low = 0, high = length - 1;
        while (high >= low) {
            int mid = low + (high - low) / 2;
            long temp = contents[mid].longValue();
            if (temp > val) {
                high = mid - 1;
            } else if (temp < val) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return (float) (low - 0.5);
    }

    /**
     * 将val插入数组指定位置pos
     */
    private void insert(long val, int pos) {
        contents = Arrays.copyOf(contents, length + 1);
        System.arraycopy(contents, pos, contents, pos + 1, length - pos);
        if (encoding == Enc.SHORT) {
            contents[pos] = (short) val;
        } else if (encoding == Enc.INT) {
            contents[pos] = (int) val;
        } else {
            contents[pos] = val;
        }
        length++;
    }


    /**
     * 更换数组编码，同时插入val；
     * 不需要判断val插入位置，因为不是在首部就是在尾部
     */
    private void upgradeAndAdd(Enc enc, long val) {
        Number[] tempArr = contents;
        contents = newArray(enc, length + 1);
        int prepend = val < 0 ? 1 : 0;
        for (int i = 0; i < length; ++i) {
            setVal(i + prepend, tempArr[i].longValue());
        }
        length++;
        setVal((prepend + length - 1) % length, val);
    }

    /**
     * 按照一定编码，将数值写入数组（此时数组已经扩容完毕）
     */
    private void setVal(int pos, long val) {
        switch (encoding) {
            case SHORT:
                contents[pos] = (short) val;
                break;
            case INT:
                contents[pos] = (int) val;
                break;
            case LONG:
                contents[pos] = val;
                break;
        }
    }

    /**
     * 根据编码新建一个数组
     */
    private Number[] newArray(Enc enc, int len) {
        switch (enc) {
            case SHORT:
                return new Short[len];
            case INT:
                return new Integer[len];
            case LONG:
                return new Long[len];
        }
        return null;
    }

    /**
     * 返回适合该val值的编码
     */
    private Enc valueEncoding(long val) {
        if (val < Enc.INT.MIN() || val > Enc.INT.MAX()) {
            return Enc.LONG;
        } else if (val < Enc.SHORT.MIN() || val > Enc.SHORT.MAX()) {
            return Enc.INT;
        } else {
            return Enc.SHORT;
        }
    }

    /**
     * 边界检查
     *
     * @param index
     */
    private void rangeCheck(int index) {
        if (index < 0 || index > length - 1) {
            throw new IndexOutOfBoundsException(outOfBoundMsg(index));
        }
    }

    private String outOfBoundMsg(int index) {
        return "Index: " + index + ", Size: " + length;
    }

    public int length() {
        return length;
    }

    public int blobLen() {
        return 8 + 4 + 4 + 4 + 4 + length * encoding.VAL();
    }

    public Number[] getContents() {
        return contents;
    }

    public static void main(String[] args) {
        IntSet intSet = new IntSet();

        intSet.add(8);
        intSet.add(5);
        System.out.println(Arrays.toString(intSet.getContents()));
        System.out.println(intSet.getInsertIndex(10));
        intSet.add(10);
        System.out.println(Arrays.toString(intSet.getContents()));
        intSet.add(9);
        System.out.println(Arrays.toString(intSet.getContents()));
        intSet.add(65536);
        // System.out.println(intSet.contents[2].getClass());
        // System.out.println(intSet.containsVal(10));
        // System.out.println(intSet.containsVal(11));
        // System.out.println(intSet.getIndex(11));
        // System.out.println(intSet.getInsertIndex(11));
        //
        // System.out.println(intSet.getIndex(8));
        // System.out.println(intSet.getInsertIndex(8));
        // System.out.println(intSet.contents[0]);
        System.out.println(Arrays.toString(intSet.getContents()));
        intSet.remove(9);
        System.out.println(Arrays.toString(intSet.getContents()));
        intSet.remove(5);
        System.out.println(Arrays.toString(intSet.getContents()));
        intSet.remove(65536);
        System.out.println(Arrays.toString(intSet.getContents()));

    }
}

enum Enc {
    SHORT(2, -32768, 32767),
    INT(4, 0x80000000, 0x7fffffff),
    LONG(8, 0x8000000000000000L, 0x7fffffffffffffffL);
    private final byte VAL;
    private final long MIN;
    private final long MAX;

    public byte VAL() {
        return VAL;
    }

    public long MIN() {
        return MIN;
    }

    public long MAX() {
        return MAX;
    }

    Enc(int val, long min, long max) {
        this.VAL = (byte) val;
        this.MIN = min;
        this.MAX = max;
    }

}