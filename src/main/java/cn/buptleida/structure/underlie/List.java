package cn.buptleida.structure.underlie;

import cn.buptleida.structure.base.RedisObj;

import java.util.Iterator;

public class List<T extends Comparable<? super T>> implements RedisObj {
    public static int AL_START_HEAD = 0;
    public static int AL_START_TAIL = 1;

    private ListNode<T> head;
    private ListNode<T> tail;
    private long len;

    public List() {
        this.len = 0;
        this.head = null;
        this.tail = null;
    }

    /**
     * 给定val，新增节点到表头
     */
    public void addNodeHead(T val) {
        ListNode<T> node = new ListNode<>(val);
        if (len == 0) {
            head = tail = node;
            node.next = node.prev = null;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
            node.prev = null;
        }
        len++;
    }

    /**
     * 给定val，新增节点到表尾
     */
    public void addNodeTail(T val) {
        ListNode<T> node = new ListNode<>(val);
        if (len == 0) {
            head = tail = node;
            node.next = node.prev = null;
        } else {
            tail.next = node;
            node.prev = tail;
            node.next = null;
            tail = node;
        }
        len++;
    }

    /**
     * 给定val，获取对应节点。多个重复则返回第一个。
     */
    public ListNode<T> searchKey(T val){
        Itr it = this.iterator(AL_START_HEAD);
        while(it.hasNext()){
            if(it.next().compareTo(val)==0)
                return it.getCurrent();
        }
        return null;
    }
    /**
     * 删除给定节点
     */
    public void delNode(ListNode<T> node){
        if(node==null) return;
        //修改前置节点
        if(node.prev!=null){
            node.prev.next = node.next;
        }else{
            head = node.next;
        }
        //修改后置节点
        if(node.next!=null){
            node.next.prev = node.prev;
        }else{
            tail = node.prev;
        }
        len--;
    }
    /**
     * 根据val新建一个节点，插入某节点之前或之后;
     * 如果after为0，则插入oldNode之前
     * 如果after为1，则插入oldNode之后
     */
    public void insertNode(ListNode<T> oldNode, T val, int after){
        if(oldNode==null) return;

        ListNode<T> newNode = new ListNode<>(val);
        if(after == 0){
            if(oldNode == head) head = newNode;
            newNode.next = oldNode;
            newNode.prev = oldNode.prev;
        }else{
            if(oldNode == tail) tail = newNode;
            newNode.next = oldNode.next;
            newNode.prev = oldNode;
        }
        if(newNode.next!=null) newNode.next.prev = newNode;
        if(newNode.prev!=null) newNode.prev.next = newNode;

        len++;
    }

    public long getLen() {
        return len;
    }

    public ListNode<T> head() {
        return head;
    }

    public ListNode<T> tail() {
        return tail;
    }

    public Itr iterator(int direction) {
        return new Itr(direction);
    }

    private class Itr implements Iterator<T> {
        private final int direction;
        private ListNode<T> current;
        private ListNode<T> next;
        private int nextIndex;

        Itr(int direction) {
            this.direction = direction;
            this.nextIndex = 0;
            this.next = direction == AL_START_HEAD ? head : tail;
            this.current = null;
        }
        ListNode<T> getCurrent(){
            return this.current;
        }

        @Override
        public void remove() {
            if(current!=null){
                delNode(current);
                current = null;
                nextIndex--;
            }
        }
        @Override
        public boolean hasNext() {
            return nextIndex < len;
        }

        @Override
        public T next() {
            current = next;
            if (current != null) {
                if (direction == AL_START_HEAD) {
                    next = next.next;
                } else {
                    next = next.prev;
                }
            }
            nextIndex++;
            return current != null ? current.value: null;
        }
    }
}
