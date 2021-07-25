package cn.buptleida.structure;

import cn.buptleida.structure.base.RedisObj;
import cn.buptleida.structure.underlie.Dict;
import cn.buptleida.structure.underlie.SkipList;

class ZSet<T extends Comparable<? super T>> implements RedisObj {
    SkipList<T> zsl;
    Dict<T,Double> dict;
    ZSet(){
        this.dict = new Dict<>();
        this.zsl = new SkipList<>();
    }
}
