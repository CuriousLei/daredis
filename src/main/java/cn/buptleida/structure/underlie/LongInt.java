package cn.buptleida.structure.underlie;

import cn.buptleida.structure.base.RedisObj;

public class LongInt implements RedisObj {
    private long val;

    public LongInt(long val) {
        this.val = val;
    }

    public void setVal(long val) {
        this.val = val;
    }

    public long getVal() {
        return val;
    }
}
