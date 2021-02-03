package cn.buptleida.conf;

public class Command {
    private final String name;//命令名称
    private final String method;//执行命令需要调用的方法
    private final int paramNum;//命令参数个数
    private final int flag;//标识命令的类型，如0b1表示只读，0b10表示写入
    private final byte level;//0b1,0b10,0b100,0b1000分别表示对象级、键空间级、数据库级、客户端级
    private final Class[] paramClasses;

    public Command(String name, String method, int paramNum, int flag, byte level, Class[] paramClasses) {
        this.name = name;
        this.method = method;
        this.paramNum = paramNum;
        this.flag = flag;
        this.level = level;
        this.paramClasses = paramClasses;
    }

    public String getName() {
        return name;
    }

    public String getMethod() {
        return method;
    }

    public int getParamNum() {
        return paramNum;
    }

    public int getFlag() {
        return flag;
    }

    public byte getLevel() {
        return level;
    }

    public Class[] getParamClasses() {
        return paramClasses;
    }
}
