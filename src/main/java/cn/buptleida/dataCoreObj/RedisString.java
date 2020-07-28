package cn.buptleida.dataCoreObj;

import cn.buptleida.dataCoreObj.base.RedisObj;
import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisEnc;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.underObj.LongInt;
import cn.buptleida.dataCoreObj.underObj.SDS;

import java.util.Arrays;

public class RedisString extends RedisObject {

    public RedisString(long val) {
        this.type = RedisType.STRING.VAL();
        this.encoding = RedisEnc.INT.VAL();
        this.ptr = new LongInt(val);
    }
    public RedisString(String str) {
        this.type = RedisType.STRING.VAL();
        this.encoding = RedisEnc.RAW.VAL();
        this.ptr = new SDS(str.toCharArray());
    }
    /**
     * 返回字符串值
     */
    public String get(){
        if(encoding == RedisEnc.INT.VAL()){
            LongInt temp = (LongInt) ptr;
            return String.valueOf(temp.getVal());
        }

        SDS temp = (SDS) ptr;
        return new String(temp.getArray());
    }
    /**
     * 拼接字符串
     */
    public int append(String extStr){
        if(encoding == RedisEnc.INT.VAL()){
            LongInt item = (LongInt) ptr;
            char[] newStr = Long.toString(item.getVal()).toCharArray();
            int dest = newStr.length;
            int extLen = extStr.length();
            newStr = Arrays.copyOf(newStr,newStr.length+extLen);
            System.arraycopy(extStr.toCharArray(),0,newStr,dest,extLen);

            encoding = RedisEnc.RAW.VAL();
            ptr = new SDS(newStr);
            return newStr.length;
        }
        SDS item = (SDS) ptr;
        SDS newSDS = item.append(extStr.toCharArray(),0,extStr.length());
        return newSDS.len();
    }
    /**
     * 加法计算
     */
    public Long incrby(){
        if(encoding != RedisEnc.INT.VAL()) return null;
        LongInt value = (LongInt) ptr;
        value.setVal(value.getVal() + 1);
        return value.getVal();
    }
    /**
     * 减法计算
     */
    public Long decrby(){
        if(encoding != RedisEnc.INT.VAL()) return null;
        LongInt value = (LongInt) ptr;
        value.setVal(value.getVal() - 1);
        return value.getVal();
    }
    /**
     * 返回字符串长度
     */
    public int strlen(){
        if(encoding == RedisEnc.INT.VAL()){
            LongInt item = (LongInt) ptr;
            return Long.toString(item.getVal()).length();
        }
        SDS item = (SDS) ptr;
        return item.len();
    }

    public static void main(String[] args) {
        RedisString redisString = new RedisString("wewewe");
        System.out.println(redisString.get());
        redisString.incrby();
        System.out.println(redisString.get());
        redisString.decrby();
        redisString.append("opopo");
        System.out.println(redisString.get());
        System.out.println(redisString.strlen());
    }
}
