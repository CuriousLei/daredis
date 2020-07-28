package cn.buptleida.nio.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 公共的数据封装
 * 提供了类型以及基本的长度的定义
 */
public abstract class Packet<T extends Closeable> implements Closeable {
    private T stream;
    protected byte type;
    // public static final byte TYPE_MEMORY_BYTES = 1;
    // public static final byte TYPE_MEMORY_STRING = 2;
    // public static final byte TYPE_MEMORY_FILE = 3;
    // public static final byte TYPE_MEMORY_DIRECT = 4;

    protected long length;

    public byte type(){
        return type;
    }

    public long length(){
        return length;
    }

    protected abstract T createStream();
    protected void closeStream(T stream) throws  IOException{
        stream.close();
    }

    @Override
    public final void close() throws IOException {
        if(stream!=null){
            closeStream(stream);
            stream = null;
        }
    }

    public final T open() {
        if(stream==null){
            stream = createStream();
        }
        return stream;
    }
}
