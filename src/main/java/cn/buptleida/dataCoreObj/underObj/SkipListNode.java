package cn.buptleida.dataCoreObj.underObj;


public class SkipListNode <T> {

    //层
    private SkipListLevel[] level;

    //后退指针
    private SkipListNode<T> backword;

    //分值
    private Double score;

    //成员对象
    private T obj;

    SkipListNode(T obj){
        this.obj = obj;
        this.level = new SkipListLevel[32];
        initLevel(this.level,32);
        this.score = Double.valueOf(0);
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

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if(anObject instanceof SkipListNode){
            SkipListNode node = (SkipListNode) anObject;
            return score.equals(node.score) && obj.equals(node.obj);
        }
        return false;
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

