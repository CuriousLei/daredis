package cn.buptleida.util;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtil {
    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if(closeable == null) continue;
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
