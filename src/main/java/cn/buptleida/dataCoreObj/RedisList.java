package cn.buptleida.dataCoreObj;

import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisEnc;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.underObj.List;
import cn.buptleida.dataCoreObj.underObj.SDS;
import cn.buptleida.dataCoreObj.underObj.ZipList;
import sun.plugin.javascript.navig.Link;

public class RedisList extends RedisObject {
    public RedisList(){
        this.type = RedisType.LIST.VAL();
        this.encoding = RedisEnc.ZIPLIST.VAL();
        this.ptr = new ZipList();
    }

    public void leftPush(long val){
        if(encoding == RedisEnc.ZIPLIST.VAL()){
            ZipList zipList = (ZipList) ptr;
            zipList.push(val,0);
        }
        List<SDS> linkedList = (List<SDS>) ptr;

    }
    public void leftPush(String val){

    }
    public void rightPush(long val){

    }
    public void rightPush(String val){

    }
}
