package cn.buptleida.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MathUtil {
    /**
     * 判断字符串是否能转换为整型
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        if(str.length()>18) return false;
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if(isNum.matches())
            return true;
        return false;
    }
}
