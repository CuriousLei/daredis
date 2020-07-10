package cn.buptleida.dataCoreObj.underObj;

import java.util.Map;

class DictHt<K, V> {

    //强制rehash的负载因子
    static final int DICT_FORCE_RESIZE_RATIO = 5;

    //自动rehash的开关
    private boolean dict_can_resize = true;
    //哈希表大小
    private int size;

    //哈希表大小掩码，用于计算索引值
    private int sizeMask;

    //哈希表中已有节点数量
    int used;

    static final private Entry<?, ?>[] EMPTY_TABLE = {};

    Entry<K, V>[] table;

    private final DictCallBack dict;

    static class Entry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private V value;
        int hash;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        Entry(K key, V value, Entry<K, V> next, int hash) {
            this.key = key;
            this.value = value;
            this.next = next;
            this.hash = hash;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            return this.value = value;
        }
    }

    interface DictCallBack {
        void dictExpandIfNeed(int length);

        void dictResize();
    }

    DictHt(DictCallBack dict) {
        this.table = (Entry<K, V>[]) EMPTY_TABLE;
        this.size = 0;
        this.used = 0;
        this.dict = dict;
    }

    void insertEntry(K key, V value, int index) {
        DictHt.Entry<K, V> e = table[index];
        table[index] = new DictHt.Entry<>(key, value, e);
        used++;

        //满足条件时，通知Dict进行rehash
        if (used >= size && (dict_can_resize || used / size >= DICT_FORCE_RESIZE_RATIO)) {
            dict.dictExpandIfNeed(2 * size);
        }
    }

    Entry<K, V> delete(K key, int index) {
        // Entry<K, V> e = table[index];

        for (Entry<K, V> e = table[index], prev = e; e != null; prev = e, e = e.next) {
            if (key == e.key || key.equals(e.key)) {
                if (e == prev) {
                    table[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                used--;
                return e;
            }
        }
        return null;
    }


    Entry<K, V> containsKey(K key, int index) {
        for (Entry<K, V> e = table[index]; e != null; e = e.next) {
            if (key == e.key || key.equals(e.key)) {
                return e;
            }
        }
        return null;
    }


    int getSize() {
        return size;
    }

    int getUsed() {
        return used;
    }

    void setSize(int len) {
        size = len;
    }
}
