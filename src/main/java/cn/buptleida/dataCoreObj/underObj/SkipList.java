package cn.buptleida.dataCoreObj.underObj;

import cn.buptleida.dataCoreObj.base.RedisObj;

import java.util.Random;

public class SkipList<T extends Comparable<? super T>>  implements RedisObj {

    //首尾结点的指针
    private SkipListNode<T> header;
    private SkipListNode<T> tail;

    //记录跳表中结点数量
    private long length;

    //最大结点的层数
    private int maxLevelHeight;

    public SkipList() {
        SkipListNode<T> node = new SkipListNode<>(null);
        this.header = node;
        this.tail = node;
        this.length = 0;
        this.maxLevelHeight = 0;
    }

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

    /**
     * 插入新节点
     *
     * @param score && obj
     * @return 插入的节点
     */
    public SkipListNode zslInsert(Double score, T obj) {
        int levelHeight = getRandomHeight();
        //if(score==2.6) System.out.println(levelHeight);
        SkipListNode<T> target = new SkipListNode<>(obj, levelHeight, score);
        //设置一个update[]，大小为max(levelHeight,maxLevel)，对于新节点来说，update[i] 表示将新节点第i层插入到update[i]节点后面
        SkipListNode[] update = new SkipListNode[Math.max(levelHeight, maxLevelHeight)];
        int[] rank = new int[update.length];//记录每一个update节点的排位
        int i = update.length - 1;
        if (levelHeight > maxLevelHeight) {
            for (; i >= maxLevelHeight; --i) {
                update[i] = header;
                rank[i] = 0;
            }
            maxLevelHeight = levelHeight;
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
            if (update[i].getLevel()[i].getForward() == null) continue;
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
     * 删除节点
     * @param obj
     * @return 删除的节点(若节点不存在则返回null)
     */
    public SkipListNode zslDelete(double score, T obj) {
        SkipListNode[] update = new SkipListNode[maxLevelHeight];
        SkipListNode<T> node = header;
        for (int i = maxLevelHeight - 1; i >= 0; --i) {
            SkipListNode<T> next = node.getLevel()[i].getForward();
            //遍历得到与target最接近的节点
            while (next != null && (score > next.getScore() || score == next.getScore() && next.getObj().compareTo(obj) < 0)) {
                node = next;
                next = node.getLevel()[i].getForward();
            }
            update[i] = node;
        }
        //待删除的目标节点
        SkipListNode<T> target = update[0].getLevel()[0].getForward();
        if(target==null) return null;

        for (int i = maxLevelHeight - 1; i >= 0; --i) {
            SkipListLevel current = update[i].getLevel()[i];
            SkipListNode<T> next = current.getForward();
            if (next == null) continue;
            if (next != target) {
                current.modifySpan(-1);
                continue;
            }
            current.setForward(target.getLevel()[i].getForward());
            if(current.getForward()!=null)
                current.modifySpan(target.getLevel()[i].getSpan() - 1);
            else
                current.setSpan(0);
        }
        length--;
        while(header.getLevel()[maxLevelHeight-1].getSpan()==0){
            maxLevelHeight--;
        }
        return target;
    }


    /**
     * 使用幂次定律获取随机层高度
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

    /**
     * 给定一个分值范围，如果至少有一个节点的分值在这范围之内则返回true，否则false
     * @return
     */
    public boolean zslIsInRange(double fromScore, double toScore) {
        if (header.getScore() > toScore || tail.getScore() < fromScore) {
            return false;
        }
        return true;
    }

    /**
     * 根据分值范围，返回第一个符合范围的节点
     * @param fromScore
     * @param toScore
     * @param node 表示从哪个结点开始访问
     * @param k 表示从哪个索引层开始访问
     * @return
     */
    public SkipListNode<T> zslFirstInRange(double fromScore, double toScore, SkipListNode<T> node, int k) {
        if (!zslIsInRange(fromScore, toScore)) {
            return null;
        }

        SkipListNode<T> next = node.getLevel()[k].getForward();

        if (next == null || next.getScore() >= fromScore) {
            if (k == 0) return next != null && next.getScore() > toScore ? null : next;
            return zslFirstInRange(fromScore, toScore, node, k - 1);
        }
        return zslFirstInRange(fromScore, toScore, next, k);
    }

    /**
     * 根据分值范围，返回最后一个符合范围的节点
     *
     * @param fromScore
     * @param toScore
     * @return
     */
    public SkipListNode<T> zslLastInRange(double fromScore, double toScore, SkipListNode<T> node, int k) {
        if (!zslIsInRange(fromScore, toScore)) {
            return null;
        }

        SkipListNode<T> next = node.getLevel()[k].getForward();

        if (next == null || next.getScore() > toScore) {
            if (k == 0) return next != null && next.getScore() < fromScore ? null : node;
            return zslLastInRange(fromScore, toScore, node, k - 1);
        }
        return zslLastInRange(fromScore, toScore, next, k);
    }

    public SkipListNode<T> searchByScore(double score, SkipListNode<T> node, int k) {
        return zslFirstInRange(score, score, node, k);
    }

    public SkipListNode<T> getHeader() {
        return header;
    }

    public int getMaxLevelHeight() {
        return maxLevelHeight;
    }

    /**
     * 测试
     * @param args
     */
    public static void main(String[] args) {

        SkipList<Integer> skipList = new SkipList<>();
        skipList.zslInsert(1.2, 32);
        skipList.zslInsert(1.6, 30);
        skipList.zslInsert(1.4, 36);
        skipList.zslInsert(1.4, 30);
        // skipList.zslInsert(2.6, 32);
        // skipList.zslInsert(2.6, 30);
        // skipList.zslInsert(2.6, 36);
        // skipList.zslInsert(2.6, 30);
         skipList.zslInsert(2.6, 56);
        skipList.zslInsert(3.6, 119);
        printSkipList(skipList);

        //SkipListNode<Integer> temp = skipList.searchByScore(2.6, skipList.header, skipList.maxLevelHeight - 1);
        // SkipListNode<Integer> tempFirst = skipList.zslFirstInRange(1.2, 2.7, skipList.header,
        //         skipList.maxLevelHeight - 1);
        // SkipListNode<Integer> tempLast = skipList.zslLastInRange(1.36, 1.6, skipList.header,
        //         skipList.maxLevelHeight - 1);
        // printNode(tempFirst);
        // printNode(tempLast);

        skipList.zslDelete(2.6,56);
        printSkipList(skipList);

    }


    /**
     * 输出整个跳表
     */
    public static void printSkipList(SkipList<?> skipList) {
        System.out.println("length:" + skipList.length);
        System.out.println("maxLevel:" + skipList.maxLevelHeight);
        SkipListNode<?> temp = skipList.header;
        while (temp != null) {
            System.out.println();
            System.out.print(" score:" + temp.getScore());
            System.out.print(" data:" + temp.getObj());
            System.out.print(" LEVELS: ");
            for (int i = 0; i < temp.getLevel().length && i < skipList.maxLevelHeight; i++) {
                System.out.print(" level" + i + ":" + temp.getLevel()[i].getSpan());
            }

            temp = temp.getLevel()[0].getForward();
        }
        System.out.println();
    }

    /**
     * 输出节点信息
     * @param node
     */
    private static void printNode(SkipListNode<Integer> node) {
        System.out.println();
        System.out.print(" score:" + node.getScore());
        System.out.print(" data:" + node.getObj());
        System.out.print(" LEVELS: ");
        for (int i = 0; i < node.getLevel().length; i++) {
            System.out.print(" level" + i + ":" + node.getLevel()[i].getSpan());
        }
    }
}
