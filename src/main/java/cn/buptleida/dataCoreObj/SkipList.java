package cn.buptleida.dataCoreObj;

import java.util.Random;

public class SkipList<T extends Comparable<? super T>> {

    //首尾结点的指针
    private SkipListNode<T> header;
    private SkipListNode<T> tail;

    //记录跳表中结点数量
    private long length;

    //最大结点的层数
    private int maxLevel;

    public SkipList() {
        SkipListNode<T> node = new SkipListNode<>(null);
        this.header = node;
        this.tail = node;
        this.length = 0;
        this.maxLevel = 0;
    }

    /**
     * 插入新节点
     *
     * @param obj
     * @return
     */
    // public boolean insert(double score, T obj) {
    //
    //     int levelHeight = getRandomHeight();
    //     SkipListNode<T> node = new SkipListNode<>(obj, levelHeight);
    //     node.setBackword(tail);
    //
    //     SkipListNode<T> backNode = tail;
    //     int span =0;
    //     for (int i = 0; i < levelHeight; ++i) {
    //         if (i < backNode.getLevel().length) {
    //             backNode.getLevel()[i].setForward(node);
    //             backNode.getLevel()[i].setSpan(span);
    //         } else {
    //             backNode = backNode.getBackword();
    //             ++span;
    //             --i;
    //         }
    //     }
    //     //设置分值
    //     node.setScore(span+backNode.getScore());
    //     return true;
    // }
    public SkipListNode slInsert(double score, T obj) {
        int levelHeight = getRandomHeight();
        SkipListNode<T> target = new SkipListNode<>(obj, levelHeight, score);
        //设置一个update[]，大小为max(levelHeight,maxLevel)，对于新节点来说，update[i] 表示将新节点第i层插入到update[i]节点后面
        SkipListNode[] update = new SkipListNode[Math.max(levelHeight, maxLevel)];
        int[] rank = new int[update.length];//记录每一个update节点的排位
        int i = update.length - 1;
        if (levelHeight > maxLevel) {
            for (; i >= maxLevel; --i) {
                update[i] = header;
                rank[i] = 0;
            }
            maxLevel = levelHeight;
        }
        for (; i >= 0; --i) {

            SkipListNode<T> node = header;
            SkipListNode<T> next = node.getLevel()[i].getForward();
            rank[i] = 0;
            //遍历得到与target最接近的节点(左侧)
            while (next != null && (score > next.getScore() || score == next.getScore() && next.getObj().compareTo(obj) < 0)) {
                rank[i] += node.getLevel()[i].getSpan();
                node = next;
                next = node.getLevel()[i].getForward();

            }
            update[i] = node;
        }

        //当maxLevel>levelHeight，前面部分节点的span值加1，因为多出来一个新节点
        for (i = update.length - 1; i >= levelHeight; --i) {
            int span = update[i].getLevel()[i].getSpan();
            update[i].getLevel()[i].setSpan(++span);
        }
        //遍历 update[] 进行插入和更新操作
        for (; i >= 0; --i) {

            SkipListLevel pre = update[i].getLevel()[i];
            //将target节点插入update[i]和temp之间
            SkipListNode<T> temp = pre.getForward();
            int span = pre.getSpan();

            pre.setForward(target);
            pre.setSpan(rank[0] + 1 - rank[i]);

            target.getLevel()[i].setSpan(span > 0 ? (span - rank[0] + rank[i]) : 0);
            target.getLevel()[i].setForward(temp);
            //设置后退指针
            if (temp == null) {
                target.setBackword(header);
            } else {
                target.setBackword(temp.getBackword());
                temp.setBackword(target);
            }

        }

        if (tail.getLevel()[0].getForward() != null) {
            tail = target;
        }
        length++;
        return target;

    }


    /**
     * 获取随机的层高度
     *
     * @return
     */
    private static int getRandomHeight() {
        Random random = new Random();
        int i = 1;
        for (; i < 32; ++i) {
            if (random.nextInt(2) == 0) {
                break;
            }
        }
        return i;
    }

    public static void main(String[] args) {

        SkipList<Integer> skipList = new SkipList<>();
        skipList.slInsert(1.2, 32);
        skipList.slInsert(1.6, 30);
        skipList.slInsert(1.4, 30);
        skipList.slInsert(1.4, 30);
        printSkipList(skipList);

    }

    /**
     * 输出整个跳表
     */
    private static void printSkipList(SkipList<Integer> skipList) {
        System.out.println("length:" + skipList.length);
        System.out.println("maxLevel:" + skipList.maxLevel);
        SkipListNode<Integer> temp = skipList.header;
        while (temp != null) {
            System.out.println();
            System.out.print(" score:" + temp.getScore());
            System.out.print(" data:" + temp.getObj());
            System.out.print(" LEVELS: ");
            for (int i = 0; i < temp.getLevel().length && i < skipList.maxLevel; i++) {
                System.out.print(" level" + i + ":" + temp.getLevel()[i].getSpan());
            }

            temp = temp.getLevel()[0].getForward();
        }
    }
}
