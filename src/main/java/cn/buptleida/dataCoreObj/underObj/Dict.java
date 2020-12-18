package cn.buptleida.dataCoreObj.underObj;

import cn.buptleida.dataCoreObj.base.RedisObj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class Dict<K, V> implements DictHt.DictCallBack, RedisObj {
    static final int DICT_HT_INITIAL_SIZE = 1 << 2;

    static final int MAXIMUM_CAPACITY = 1 << 30;

    //两个哈希表
    DictHt<K, V> ht;
    DictHt<K, V> htBac;

    //记录渐进式rehash的位置
    private int treHashIdx;

    public Dict() {
        this.ht = new DictHt<>(this);
        this.htBac = new DictHt<>(this);
        treHashIdx = -1;
    }

    private boolean isRehashing() {
        return treHashIdx != -1;
    }

    public V put(K key, V value) {
        //如果ht大小为0，则需要一次初始化
        if (ht.getSize() == 0) {
            ht.table = new DictHt.Entry[DICT_HT_INITIAL_SIZE];
            ht.setSize(DICT_HT_INITIAL_SIZE);
        }
        //单步迁移
        dictRehash(1);
        //查看key是否已存在，若存在，直接修改
        DictHt.Entry<K, V> e;
        if ((e = dictContainsKey(key)) != null) {
            V oldVal = e.getValue();
            e.setValue(value);
            return oldVal;
        }
        //运行到此步，说明dict中不包含此key
        //判断是哪个ht，如果正在rehash，新节点都插入htBac中
        DictHt<K, V> dictHt = isRehashing() ? htBac : ht;
        //计算索引
        int hash = hash(key);
        int index = getIndex(hash, dictHt.getSize());

        //新建并插入节点
        dictHt.insertEntry(key, value, index);

        return null;
    }

    public V get(K key){
        //单步迁移
        dictRehash(1);
        DictHt.Entry<K, V> e;
        if((e=dictContainsKey(key))==null)
            return null;
        return e.getValue();
    }

    /**
     * 判断dict是否包含某个key，若包含则返回这个entry，不包含返回null
     *
     * @param key
     * @return
     */
    private DictHt.Entry<K, V> dictContainsKey(K key) {
        int hash = hash(key);
        DictHt.Entry<K, V> e;
        if ((e = ht.containsKey(key, getIndex(hash, ht.getSize()))) != null)
            return e;
        if(treHashIdx==-1)
            return null;
        if ((e = htBac.containsKey(key, getIndex(hash, htBac.getSize()))) != null)
            return e;
        return null;
    }

    /**
     * 判断是否存在某个键值对
     */
    public boolean exist(K key){
        if(dictContainsKey(key) == null)
            return false;
        return true;
    }

    private int hash(K key) {
        int h = 0;
        h ^= key.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        h ^= (h >>> 7) ^ (h >>> 4);
        return h;
    }

    private int getIndex(int hash, int length) {

        return hash & (length - 1);
    }

    private void add(K key, V value) {

    }

    public V replace(K key, V value) {
        return null;
    }
    public V delete(K key){
        dictRehash(1);

        DictHt.Entry<K, V> e;
        int hash = hash(key);
        if((e = ht.delete(key,getIndex(hash, ht.getSize())))!=null)
            return e.getValue();
        if(treHashIdx==-1)
            return null;
        if((e = htBac.delete(key,getIndex(hash, htBac.getSize())))!=null)
            return e.getValue();
        return null;
    }

    /**
     *
     * @return 字典中键值对数量
     */
    public int dictSize(){
        return ht.getUsed() + htBac.getUsed();
    }

    /**
     * 获得字典里随机一个结点
     * @return
     */
    public DictHt.Entry<K,V> dictGetRandomEntry(){
        DictHt.Entry<K,V> entry;
        Random rand = new Random();
        if(isRehashing()){
            do{
                int h = rand.nextInt(ht.getSize()+htBac.getSize());
                entry = h<ht.getSize()?ht.table[h]:htBac.table[h-ht.getSize()];
            }while (entry == null);
        }else{
            do{
                int h = rand.nextInt(ht.getSize());
                entry = ht.table[h];
            }while (entry == null);
        }
        //计算链表长度
        int listLen = 0;
        DictHt.Entry<K,V> temp = entry;
        while(temp!=null){
            temp = temp.next;
            listLen++;
        }
        //取链表上随机一个结点
        int listIndex = rand.nextInt(listLen);
        DictHt.Entry<K,V>  res = entry;
        while (listIndex--!=0){
            res = res.next;
        }

        return res;
    }
    /**
     * 获得字典里随机一个结点的键
     * @return
     */
    public K dictGetRandomKey(){
        DictHt.Entry<K,V> entry = dictGetRandomEntry();
        return entry.getKey();
    }

    /**
     * 获取所有键
     * @return
     */
    public Object[] getAllKeys(){
        int index = 0;
        Object[] res = new Object[ht.getUsed()];
        for(int i =0;i<ht.table.length;++i){
            for (DictHt.Entry<?, ?> e = ht.table[i]; e != null; e = e.next) {
                res[index++] = e.getKey();
            }
        }
        for(int i =0;i<htBac.table.length;++i){
            for (DictHt.Entry<?, ?> e = htBac.table[i]; e != null; e = e.next) {
                res[index++] = e.getKey();
            }
        }
        return res;
    }

    private void dictRehash(int n) {
        if (!isRehashing()) return;
        //进行n步rehash
        for (;n > 0;n--,treHashIdx++) {

            //前插法进行rehash
            DictHt.Entry<K, V> e;
            while ((e = ht.table[treHashIdx]) != null) {
                ht.table[treHashIdx]= e.next;
                ht.used--;

                //计算新的索引值
                int idx = getIndex(hash(e.getKey()), htBac.getSize());

                e.next = htBac.table[idx];
                htBac.table[idx] = e;
                htBac.used++;
            }

            //判断rehash结束
            if(treHashIdx+1>=ht.getSize()){
                ht = htBac;
                htBac = new DictHt<>(this);
                treHashIdx = -1;
                break;
            }
        }
    }

    @Override
    public void dictExpandIfNeed(int length) {
        if (isRehashing()) return;

        int newSize = roundUpToPowerOf2(length);
        //开始expand
        htBac.table = new DictHt.Entry[newSize];
        htBac.setSize(newSize);
        treHashIdx = 0;

    }

    @Override
    public void dictResize() {

    }
    private static int roundUpToPowerOf2(int number) {
        // assert number >= 0 : "number must be non-negative";
        int rounded = number >= MAXIMUM_CAPACITY
                ? MAXIMUM_CAPACITY
                : (rounded = Integer.highestOneBit(number)) != 0
                ? (Integer.bitCount(number) > 1) ? rounded << 1 : rounded
                : 1;

        return rounded;
    }

    //下方的代码均用来测试
    public static void main(String[] args) {
        Dict<SDS, SDS> dict = new Dict<>();
        String str = "leida";
        SDS sds = new SDS(str.toCharArray());
        System.out.println(dict.dictContainsKey(sds));
    }


    public static void printDictInfo(Dict<?,?> dict){
        System.out.println("treHashIdx: "+dict.treHashIdx);
        System.out.println("ht-size:"+dict.ht.getSize()+", ht-used:"+dict.ht.used);
        printHashTableInfo(dict.ht.table);
        System.out.println("htBac-size:"+dict.htBac.getSize()+", htBac-used:"+dict.htBac.used);
        printHashTableInfo(dict.htBac.table);
        System.out.println();
    }
    private static void printHashTableInfo(DictHt.Entry<?, ?>[] table){
        for(int i =0;i<table.length;++i){
            System.out.print(i+" ");
            for (DictHt.Entry<?, ?> e = table[i]; e != null; e = e.next) {
                System.out.print("key:"+e.getKey()+" val:"+e.getValue()+", ");
            }
            System.out.println("null");
        }
    }
    static void systemRead(Dict<String,Integer> dict) {
        try {
            InputStream in = System.in;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            for(;;){
                String str = bufferedReader.readLine();//从键盘输入获取内容
                String[] ops = str.split(" ");
                if(ops[0].equalsIgnoreCase("put")){
                    dict.put(ops[1],Integer.valueOf(ops[2]));
                }
                if(ops[0].equalsIgnoreCase("get")){
                    System.out.println(dict.get(ops[1]));
                }
                if(ops[0].equalsIgnoreCase("delete")){
                    System.out.println(dict.delete(ops[1]));
                }
                if(str.equalsIgnoreCase("exit")){
                    break;
                }
                printDictInfo(dict);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
