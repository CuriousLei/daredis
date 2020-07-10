package cn.buptleida.dataCoreObj.underObj;

import cn.buptleida.dataCoreObj.enumerate.IntEnc;
import cn.buptleida.dataCoreObj.base.RedisObj;
import cn.buptleida.dataCoreObj.enumerate.Status;

import java.util.Arrays;
import java.util.Random;

public class IntSet implements RedisObj {

    private IntEnc encoding;
    private int length;
    private Number[] contents;

    // private Short[] shortContents;
    // private Integer[] intContents;
    // private Long[] longContents;

    public IntSet() {

        this.encoding = IntEnc.INT_16;
        this.length = 0;
        contents = new Short[0];
    }


    public void add(long val) {
        IntEnc curEnc = valueEncoding(val);//获取新值需要的编码方式
        if (curEnc.LEN() > encoding.LEN()) {
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

    public int remove(long val) {
        int index;
        if ((index = getIndex(val)) < 0)
            return Status.ERROR;
        System.arraycopy(contents, index + 1, contents, index, length - index - 1);
        contents = Arrays.copyOf(contents, length - 1);
        length--;
        return Status.SUCCESS;
    }


    /**
     * 判断元素是否存在，存在返回true，否则false
     */
    public boolean containsVal(long val) {
        float t = binarySearch(val);
        return Math.ceil(t) == t;
    }

    /**
     * 根据index返回对应的元素
     * @param index
     * @return
     */
    public long get(int index) {
        rangeCheck(index);

        return contents[index].longValue();
    }

    /**
     * 随机返回列表中一个元素
     * @return
     */
    public long getRandom(){
        Random rand = new Random();
        int index = rand.nextInt(length);

        return get(index);
    }

    /**
     * 获取元素的下标，如果不存在返回-1
     */
    public int getIndex(long val) {
        float t = binarySearch(val);
        return (int) (t + 0.5) == t ? (int) t : -1;
    }

    /**
     * 判断是否包含某整数
     * @param val
     * @return 若存在返回true，不存在返回false
     */
    public boolean exist(long val){
        if(getIndex(val)==-1)
            return false;
        return true;
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
        if (encoding == IntEnc.INT_16) {
            contents[pos] = (short) val;
        } else if (encoding == IntEnc.INT_32) {
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
    private void upgradeAndAdd(IntEnc enc, long val) {
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
            case INT_16:
                contents[pos] = (short) val;
                break;
            case INT_32:
                contents[pos] = (int) val;
                break;
            case INT_64:
                contents[pos] = val;
                break;
        }
    }

    /**
     * 根据编码新建一个数组
     */
    private Number[] newArray(IntEnc enc, int len) {
        switch (enc) {
            case INT_16:
                return new Short[len];
            case INT_32:
                return new Integer[len];
            case INT_64:
                return new Long[len];
        }
        return null;
    }

    /**
     * 返回适合该val值的编码
     */
    private IntEnc valueEncoding(long val) {
        if (val < IntEnc.INT_32.MIN() || val > IntEnc.INT_32.MAX()) {
            return IntEnc.INT_64;
        } else if (val < IntEnc.INT_16.MIN() || val > IntEnc.INT_16.MAX()) {
            return IntEnc.INT_32;
        } else {
            return IntEnc.INT_16;
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
        return 8 + 4 + 4 + 4 + 4 + length * encoding.LEN();
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

