package dong.zhi.test;

import java.util.Random;

public class TestSync {

    public int i1;
    public int i2;
    public static int i3;

    public void f1(String arg){
        synchronized (arg){
            i1++;
        }
    }

    public synchronized void f2(String arg){
        i2++;
    }

    public static synchronized void f3(String arg){
        i3++;
    }

    public static void main(String[] args){
        final TestSync test = new TestSync();
        final String arg1 = "a";
        final String arg2 = "a";
        final Random random = new Random(100);
        for(int x = 0; x < 10; x++){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean bool = random.nextBoolean();
                    test.f1(bool ? arg1 : arg2);
                    test.f2(bool ? arg1 : arg2);
                    test.f3(bool ? arg1 : arg2);
                }
            }).start();
        }
        System.out.println(test.i1);
    }

}
