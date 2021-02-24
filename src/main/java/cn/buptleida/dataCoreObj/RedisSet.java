package cn.buptleida.dataCoreObj;

import cn.buptleida.dataCoreObj.base.CmdExecutor;
import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisEnc;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.enumerate.Status;
import cn.buptleida.dataCoreObj.underObj.Dict;
import cn.buptleida.dataCoreObj.underObj.IntSet;
import cn.buptleida.dataCoreObj.underObj.SDS;

import java.util.Arrays;

public class RedisSet extends RedisObject implements CmdExecutor {

    public RedisSet(){
        this.type = RedisType.SET.VAL();
        this.encoding = RedisEnc.INTSET.VAL();
        this.ptr = new IntSet();
    }

    /**
     * 向集合中添加元素
     * @param val
     */
    public void sAdd(long val){
        if(encoding == RedisEnc.INTSET.VAL()){
            IntSet intSet = (IntSet) ptr;
            intSet.add(val);
            checkVary();
        }else{
            Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
            SDS key = new SDS(Long.toString(val).toCharArray());
            dict.put(key,null);
        }
    }

    /**
     * 向集合中添加元素
     * @param val
     */
    public void sAdd(String val){
        if(encoding == RedisEnc.INTSET.VAL()){
            intSet2Dict();
        }

        Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
        SDS key = new SDS(val.toCharArray());
        dict.put(key,null);
    }

    /**
     * 检查是否要进行编码转换
     */
    private void checkVary(){
        if(encoding == RedisEnc.INTSET.VAL()){
            IntSet intSet = (IntSet) ptr;

            int len = intSet.length();
            if(len > 512){
                intSet2Dict();
            }
        }
    }

    /**
     * 进行编码转换，整数列表转化为字典
     * @return 返回转化好的ptr
     */
    private void intSet2Dict(){
        Dict<SDS,SDS> dict = new Dict<>();
        IntSet intSet = (IntSet) ptr;
        for (Number item : intSet.getContents()){
            SDS key = new SDS(item.toString().toCharArray());
            dict.put(key,null);
        }

        encoding = RedisEnc.HT.VAL();
        ptr = dict;
    }

    /**
     * 获取集合中的元素数量
     * @return
     */
    public int sCard(){
        if(encoding == RedisEnc.INTSET.VAL()){
            IntSet intSet = (IntSet) ptr;
            return intSet.length();
        }

        Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
        return dict.dictSize();
    }

    /**
     * 判断元素是否存在于集合中
     * @param val
     * @return
     */
    public boolean sIsMember(long val){
        if(encoding == RedisEnc.INTSET.VAL()){
            IntSet intSet = (IntSet) ptr;
            return intSet.exist(val);
        }
        SDS key = new SDS(Long.toString(val).toCharArray());

        Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
        return dict.exist(key);
    }
    public boolean sIsMember(String val){
        if(encoding == RedisEnc.INTSET.VAL())
            return false;
        SDS key = new SDS(val.toCharArray());

        Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
        return dict.exist(key);
    }

    /**
     * 随机返回集合中一个元素
     * @return
     */
    public String sRandMember(){
        if(encoding == RedisEnc.INTSET.VAL()){
            IntSet intSet = (IntSet) ptr;
            return Long.toString(intSet.getRandom());
        }

        Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
        return dict.dictGetRandomKey().toString();
    }

    /**
     * 随机返回集合中一个元素，并将其删除
     * @return
     */
    public String sPop(){
        String res;
        if(encoding == RedisEnc.INTSET.VAL()){
            IntSet intSet = (IntSet) ptr;
            long val = intSet.getRandom();
            res = Long.toString(val);
            intSet.remove(val);
        }else{
            Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
            SDS key = dict.dictGetRandomKey();
            res = key.toString();
            dict.delete(key);
        }
        return res;
    }

    /**
     * 删除给定元素
     * @param val
     * @return
     */
    public int sRem(long val){
        if(encoding == RedisEnc.INTSET.VAL()){
            IntSet intSet = (IntSet) ptr;
            return intSet.remove(val);
        }else{
            Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
            SDS key = new SDS(Long.toString(val).toCharArray());
            if(dict.delete(key)==null)
                return Status.ERROR;
            return Status.SUCCESS;
        }
    }
    public int sRem(String val){
        if(encoding == RedisEnc.INTSET.VAL())
            return Status.ERROR;
        Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
        SDS key = new SDS(val.toCharArray());
        if(dict.delete(key)==null)
            return Status.ERROR;
        return Status.SUCCESS;
    }
}
