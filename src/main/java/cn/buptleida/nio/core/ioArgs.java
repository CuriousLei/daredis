package cn.buptleida.nio.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;

public class ioArgs {
    private int limit = 256*256;
    private byte[] byteBuffer = new byte[256*256];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);
    private String srcUid;

    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    public int write(SocketChannel channel) throws IOException {
        return channel.write(buffer);
    }

    /**
     * 获取容量
     * @return
     */
    public int capacity(){
        return buffer.capacity();
    }
    /**
     * 从bytes中读到buffer
     *
     * @return
     */
    public int readFrom(ReadableByteChannel channel) throws IOException {
        // int size = Math.min(bytes.length - offset, buffer.remaining());
        // buffer.put(bytes, offset, size);
        // return size;
        startWriting();

        int byteProduced = 0;
        //hasRemaining就是position<limit返回true
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            byteProduced += len;
        }

        finishWriting();
        return byteProduced;
    }

    /**
     * 从buffer中写数据入到bytes
     *
     * @return
     */
    public int writeTo(WritableByteChannel channel) throws IOException {
        // int size = Math.min(bytes.length - offset, buffer.remaining());
        // buffer.get(bytes, offset, size);
        // return size;
        int byteProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            //System.out.println("发送长度"+len);
            if (len < 0) {
                throw new EOFException();
            }
            byteProduced += len;
        }
        return byteProduced;
    }

    public void startWriting() {
        buffer.clear();
        //定义容纳区间
        buffer.limit(limit);
    }

    public void finishWriting() {
        buffer.flip();
    }


    /**
     * 从channel中读取数据
     *
     * @param channel
     * @return
     * @throws IOException
     */
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();

        int byteProduced = 0;
        //hasRemaining就是position<limit返回true
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            byteProduced += len;
        }

        finishWriting();
        return byteProduced;
    }


    /**
     * 往channel中写数据
     *
     * @param channel
     * @return
     * @throws IOException
     */
    public int writeTo(SocketChannel channel) throws IOException {
        int byteProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            //System.out.println("发送长度"+len);
            if (len < 0) {
                throw new EOFException();
            }
            byteProduced += len;
        }
        return byteProduced;
    }

    public void writeLength(int total) {
        startWriting();
        buffer.putInt(total);
        finishWriting();
    }

    /**
     * 获取消息体的长度
     * @return
     */
    public int readLength(){
        //读取内部position开始的四个字节以int值返回，即获取首部
        return buffer.getInt();
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public String getSrcUid() {
        return srcUid;
    }

    public void setSrcUid(String srcUid) {
        this.srcUid = srcUid;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    // public interface IoArgsEventListener{
    //     void onStarted(ioArgs args);
    //
    //     void onCompleted(ioArgs args);
    // }

    /**
     * 消费状态的回调
     */
    public interface IoArgsEventProcessor{
        /**
         * 提供一份可消费的IoArgs
         * @return
         */
        ioArgs providerIoArgs();

        void onConsumeFailed(ioArgs args, Exception e);
        void onConsumeCompleted(ioArgs args);
    }
}
