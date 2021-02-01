package pressure;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.channels.Selector;

public class JedisTest {
    private final Selector readSelector;
    private Object lock = new Object();
    private Thread thread1;
    private Thread thread2;

    public JedisTest() throws IOException {
        this.readSelector = Selector.open();
        this.thread1 = new Thread(() -> {
            while(true){
                try {
                    if (readSelector.select() == 0) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("select被唤醒");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void test(){
        thread1.start();
        long start = System.currentTimeMillis();
        for(int i=0;i<10000;++i){
            readSelector.wakeup();
        }
        long end = System.currentTimeMillis();
        System.out.println("耗时："+(end-start));
        // System.out.println("唤醒第一次");
        // readSelector.wakeup();
        // System.out.println("唤醒第二次");
    }


    public static void main(String[] args) throws IOException {
        JedisTest test = new JedisTest();
        test.test();
    }

}
