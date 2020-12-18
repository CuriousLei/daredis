package cn.buptleida.database;

import cn.buptleida.dataCoreObj.RedisHash;
import cn.buptleida.dataCoreObj.RedisString;
import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.underObj.Dict;
import cn.buptleida.dataCoreObj.underObj.SDS;
import cn.buptleida.util.MathUtil;

import java.util.Arrays;

public class RedisDB {

    //数据库的键空间
    Dict<SDS, RedisObject> dict;

    public RedisDB() {
        this.dict = new Dict<>();
    }
    //键命令

    /**
     * 获取所有键
     * @param params
     * @return
     */
    public String KEYS(String ... params){
        String pattern = params[0];

        if(pattern.equals("*")){
            Object[] objList = dict.getAllKeys();
            return Arrays.toString(objList);
        }
        return null;
    }

    /*----------------------------String相关命令----------------------------*/

    /**
     * 插入键值对
     * @param params{key, val}
     * @return
     */
    public String SET(String ... params) {
        String key = params[0];
        String val = params[1];

        SDS keySds = new SDS(key.toCharArray());

        //Long.parseLong(val);
        RedisString valStr = new RedisString(val);
        dict.put(keySds, valStr);

        return SuccessInfo();
    }


    /**
     * 根据键获取对应的值
     *
     * @param params{key}
     * @return
     */
    public String GET(String ... params) {
        String key = params[0];

        RedisString val = getValByKey(key);
        if(val == null) return NotExistInfo(key);
        return val.get();
    }

    /**
     * 拼接字符串
     *
     * @param params{key,extStr}
     * @return 返回新字符串的长度
     */
    public int APPEND(String ... params) {
        String key = params[0];
        String extStr = params[1];

        RedisString val = getValByKey(key);
        if(val == null){//不存在则直接SET一个新字符串
            SET(key,extStr);
            return extStr.length();
        }

        return val.append(extStr);
    }

    /**
     * 根据给定键，返回对应的字符串长度
     * @param params{key}
     * @return
     */
    public int STRLEN(String ... params) {
        String key = params[0];

        RedisString val = getValByKey(key);
        if(val==null) return -1;
        return val.strlen();
    }

    /**
     * 将 key 中储存的数字值增一
     * @param params{key}
     * @return 增一后的结果
     */
    public long INCR(String ... params){
        String key = params[0];

        RedisString val = getValByKey(key);
        if(val==null){
            SET(key,"0");
            return INCR(key);
        }
        return val.incrby();
    }
    /**
     * 将 key 中储存的数字值减一
     * @param params{key}
     * @return 减一后的结果
     */
    public long DECR(String ... params){
        String key = params[0];

        RedisString val = getValByKey(key);
        if(val==null){
            SET(key,"0");
            return DECR(key);
        }
        return val.decrby();
    }

    /*----------------------------HashTable相关命令----------------------------*/
    /**
     * 哈希表插入
     * @param params
     * @return
     */
    public String HSET(String ... params){
        String key = params[0];
        RedisHash ht = getHashValByKey(key);
        if(ht==null){
            ht = new RedisHash();
            dict.put(new SDS(key.toCharArray()),ht);
        }
        RedisHash ht2 = getHashValByKey(key);
        String iVal = params[2];
        if(MathUtil.isNumeric(iVal)){
            ht.hSet(params[1],Long.parseLong(iVal));
        }else{
            ht.hSet(params[1],iVal);
        }
        return SuccessInfo();
    }
    /**
     * 哈希表查找
     * @param params
     * @return
     */
    public String HGET(String ... params){
        String key = params[0];
        RedisHash ht = getHashValByKey(key);
        if(ht==null){
            return NotExistInfo(key);
        }
        return ht.hGet(params[1]);
    }

    /**
     * 哈希表获取长度
     * @param params
     * @return
     */
    public String HLEN(String ... params){
        String key = params[0];
        RedisHash ht = getHashValByKey(key);
        if(ht==null){
            return NotExistInfo(key);
        }
        return String.valueOf(ht.hLen());
    }

    /**
     * 哈希表判断是否存在某键值对
     * @param params
     * @return
     */
    public String HEXISTS(String ... params){
        String key = params[0];
        RedisHash ht = getHashValByKey(key);
        if(ht==null){
            return NotExistInfo(key);
        }
        return String.valueOf(ht.hExists(params[1]));
    }
    public String HDEL(String ... params){
        String key = params[0];
        RedisHash ht = getHashValByKey(key);
        if(ht==null){
            return NotExistInfo(key);
        }
        int status = ht.hDel(params[1]);
        if(status == 1){
            return SuccessInfo();
        }else{
            return FailureInfo();
        }
    }

    /*----------------------------私有方法----------------------------*/
    private RedisString getValByKey(String key) {
        SDS keySds = new SDS(key.toCharArray());

        RedisObject val = dict.get(keySds);
        if (val == null) return null;
        if (val.getType() != RedisType.STRING.VAL())
            return null;
        return (RedisString) val;
    }

    private RedisHash getHashValByKey(String key) {
        SDS keySds = new SDS(key.toCharArray());

        RedisObject val = dict.get(keySds);
        if (val == null) return null;
        if (val.getType() != RedisType.HASH.VAL())
            return null;
        return (RedisHash) val;
    }
    private boolean isExist(String key){
        SDS keySds = new SDS(key.toCharArray());
        return dict.exist(keySds);
    }
    private String NotExistInfo(String key){
        return "Key: \'"+key+"\' not exist ~";
    }
    private String SuccessInfo(){
        return "OK";
    }
    private String FailureInfo(){
        return "FAIL";
    }

}
