package cn.buptleida.structure.enumerate;

public enum IntEnc {
    INT_8(1, -128, 127),
    INT_16(2, -32768, 32767),
    INT_24(3, -8388608, 8388607),
    INT_32(4, 0x80000000, 0x7fffffff),
    INT_64(8, 0x8000000000000000L, 0x7fffffffffffffffL);
    private final int LEN;
    private final long MIN;
    private final long MAX;

    IntEnc(int len, long min, long max) {
        this.LEN = len;
        this.MAX = max;
        this.MIN = min;
    }

    public int LEN() {
        return LEN;
    }

    public long MIN() {
        return MIN;
    }

    public long MAX() {
        return MAX;
    }
}
