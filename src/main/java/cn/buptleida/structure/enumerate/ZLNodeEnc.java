package cn.buptleida.structure.enumerate;

public enum ZLNodeEnc {
    INT_8(254),
    INT_16(192),
    INT_24(240),
    INT_32(208),
    INT_64(224);
    private final int VAL;

    ZLNodeEnc(int val) {
        this.VAL = val;
    }

    public int VAL() {
        return VAL;
    }

    //根据整型content值得到编码值
    public static int getEncoding_Int(long val) {
        if (val >= 0 && val <= 12) {
            return INT_24.VAL + (int) val + 1;
        } else if (val >= IntEnc.INT_8.MIN() && val <= IntEnc.INT_8.MAX()) {
            return INT_8.VAL;
        } else if (val >= IntEnc.INT_16.MIN() && val <= IntEnc.INT_16.MAX()) {
            return INT_16.VAL;
        } else if (val >= IntEnc.INT_24.MIN() && val <= IntEnc.INT_24.MAX()) {
            return INT_24.VAL;
        } else if (val >= IntEnc.INT_32.MIN() && val <= IntEnc.INT_32.MAX()) {
            return INT_32.VAL;
        } else {
            return INT_64.VAL;
        }
    }

    //根据字节数组长度得到编码值
    public static long getEncoding_ByteArr(int byteArrLen) {
        if (byteArrLen < 64) {
            return byteArrLen;
        } else if (byteArrLen < 16384) {
            return 16384 + byteArrLen;
        } else {
            return ((long) 128 << 32) | byteArrLen;
        }
    }

    //根据编码值获取编码的长度
    public static int getEncSize(long encVal) {
        // if(encVal<64) return 1;
        // if(encVal > 32768) return 5;
        // if(getConLen((int)encVal)!=0) return 1;

        // return 2;
        if ((encVal | 255) == 255) return 1;
        if ((encVal | 65535) == 65535) return 2;

        return 5;
    }

    //整型编码，根据编码值得到content长度
    private static int getConLen(int encVal) {
        switch (encVal) {
            case 254:
                return 1;
            case 192:
                return 2;
            case 240:
                return 3;
            case 208:
                return 4;
            case 224:
                return 8;
        }
        if (encVal > 240 && encVal < 254) return 0;
        return -1;
    }

    //字节数组编码，根据编码值得到content长度
    public static int getConLen(long encVal) {
        int intEncVal = (int) encVal;
        int intRes;
        //content为整型的情况
        if ((intRes = getConLen(intEncVal)) != -1) return intRes;
        //encoding两字节的情况
        if (encVal > 63 && encVal < 32768) return intEncVal - 16384;

        //字节数组，encoding为1字节或5字节情况
        return intEncVal;

    }
}
