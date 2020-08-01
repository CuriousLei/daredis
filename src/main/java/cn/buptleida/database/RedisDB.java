package cn.buptleida.database;

import cn.buptleida.dataCoreObj.RedisString;
import cn.buptleida.dataCoreObj.base.RedisObject;
import cn.buptleida.dataCoreObj.enumerate.RedisType;
import cn.buptleida.dataCoreObj.enumerate.Status;
import cn.buptleida.dataCoreObj.underObj.Dict;
import cn.buptleida.dataCoreObj.underObj.SDS;

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

    //String相关命令

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

        return "+OK";
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
        if(val == null) return null;
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

    private RedisString getValByKey(String key) {
        SDS keySds = new SDS(key.toCharArray());

        RedisObject val = dict.get(keySds);
        if (val == null) return null;
        if (val.getType() != RedisType.STRING.VAL())
            return null;
        return (RedisString) val;
    }

    public static void main(String[] args) {

    }
}
