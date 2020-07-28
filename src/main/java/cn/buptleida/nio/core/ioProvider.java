package cn.buptleida.nio.core;

import java.nio.channels.SocketChannel;

public interface ioProvider {
    boolean registerInput(SocketChannel channel, InputHandler inputHandler);

    boolean registerOutput(SocketChannel channel, OutputHandler outputHandler);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);

    abstract class InputHandler implements Runnable {

        @Override
        public final void run() {
            handle();
        }

        protected abstract void handle();
    }

    abstract class OutputHandler implements Runnable {
        // Object attach;
        // public final void setAttach(Object attach) {
        //     this.attach = attach;
        // }
        //
        // public final <T> T getAttach() {
        //     return (T) this.attach;
        // }

        // @Override
        // public final void run() {
        //     handle(attach);
        // }
        //
        // protected abstract void handle(Object attach);
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
