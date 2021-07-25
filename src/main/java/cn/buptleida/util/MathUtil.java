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

    /**
     * 取比number大的最小的2的幂
     * @param number 数字
     * @param maxCapacity 上限
     * @return 比目标数字大的最小的2的幂
     */
    public static long roundUpToPowerOf2(long number, long maxCapacity) {
        long rounded = number >= maxCapacity
                ? maxCapacity
                : (rounded = Long.highestOneBit(number)) != 0
                ? (Long.bitCount(number) > 1) ? rounded << 1 : rounded
                : 1;

        return rounded;
    }

    public static int roundUpToPowerOf2(int number, int maxCapacity) {
        int rounded = number >= maxCapacity
                ? maxCapacity
                : (rounded = Integer.highestOneBit(number)) != 0
                ? (Integer.bitCount(number) > 1) ? rounded << 1 : rounded
                : 1;

        return rounded;
    }
}
