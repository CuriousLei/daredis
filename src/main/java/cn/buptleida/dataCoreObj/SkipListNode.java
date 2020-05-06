package cn.buptleida.dataCoreObj;


import java.util.ArrayList;

class SkipListNode <T> {

    //层
    private SkipListLevel[] level;

    //后退指针
    private SkipListNode<T> backword;

    //分值
    private double score;

    //成员对象
    private T obj;

    SkipListNode(T obj){
        this.obj = obj;
        this.level = new SkipListLevel[32];
        initLevel(this.level,32);
        this.score = 0;
    }
    SkipListNode(T obj, int levelHeight,double score){
        this.obj = obj;
        this.level = new SkipListLevel[levelHeight];
        initLevel(this.level,levelHeight);
        this.score = score;
    }

    private void initLevel(SkipListLevel[] level, int height){
        for(int i=0;i<height;++i){
            level[i] = new SkipListLevel();
        }
    }

    public void setBackword(SkipListNode<T> backword) {
        this.backword = backword;
    }

    public void setLevel(SkipListLevel[] level) {
        this.level = level;
    }

    public SkipListNode<T> getBackword() {
        return backword;
    }

    public double getScore() {
        return score;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public SkipListLevel[] getLevel() {
        return level;
    }

}

