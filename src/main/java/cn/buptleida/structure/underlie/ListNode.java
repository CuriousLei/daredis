package cn.buptleida.structure.underlie;

public class ListNode<T> {
    public ListNode<T> prev;
    public ListNode<T> next;
    T value;

    ListNode(T x) {
        value = x;
    }

    public T getValue() {
        return value;
    }
}
