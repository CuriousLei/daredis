package cn.buptleida.nio.core;

import java.nio.channels.SocketChannel;

public interface ioProvider {
    boolean registerInput(SocketChannel channel, InputHandler inputHandler);

    boolean registerOutput(SocketChannel channel, OutputHandler outputHandler);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);

    void close();

    abstract class InputHandler implements Runnable {

        @Override
        public final void run() {
            handle();
        }

        protected abstract void handle();
    }

    abstract class OutputHandler implements Runnable {
        @Override
        public final void run() {
            handle();
        }

        protected abstract void handle();
    }

    interface IOCallback {
        void onInput(ioArgs args);

        void onOutput();

        void onChannelClosed();
    }
}
