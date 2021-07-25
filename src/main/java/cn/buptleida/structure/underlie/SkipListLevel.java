package cn.buptleida.structure.underlie;

public class SkipListLevel{

    //前进指针
    private SkipListNode forward;

    //跨度
    private int span;

    SkipListLevel() {
        this.forward = null;
        this.span = 0;
    }

    public SkipListNode getForward() {
        return forward;
    }

    public void setForward(SkipListNode forward) {
        this.forward = forward;
    }

    public int getSpan() {
        return span;
    }

    public void setSpan(int span) {
        this.span = span;
    }
    public void modifySpan(int offset) {
        this.span = this.span  + offset;
    }
}
