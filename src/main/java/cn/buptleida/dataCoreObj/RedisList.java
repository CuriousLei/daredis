package cn.buptleida.dataCoreObj;

import cn.buptleida.dataCoreObj.base.RedisObj;
import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisEnc;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.enumerate.Status;
import cn.buptleida.dataCoreObj.underObj.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

public class RedisList extends RedisObject {
    public RedisList() {
        this.type = RedisType.LIST.VAL();
        this.encoding = RedisEnc.ZIPLIST.VAL();
        this.ptr = new ZipList();
    }

    /**
     * 向列表首部，插入整数结点
     * where取值：
     * 0：表示表头
     * 1：表示表尾
     */
    public void push(long val, int where) {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;
            zipList.push(val, where);

            checkVary(8);
        } else {
            List<SDS> linkedList = (List<SDS>) ptr;
            if (where == 0) linkedList.addNodeHead(new SDS(Long.toString(val).toCharArray()));
            else linkedList.addNodeTail(new SDS(Long.toString(val).toCharArray()));
        }
    }

    /**
     * 向列表首部，插入字符串结点
     * where取值：
     * 0：表示表头
     * 1：表示表尾
     */
    public void push(String val, int where) {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;
            byte[] byteArr = val.getBytes(StandardCharsets.UTF_16BE);
            zipList.push(byteArr, where);

            checkVary(byteArr.length);
        } else {
            List<SDS> linkedList = (List<SDS>) ptr;
            if (where == 0) linkedList.addNodeHead(new SDS(val.toCharArray()));
            else linkedList.addNodeTail(new SDS(val.toCharArray()));
        }
    }

    /**
     * 弹出结点
     */
    public String pop(int where) {
        String res;
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;
            zlentry entry;
            int pos;
            if (where == 0) {//读取首结点还是尾结点
                pos = 10;
            } else {
                pos = zipList.zlTail();
            }
            entry = zipList.getEntry(pos);
            if (entry == null) return null;
            if (ZipList.isIntVal(entry)) {//判断结点是整型还是字节数组
                long val = zipList.getNodeVal_Int(entry);
                res = Long.toString(val);
            } else {
                byte[] arr = zipList.getNodeVal_ByteArr(entry);
                res = new String(arr, StandardCharsets.UTF_16BE);
            }
            //删除位于pos的结点
            zipList.delete(pos);
        } else {
            List<SDS> linkedList = (List<SDS>) ptr;
            ListNode<SDS> node;
            if (where == 0) {//读取首结点还是尾结点
                node = linkedList.head();
            } else {
                node = linkedList.tail();
            }
            char[] arr = node.getValue().getArray();
            res = new String(arr);

            linkedList.delNode(node);
        }
        return res;
    }

    /**
     * 传参elementSize：当前添加的结点的长度；
     * 检查是否满足转换条件
     * 进行zipList和linkedList之间的转换;
     * 只在，添加元素且当前是zipList，删除元素且当前是linkList，两种情况下调用
     */
    private void checkVary(int elementSize) {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList list = (ZipList) ptr;
            int len = list.zlLen();
            if (len >= 512 || elementSize >= 64) {
                ptr = zipList2LinkList();
            }
        }
    }

    /**
     * 压缩列表转化为双端链表
     */
    private List<SDS> zipList2LinkList() {
        List<SDS> linkList = new List<>();
        ZipList zipList = (ZipList) ptr;
        int len = zipList.zlLen();
        for (int i = 0, pos = 10; i < len; ++i) {
            zlentry entry = zipList.getEntry(pos);
            //判断结点是整型还是字节数组
            if (ZipList.isIntVal(entry)) {
                long val = zipList.getNodeVal_Int(entry);
                char[] charArr = Long.toString(val).toCharArray();
                SDS sds = new SDS(charArr);
                linkList.addNodeTail(sds);
            } else {
                byte[] byteArr = zipList.getNodeVal_ByteArr(entry);
                String s = new String(byteArr, StandardCharsets.UTF_16BE);
                SDS sds = new SDS(s.toCharArray());
                linkList.addNodeTail(sds);
            }
            pos += entry.size();
        }

        encoding = RedisEnc.LINKEDLIST.VAL();
        return linkList;
    }

    /**
     * 获取列表长度
     */
    public long LLen() {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;
            return zipList.zlLen();
        } else {
            List<SDS> linkedList = (List<SDS>) ptr;
            return linkedList.getLen();
        }
    }

    /**
     * 根据结点值获取下标
     * 返回index
     */
    // public int search(String str) {
    //     if (encoding == RedisEnc.ZIPLIST.VAL()) {
    //         ZipList zipList = (ZipList) ptr;
    //
    //     } else {
    //         List<SDS> linkedList = (List<SDS>) ptr;
    //
    //     }
    //     return 0;
    // }

    /**
     * 在index的位置插入整型结点
     * 只用于压缩列表
     */
    public RedisObj insertAtIndex(int index, long val) {
        if (encoding == RedisEnc.LINKEDLIST.VAL()) return ptr;

        ZipList zipList = (ZipList) ptr;
        zipList.insertAt(index, val);

        return ptr;
    }

    /**
     * 在index的位置插入字符串结点
     */
    public RedisObj insertAtIndex(int index, String str) {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;

            zipList.insertAt(index, str.getBytes(StandardCharsets.UTF_16BE));
        } else {
            List<SDS> linkedList = (List<SDS>) ptr;
            ListNode<SDS> node = linkedList.head();
            SDS newSds = new SDS(str.toCharArray());
            if (linkedList.getLen() == 0 || index == 0) {
                linkedList.addNodeHead(newSds);
            } else {
                int k = 1;
                while (k != index) {
                    node = node.next;
                    k++;
                }
                linkedList.insertNode(node, newSds, 1);
            }

        }
        return ptr;
    }

    /**
     * 删除包含指定元素的结点
     * 返回success或error
     */
    public int deleteByVal(long val) {
        if (encoding == RedisEnc.LINKEDLIST.VAL()) return Status.ERROR;

        ZipList zipList = (ZipList) ptr;
        zipList.deleteByIntVal(val);
        return Status.SUCCESS;
    }

    public int deleteByVal(String str) {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;
            zipList.deleteByIntVal(str.getBytes(StandardCharsets.UTF_16BE));
            return Status.SUCCESS;
        }

        SDS anStr = new SDS(str.toCharArray());
        List<SDS> linkedList = (List<SDS>) ptr;
        ListNode<SDS> node = linkedList.head();

        while (node != null) {
            if (node.getValue().compareTo(anStr) == 0) {
                ListNode<SDS> next = node.next;
                linkedList.delNode(node);
                node = next;
                continue;
            }
            node = node.next;
        }

        return Status.SUCCESS;
    }

    public static void main(String[] args) {

        //byte[] arr = new byte[0];
        // List<SDS> linkList = new List<>();
        RedisList list = new RedisList();
        testLinkList(list);

        //list.push(1,1);
        // list.push("ertyuii",1);
        // list.push(876,0);
        // list.insertAtIndex(1,999999);
        // list.insertAtIndex(4,"uiuiuiui");
        // //list.insertAtIndex(7,6776);
        // System.out.println("列表长度："+list.LLen());
        // printAllItem(list);
        // // System.out.println(list.pop(0));
        // // printAllItem(list);
        // // System.out.println(list.pop(1));
        // list.deleteByVal("uiuiuiui");
        // list.deleteByVal(999999);
        //printAllItem(list);
    }

    private static void printAllItem(RedisList list) {
        if (list.encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) list.ptr;
            ZipList.print(zipList);
        } else {
            List<SDS> linkedList = (List<SDS>) list.ptr;
            Iterator<SDS> it = linkedList.iterator(List.AL_START_HEAD);
            while (it.hasNext()) {
                SDS sds = it.next();
                System.out.print(" " + Arrays.toString(sds.getBuf()));
            }
            System.out.println();
        }
    }

    private static void testLinkList(RedisList list) {
        // for(int i=1;i<401;++i){
        //     list.push(i,1);
        // }
        // for(int i=1;i<112;++i){
        //     list.push("sds"+i,1);
        // }
        for (int i = 1; i < 4; ++i) {
            list.push(i, 1);
        }
        list.push("qwertyuiopasdfghjklzxcvbnm1234567", 1);
        System.out.println("列表长度：" + list.LLen());
        printAllItem(list);

    }

}
