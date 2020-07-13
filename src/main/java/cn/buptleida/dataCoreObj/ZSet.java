package cn.buptleida.dataCoreObj;

import cn.buptleida.dataCoreObj.base.RedisObj;
import cn.buptleida.dataCoreObj.underObj.Dict;
import cn.buptleida.dataCoreObj.underObj.SkipList;

class ZSet<T extends Comparable<? super T>> implements RedisObj {
    SkipList<T> zsl;
    Dict<T,Double> dict;
    ZSet(){
        this.dict = new Dict<>();
        this.zsl = new SkipList<>();
    }
}
