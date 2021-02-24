package cn.buptleida.dataCoreObj;

import cn.buptleida.dataCoreObj.base.CmdExecutor;
import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisEnc;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.enumerate.Status;
import cn.buptleida.dataCoreObj.underObj.*;

import java.nio.charset.StandardCharsets;

public class RedisZSet extends RedisObject implements CmdExecutor {

    public RedisZSet() {
        this.type = RedisType.ZSET.VAL();
        this.encoding = RedisEnc.ZIPLIST.VAL();
        this.ptr = new ZipList();
    }

    /**
     * 插入新元素
     */
    public void zAdd(String member, double score) {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;

            byte[] memberBytes = member.getBytes(StandardCharsets.UTF_16BE);
            byte[] scoreBytes = Double.toString(score).getBytes(StandardCharsets.UTF_8);

            //判断结点是否已存在，若存在则删除
            zlentry node;
            if ((node = zipList.exist(memberBytes, 1)) != null) {
                zipList.delete(node.endPos());
                zipList.delete(node);
            }
            int pos = zipList.zlTail();
            while (pos >= 10) {
                zlentry entry = zipList.getEntry(pos);
                byte[] entryScore = zipList.getNodeVal_ByteArr(entry);
                String s = new String(entryScore, StandardCharsets.UTF_8);
                double sc = Double.valueOf(s);
                if (score >= sc) {
                    zipList.insertAfter(entry, scoreBytes);
                    zipList.insertAfter(entry, memberBytes);
                    break;
                }
                pos = zipList.getPrePos(zipList.getPrePos(pos));
            }
            //待插入的位置为压缩列表首部
            if(pos<10){
                zipList.push(scoreBytes,0);
                zipList.push(memberBytes,0);
            }
            checkVary(memberBytes.length);
        } else {
            ZSet<SDS> zSet = (ZSet<SDS>) ptr;
            SDS key = new SDS(member.toCharArray());
            Double val;
            if ((val = zSet.dict.delete(key)) != null) {
                zSet.zsl.zslDelete(val, key);
            }
            zSet.zsl.zslInsert(score, key);
            zSet.dict.put(key, score);
        }
    }
    /**
     * @param elementSize 当前添加的结点的长度；
     * 检查是否满足转换条件
     * 进行zipList和linkedList之间的转换;
     * 只在，添加元素且当前是zipList，这种情况下调用
     */
    private void checkVary(int elementSize){
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList list = (ZipList) ptr;
            int len = list.zlLen()/2;
            if (len >= 128 || elementSize >= 64) {
                zipList2SkipList();
            }
        }
    }
    private void zipList2SkipList(){
        ZSet<SDS> zSet = new ZSet<>();

        ZipList zipList = (ZipList) ptr;
        int scorePos = zipList.zlTail();
        int memPos = zipList.getPrePos(scorePos);
        while(memPos>=10){
            //获取成员结点值
            zlentry memEntry = zipList.getEntry(memPos);
            byte[] memBytes = zipList.getNodeVal_ByteArr(memEntry);
            String memStr = new String(memBytes,StandardCharsets.UTF_16BE);
            SDS member = new SDS(memStr.toCharArray());
            //获取分值
            zlentry scoreEntry = zipList.getEntry(scorePos);
            byte[] scoreBytes = zipList.getNodeVal_ByteArr(scoreEntry);
            String scoreStr = new String(scoreBytes, StandardCharsets.UTF_8);
            double score = Double.valueOf(scoreStr);
            //插入到字典和跳跃表中
            zSet.dict.put(member,score);
            zSet.zsl.zslInsert(score,member);

            scorePos = zipList.getPrePos(memPos);
            memPos = zipList.getPrePos(scorePos);
        }

        encoding = RedisEnc.SKIPLIST.VAL();
        ptr = zSet;
    }

    /**
     * 获取集合中的元素数量
     *
     * @return
     */
    public int zCard() {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;
            return zipList.zlLen() / 2;
        }

        ZSet<SDS> zSet = (ZSet<SDS>) ptr;
        return zSet.dict.dictSize();
    }

    /**
     * 统计给定范围内的结点数量
     *
     * @return
     */
    public int zCount(double min, double max) {
        if (encoding == RedisEnc.ZIPLIST.VAL()) {
            ZipList zipList = (ZipList) ptr;
            int pos = zipList.zlTail();
            int count = 0;
            //从表尾向表头遍历压缩列表每个分值
            while (pos > 0) {
                zlentry entry = zipList.getEntry(pos);
                byte[] entryScore = zipList.getNodeVal_ByteArr(entry);
                String s = new String(entryScore, StandardCharsets.UTF_8);
                double score = Double.valueOf(s);
                if (score < min) break;
                if (score <= max) count++;
                pos = zipList.getPrePos(zipList.getPrePos(pos));
            }
            return count;
        } else {
            ZSet<SDS> zSet = (ZSet<SDS>) ptr;
            SkipListNode<SDS> firstNodeInRange = zSet.zsl.zslFirstInRange(min, max, zSet.zsl.getHeader(),
                    zSet.zsl.getMaxLevelHeight() - 1);
            SkipListNode<SDS> lastNodeInRange = zSet.zsl.zslLastInRange(min, max, zSet.zsl.getHeader(),
                    zSet.zsl.getMaxLevelHeight() - 1);
            if(firstNodeInRange == null || lastNodeInRange == null)
                return 0;
            int rankLeft = getRank(firstNodeInRange.getScore(), firstNodeInRange.getObj(),zSet.zsl.getHeader(),
                    zSet.zsl.getMaxLevelHeight() - 1);
            int rankRight = getRank(lastNodeInRange.getScore(), lastNodeInRange.getObj(),zSet.zsl.getHeader(),
                    zSet.zsl.getMaxLevelHeight() - 1);
            return rankRight - rankLeft + 1;
        }
    }

    /**
     * 给定成员，计算结点的升序排位
     * @param obj
     * @return 返回0表示不存在包含给定分值和成员的结点
     */
    public int zRank(String obj) {
        if(encoding == RedisEnc.ZIPLIST.VAL()){
            ZipList zipList = (ZipList) ptr;
            int rank = zipList.zlLen()/2;
            int scPos = zipList.zlTail();
            int objPos = zipList.getPrePos(scPos);
            while (objPos >= 10) {
                zlentry entry = zipList.getEntry(objPos);
                byte[] entryObjBytes = zipList.getNodeVal_ByteArr(entry);
                String itemObj = new String(entryObjBytes,StandardCharsets.UTF_16BE);
                if(itemObj.equals(obj)){
                    break;
                }
                scPos = zipList.getPrePos(objPos);
                objPos = zipList.getPrePos(scPos);
                rank--;
            }
            return rank;
        }

        ZSet<SDS> zSet = (ZSet<SDS>) ptr;
        SDS objSds = new SDS(obj.toCharArray());
        Double sc = zSet.dict.get(objSds);
        if (sc == null) return -1;

        double score = zSet.dict.get(objSds);
        return getRank(score, objSds, zSet.zsl.getHeader(), zSet.zsl.getMaxLevelHeight() - 1);
    }

    /**
     * 给定成员字符串，获得结点逆序排位
     * @param obj
     * @return
     */
    public int zRevRank(String obj){
        int rank = zRank(obj);
        if(rank<1) return 0;
        return zCard() - rank + 1;
    }

    /**
     * @param score 待查找结点的分值
     * @param obj   待查找结点的成员
     * @param node  每次递归时访问的结点
     * @param k     索引层编号
     * @return
     */
    private int getRank(double score, SDS obj, SkipListNode<SDS> node, int k) {
        SkipListNode<SDS> next = node.getLevel()[k].getForward();

        if (next == null || next.getScore() > score || score == next.getScore() && next.getObj().compareTo(obj) > 0) {
            if (k == 0) return 0;
            return getRank(score, obj, node, k - 1);
        }
        return node.getLevel()[k].getSpan() + getRank(score, obj, next, k);
    }

    /**
     * 删除包含给定成员的结点
     * @param member
     */
    public int zRem(String member){
        if(encoding == RedisEnc.ZIPLIST.VAL()){
            ZipList zipList = (ZipList) ptr;
            byte[] keyBytes = member.getBytes(StandardCharsets.UTF_16BE);
            zlentry keyEntry;
            if((keyEntry=zipList.exist(keyBytes,1))==null)
                return Status.ERROR;
            zlentry valEntry = zipList.getNextEntry(keyEntry);
            zipList.delete(valEntry);
            zipList.delete(keyEntry);
            return Status.SUCCESS;
        }else{
            ZSet<SDS> zSet = (ZSet<SDS>) ptr;
            SDS memberSDS = new SDS(member.toCharArray());
            Double score;
            if ((score = zSet.dict.delete(memberSDS)) != null) {
                zSet.zsl.zslDelete(score, memberSDS);
                return Status.SUCCESS;
            }
            return Status.ERROR;
        }
    }
    /**
     * 根据给定成员获取分值
     * @param member
     */
    public Double zScore(String member){
        if(encoding == RedisEnc.ZIPLIST.VAL()){
            ZipList zipList = (ZipList) ptr;
            int len = zipList.zlLen()/2;
            //从表头向表尾遍历压缩列表
            for (int i = 0, pos = 10; i < len; ++i){
                zlentry memberEntry = zipList.getEntry(pos);
                zlentry scoreEntry = zipList.getNextEntry(memberEntry);

                String s = new String(zipList.getNodeVal_ByteArr(memberEntry),StandardCharsets.UTF_16BE);
                if(member.equals(s)){
                    //从相邻下一个结点中取出score值
                    byte[] scoreBytes = zipList.getNodeVal_ByteArr(scoreEntry);
                    String scoreStr = new String(scoreBytes, StandardCharsets.UTF_8);
                    double score = Double.valueOf(scoreStr);
                    return score;
                }
                pos = scoreEntry.endPos();
            }
            return null;
        }else{
            ZSet<SDS> zSet = (ZSet<SDS>) ptr;
            SDS memberSDS = new SDS(member.toCharArray());
            return zSet.dict.get(memberSDS);
        }
    }
}
