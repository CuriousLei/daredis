package cn.buptleida.dataCoreObj;

import cn.buptleida.dataCoreObj.base.CmdExecutor;
import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisEnc;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.underObj.LongInt;
import cn.buptleida.dataCoreObj.underObj.SDS;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RedisString extends RedisObject  implements CmdExecutor {

    public RedisString(Long val) {
        this.type = RedisType.STRING.VAL();
        this.encoding = RedisEnc.INT.VAL();
        this.ptr = new LongInt(val);
    }
    public RedisString(String str) {
        if(!isNumeric(str)){
            this.type = RedisType.STRING.VAL();
            this.encoding = RedisEnc.RAW.VAL();
            this.ptr = new SDS(str.toCharArray());
        }else{
            this.type = RedisType.STRING.VAL();
            this.encoding = RedisEnc.INT.VAL();
            this.ptr = new LongInt(Long.parseLong(str));
        }

    }

    /**
     * 判断字符串是否能转换为整型
     * @param str
     * @return
     */
    private boolean isNumeric(String str){
        if(str.length()>18) return false;
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if(isNum.matches())
            return true;
        return false;
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
}
