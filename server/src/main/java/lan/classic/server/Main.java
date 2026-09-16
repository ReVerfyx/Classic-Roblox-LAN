package lan.classic.server;
import lan.classic.*;
import java.io.File;
public final class Main {
    public static void main(String[] args)throws Exception{
        int bots=1;String language="ru",mapFile=null,adminKey=null;GameConfig.Mode mode=GameConfig.Mode.DISASTERS;
        for(int i=0;i<args.length;i++){String arg=args[i];if(i==0&&!arg.startsWith("--")){bots=Integer.parseInt(arg);continue;}if(i==1&&!arg.startsWith("--")){language=arg;continue;}if("--bots".equals(arg)&&i+1<args.length)bots=Integer.parseInt(args[++i]);else if("--lang".equals(arg)&&i+1<args.length)language=args[++i];else if("--map".equals(arg)&&i+1<args.length)mapFile=args[++i];else if("--admin-key".equals(arg)&&i+1<args.length)adminKey=args[++i];else if("--mode".equals(arg)&&i+1<args.length)mode=GameConfig.Mode.valueOf(args[++i].toUpperCase());}
        if(bots<0||bots>16)throw new IllegalArgumentException("Bots 0..16");
        GameConfig config=new GameConfig();config.language=language;config.mode=mode;config.trustedScripts=mapFile!=null;
        World world=new World(config);
        if(mapFile!=null)world.loadPlace(AuthorizedPlaceLoader.read(new File(mapFile),adminKey));
        for(int i=0;i<bots;i++)world.add("Builder"+(i+1),true,language);
        final Net.Host host=new Net.Host(world,true,Net.PORT);Runtime.getRuntime().addShutdownHook(new Thread(new Runnable(){public void run(){host.close();}}));
        System.out.println("Classic LAN v3; TCP 53640 / UDP 53641; map="+world.mapName+"; bots="+bots+"; Ctrl+C to stop");
        new java.util.concurrent.CountDownLatch(1).await();
    }
}
