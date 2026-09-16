package lan.classic.server;
import lan.classic.*;
public final class Main {
    public static void main(String[] args)throws Exception{
        int bots=args.length>0?Integer.parseInt(args[0]):1;if(bots<0||bots>16)throw new IllegalArgumentException("Bots 0..16");
        String language=args.length>1?args[1]:"ru";World world=new World();for(int i=0;i<bots;i++)world.add("Builder"+(i+1),true,language);
        final Net.Host host=new Net.Host(world,true,Net.PORT);Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){public void run(){host.close();}}));
        System.out.println("Classic LAN v2; TCP 53640 / UDP 53641; bots="+bots+"; Ctrl+C to stop");
        new java.util.concurrent.CountDownLatch(1).await();
    }
}
