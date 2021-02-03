package cn.buptleida.util;

public class ConvertUtil {
    /**
     * 将String值转化为对给定的类型
     * @param str
     * @param type
     * @return
     */
    public static Object convertFromStr(String str, Class type){
        if (String.class.equals(type)) {
            return str;
        } else if (int.class.equals(type)) {
            return Integer.parseInt(str);
        } else if (long.class.equals(type)) {
            return Long.parseLong(str);
        } else if (boolean.class.equals(type)) {
            return Boolean.parseBoolean(str);
        }
        return null;
    }
}
