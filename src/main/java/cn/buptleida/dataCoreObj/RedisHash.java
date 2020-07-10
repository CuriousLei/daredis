package cn.buptleida.dataCoreObj;

import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisEnc;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.enumerate.Status;
import cn.buptleida.dataCoreObj.underObj.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;


public class RedisHash extends RedisObject {
    public RedisHash(){
        this.type = RedisType.HASH.VAL();
        this.encoding = RedisEnc.ZIPLIST.VAL();
        this.ptr = new ZipList();
    }

    /**
     * 插入键值对
     * @param key 全当字符串来处理
     * @param val 可能是字符串，可能是整型数字
     */
    public void hSet(String key, String val){
        if(encoding == RedisEnc.ZIPLIST.VAL()){
            ZipList zipList = (ZipList) ptr;
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_16BE);
            byte[] valBytes = val.getBytes(StandardCharsets.UTF_16BE);

            zlentry entry;
            int update=0;
            //如果key已存在，则更新对应的val
            if((entry = zipList.exist(keyBytes,1))!=null){
                int valPos = entry.endPos();
                zipList.delete(valPos);
                zipList.insertAfter(entry, valBytes);
                update = 1;
            }
            //没有执行更新操作，则进行push
            if(update == 0){
                zipList.push(keyBytes,1);
                zipList.push(valBytes,1);
            }

            checkVary(Math.max(keyBytes.length, valBytes.length));
        }else{
            Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
            SDS keySDS = new SDS(key.toCharArray());
            SDS valSDS = new SDS(val.toCharArray());
            dict.put(keySDS, valSDS);
        }
    }

    public void hSet(String key, long val){
        if(encoding == RedisEnc.ZIPLIST.VAL()){
            ZipList zipList = (ZipList) ptr;
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_16BE);

            zlentry entry;
            int update=0;
            //如果key已存在，则更新对应的val
            if((entry = zipList.exist(keyBytes,1))!=null){
                int valPos = entry.endPos();
                zipList.delete(valPos);
                zipList.insertAfter(entry, val);
                update = 1;
            }
            //没有执行更新操作，则进行push
            if(update == 0){
                zipList.push(keyBytes,1);
                zipList.push(val,1);
            }

            checkVary(keyBytes.length);
        }else{
            Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
            SDS keySDS = new SDS(key.toCharArray());
            SDS valSDS = new SDS(Long.toString(val).toCharArray());
            dict.put(keySDS, valSDS);
        }
    }
    /**
     * 根据键查找值
     */
    public String hGet(String key){
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;
            zlentry keyEntry;
            if((keyEntry = zipList.exist(key.getBytes(StandardCharsets.UTF_16BE),1))==null)
                return null;
            zlentry valEntry = zipList.getNextEntry(keyEntry);

            //判断是整数结点还是字节数组结点
            if(ZipList.isIntVal(valEntry)){
                long val = zipList.getNodeVal_Int(valEntry);
                return Long.toString(val);
            }else{
                byte[] val = zipList.getNodeVal_ByteArr(valEntry);
                return new String(val,StandardCharsets.UTF_16BE);
            }

        }else{
            Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
            SDS keySDS = new SDS(key.toCharArray());
            SDS valSDS = dict.get(keySDS);
            return valSDS.toString();
        }
    }
    /**
     * 输入给定键；
     * 查找是否存在该键值对。
     */
    public boolean hExists(String key){
        if (encoding == RedisEnc.ZIPLIST.VAL()){
            ZipList zipList = (ZipList) ptr;
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_16BE);
            if(zipList.exist(keyBytes,1)==null)
                return false;
            return true;
        }

        Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
        SDS keySDS = new SDS(key.toCharArray());
        return dict.exist(keySDS);
    }
    /**
     * 根据给定键删除对应的键值对
     * 输入：key键；
     * 输出：成功返回1，因键值对不存在删除失败返回0
     */
    public int hDel(String key){
        if (encoding == RedisEnc.ZIPLIST.VAL()){
            ZipList zipList = (ZipList) ptr;
            byte[] keyBytes = key.getBytes(StandardCharsets.UTF_16BE);
            zlentry keyEntry;
            if((keyEntry=zipList.exist(keyBytes,1))==null)
                return Status.ERROR;
            zlentry valEntry = zipList.getNextEntry(keyEntry);
            zipList.delete(valEntry);
            zipList.delete(keyEntry);
            return Status.SUCCESS;
        }else{
            Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
            SDS keySDS = new SDS(key.toCharArray());

            if(dict.delete(keySDS)==null)
                return Status.ERROR;
            return Status.SUCCESS;
        }
    }

    public int hLen(){
        if (encoding == RedisEnc.ZIPLIST.VAL()){
            ZipList zipList = (ZipList) ptr;
            return zipList.zlLen()/2;
        }else{
            Dict<SDS,SDS> dict = (Dict<SDS,SDS>) ptr;
            return dict.dictSize();
        }
    }
    /**
     * 传参elementSize：当前添加的结点的长度；
     * 检查是否满足转换条件
     * 进行zipList和linkedList之间的转换;
     * 只在，添加元素且当前是zipList，删除元素且当前是linkList，这两种情况下调用
     */
    private void checkVary(int elementSize) {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList list = (ZipList) ptr;
            int len = list.zlLen();
            if (len > 1024 || elementSize >= 64) {
                zipList2Dict();
            }
        }
    }

    private void zipList2Dict(){
        Dict<SDS,SDS> dict = new Dict<>();
        ZipList zipList = (ZipList) ptr;
        int len = zipList.zlLen()/2;
        for (int i = 0, pos = 10; i < len; ++i) {
            zlentry keyEntry = zipList.getEntry(pos);
            zlentry valEntry = zipList.getNextEntry(keyEntry);

            String s = new String(zipList.getNodeVal_ByteArr(keyEntry),StandardCharsets.UTF_16BE);
            SDS keySDS = new SDS(s.toCharArray());
            SDS valSDS;
            //判断结点是整型还是字节数组
            if (ZipList.isIntVal(valEntry)) {
                long val = zipList.getNodeVal_Int(valEntry);
                char[] charArr = Long.toString(val).toCharArray();
                valSDS = new SDS(charArr);
            } else {
                byte[] byteArr = zipList.getNodeVal_ByteArr(valEntry);
                String str = new String(byteArr, StandardCharsets.UTF_16BE);
                valSDS = new SDS(str.toCharArray());
            }
            dict.put(keySDS, valSDS);

            pos = valEntry.endPos();
        }

        encoding = RedisEnc.HT.VAL();
        ptr = dict;
    }

    public static void main(String[] args) {
        RedisHash hash = new RedisHash();
        testDict(hash);
        // hash.hSet("name","leida");
        // hash.hSet("age",23);
        // hash.hSet("level",15);
        // printAllItem(hash);
        //
        // hash.hSet("name","qwertyuiopasdfghjklzxcvbnm1234567");
        // printAllItem(hash);
    }
    private static void printAllItem(RedisHash hash) {
        if (hash.encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) hash.ptr;
            ZipList.print(zipList);
        } else {
            Dict<SDS,SDS> dict = (Dict<SDS,SDS>) hash.ptr;
            Dict.printDictInfo(dict);
        }
    }
    private static void testDict(RedisHash hash){
        for(int i=0;i<512;++i){
            hash.hSet(Integer.toString(i),"leida"+i);
        }
        System.out.println(hash.hGet("56"));
        System.out.println(hash.hGet("86"));
        hash.hDel("86");
        hash.hDel("100");
        System.out.println(hash.hGet("86"));
        System.out.println(hash.hLen());
        hash.hSet("56","qiqiqiqiqi");
        System.out.println(hash.hGet("56"));
        System.out.println(hash.hExists("56"));
        //printAllItem(hash);
    }
}
