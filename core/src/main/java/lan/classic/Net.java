package lan.classic;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
/** v4 framed binary TCP. Server is authoritative; clients send input, never positions. */
public final class Net {
    public static final int PORT=53640,DISCOVERY=53641,MAGIC=0x434c414e,VERSION=4;
    public static final class Snapshot {
        public int you;public float water;public String round="CONNECTING",map="Classic Baseplate";public final List<Actor> actors=new ArrayList<Actor>();public final List<String> chat=new ArrayList<String>();
    }
    public static Snapshot snapshot(World w,int you){synchronized(w){Snapshot s=new Snapshot();s.you=you;s.water=w.water;s.round=w.round;s.map=w.mapName;for(Actor a:w.actors.values()){Actor b=new Actor(a.id,a.name,false);b.x=a.x;b.y=a.y;b.z=a.z;b.yaw=a.yaw;b.health=a.health;b.phase=a.phase;b.state=a.state;b.appearance=a.appearance.copy();b.tool=a.tool;s.actors.add(b);}s.chat.addAll(w.chat);return s;}}
    public static final class Host implements Closeable {
        public final World world;private volatile boolean running=true;private ServerSocket server;private DatagramSocket discovery;
        private final List<Socket> sockets=Collections.synchronizedList(new ArrayList<Socket>());
        private final ScheduledExecutorService tick=Executors.newSingleThreadScheduledExecutor();
        public Host(World w,boolean lan,int port)throws IOException{
            world=w;
            if(lan){try{server=new ServerSocket();server.setReuseAddress(true);server.bind(new InetSocketAddress(port));discovery=new DatagramSocket(null);discovery.setReuseAddress(true);discovery.bind(new InetSocketAddress(DISCOVERY));discovery.setSoTimeout(1000);}catch(IOException e){close();throw e;}
                thread(new Runnable(){public void run(){accept();}},"accept");thread(new Runnable(){public void run(){discover();}},"discovery");}
            tick.scheduleAtFixedRate(new Runnable(){public void run(){world.tick(1f/30);}},0,33333333,TimeUnit.NANOSECONDS);
        }
        private void accept(){while(running)try{final Socket s=server.accept();if(sockets.size()>=20){s.close();continue;}s.setTcpNoDelay(true);s.setSoTimeout(5000);sockets.add(s);thread(new Runnable(){public void run(){serve(s);}},"peer");}catch(IOException e){if(!running)return;}}
        private void serve(Socket s){Actor a=null;try{
            DataInputStream in=new DataInputStream(new BufferedInputStream(s.getInputStream()));DataOutputStream out=new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            if(in.readInt()!=MAGIC||in.readInt()!=VERSION)throw new IOException("Protocol mismatch");
            String name=readText(in,24),language=readText(in,8);Appearance appearance=Appearance.read(in);if(!name.matches("[A-Za-z0-9_]{3,20}"))throw new IOException("Invalid username");
            synchronized(world){for(Actor existing:world.actors.values())if(existing.name.equalsIgnoreCase(name))throw new IOException("Username in use");if(world.actors.size()>=20)throw new IOException("Server full");a=world.add(name,false,language);a.appearance=appearance;}
            out.writeInt(MAGIC);out.writeInt(a.id);out.flush();
            while(running){int op=in.readUnsignedByte();if(op!=1&&op!=2&&op!=3)throw new IOException("Invalid opcode");float x=in.readFloat(),z=in.readFloat();boolean jump=in.readBoolean();int tool=op==3?in.readUnsignedByte():-1;String msg=readText(in,640);if(op==2)world.resetCharacter(a.id);if(op==3)world.useTool(a.id,tool);world.input(a.id,x,z,jump);if(msg.length()>0)world.message(a.id,msg);writeSnapshot(out,snapshot(world,a.id));out.flush();}
        }catch(IOException ignored){}finally{if(a!=null)world.remove(a.id);sockets.remove(s);try{s.close();}catch(IOException ignored){}}}
        private void discover(){byte[] data=new byte[64];while(running)try{DatagramPacket p=new DatagramPacket(data,data.length);discovery.receive(p);String q=new String(p.getData(),0,p.getLength(),"UTF-8");if(!q.equals("CLASSIC_LAN_DISCOVER_3"))continue;int count;synchronized(world){count=world.actors.size();}byte[] r=("CLASSIC_LAN_3|"+server.getLocalPort()+"|ReVerfyx Server|"+world.mapName+"|"+count+"|20").getBytes("UTF-8");discovery.send(new DatagramPacket(r,r.length,p.getAddress(),p.getPort()));}catch(IOException ignored){}}
        public void close(){running=false;tick.shutdownNow();try{if(server!=null)server.close();}catch(IOException ignored){}if(discovery!=null)discovery.close();synchronized(sockets){for(Socket s:sockets)try{s.close();}catch(IOException ignored){}sockets.clear();}}
    }
    public static final class Client implements Closeable {
        public volatile Snapshot latest=new Snapshot();public volatile String error="";public volatile float x,z;public volatile boolean jump;private volatile boolean running=true;private volatile Socket socket;
        private boolean resetPending,toolPending;private int pendingTool;
        public synchronized void resetCharacter(){resetPending=true;}
        private synchronized boolean consumeReset(){boolean r=resetPending;resetPending=false;return r;}
        public synchronized void useTool(int tool){pendingTool=Math.max(0,Math.min(2,tool));toolPending=true;}
        private synchronized int consumeTool(){toolPending=false;return pendingTool;}
        private final ArrayDeque<String> messages=new ArrayDeque<String>();
        public synchronized void chat(String s){if(messages.size()<4)messages.add(s.length()>160?s.substring(0,160):s);}
        private synchronized String next(){return messages.isEmpty()?"":messages.removeFirst();}
        public Client(final String address,final int port,final String name,final String language){this(address,port,name,language,new Appearance());}
        public Client(final String address,final int port,final String name,final String language,final Appearance appearance){final Appearance safe=appearance==null?new Appearance():appearance.copy();thread(new Runnable(){public void run(){loop(address,port,name,language,safe);}},"client");}
        private void loop(String address,int port,String name,String language,Appearance appearance){try{socket=new Socket();socket.connect(new InetSocketAddress(address,port),4000);socket.setSoTimeout(5000);socket.setTcpNoDelay(true);DataOutputStream out=new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));DataInputStream in=new DataInputStream(new BufferedInputStream(socket.getInputStream()));out.writeInt(MAGIC);out.writeInt(VERSION);writeText(out,name);writeText(out,language);appearance.write(out);out.flush();if(in.readInt()!=MAGIC)throw new IOException("Protocol mismatch");int id=in.readInt();while(running){long start=System.nanoTime();boolean tool=toolPending;boolean reset=consumeReset();out.writeByte(tool?3:(reset?2:1));out.writeFloat(x);out.writeFloat(z);out.writeBoolean(jump);jump=false;if(tool)out.writeByte(consumeTool());writeText(out,next());out.flush();Snapshot s=readSnapshot(in);s.you=id;latest=s;long wait=50-(System.nanoTime()-start)/1000000;if(wait>0)Thread.sleep(wait);}}catch(Exception e){if(running)error="Disconnected: "+e.getClass().getSimpleName();}finally{close();}}
        public void close(){running=false;try{if(socket!=null)socket.close();}catch(IOException ignored){}}
    }
    public static List<String> search()throws IOException{
        List<String> found=new ArrayList<String>();DatagramSocket s=new DatagramSocket();try{s.setBroadcast(true);s.setSoTimeout(350);byte[] q="CLASSIC_LAN_DISCOVER_3".getBytes("UTF-8");
            List<InetAddress> addresses=new ArrayList<InetAddress>();addresses.add(InetAddress.getByName("255.255.255.255"));addresses.add(InetAddress.getByName("127.0.0.1"));
            Enumeration<NetworkInterface> interfaces=NetworkInterface.getNetworkInterfaces();while(interfaces!=null&&interfaces.hasMoreElements())for(InterfaceAddress a:interfaces.nextElement().getInterfaceAddresses())if(a.getBroadcast()!=null)addresses.add(a.getBroadcast());
            for(InetAddress addr:addresses)try{s.send(new DatagramPacket(q,q.length,addr,DISCOVERY));}catch(IOException ignored){}
            long end=System.currentTimeMillis()+1800;while(System.currentTimeMillis()<end){try{DatagramPacket p=new DatagramPacket(new byte[512],512);s.receive(p);String r=new String(p.getData(),0,p.getLength(),"UTF-8");if(r.startsWith("CLASSIC_LAN_3|")){String v=p.getAddress().getHostAddress()+"|"+r;if(!found.contains(v))found.add(v);}}catch(SocketTimeoutException ignored){}}
        }finally{s.close();}return found;
    }
    private static void writeSnapshot(DataOutputStream o,Snapshot s)throws IOException{o.writeFloat(s.water);writeText(o,s.round);writeText(o,s.map);o.writeByte(s.actors.size());for(Actor a:s.actors){o.writeInt(a.id);writeText(o,a.name);o.writeFloat(a.x);o.writeFloat(a.y);o.writeFloat(a.z);o.writeFloat(a.yaw);o.writeFloat(a.health);o.writeFloat(a.phase);o.writeByte(a.state.ordinal());a.appearance.write(o);o.writeByte(a.tool);}o.writeByte(s.chat.size());for(String c:s.chat)writeText(o,c);}
    private static Snapshot readSnapshot(DataInputStream i)throws IOException{Snapshot s=new Snapshot();s.water=i.readFloat();s.round=readText(i,64);s.map=readText(i,96);int n=i.readUnsignedByte();if(n>20)throw new IOException("Actor limit");for(int j=0;j<n;j++){Actor a=new Actor(i.readInt(),readText(i,96),false);a.x=i.readFloat();a.y=i.readFloat();a.z=i.readFloat();a.yaw=i.readFloat();a.health=i.readFloat();a.phase=i.readFloat();int state=i.readUnsignedByte();if(state>=Actor.State.values().length)throw new IOException("State");a.state=Actor.State.values()[state];a.appearance=Appearance.read(i);a.tool=i.readUnsignedByte();if(a.tool>2)throw new IOException("Tool");s.actors.add(a);}n=i.readUnsignedByte();if(n>8)throw new IOException("Chat limit");for(int j=0;j<n;j++)s.chat.add(readText(i,1024));return s;}
    public static void writeText(DataOutputStream o,String s)throws IOException{byte[] b=s.getBytes("UTF-8");if(b.length>1024)throw new IOException("Text limit");o.writeShort(b.length);o.write(b);}
    public static String readText(DataInputStream i,int max)throws IOException{int n=i.readUnsignedShort();if(n>max)throw new IOException("Text limit");byte[] b=new byte[n];i.readFully(b);return new String(b,"UTF-8");}
    private static void thread(Runnable r,String name){Thread t=new Thread(r,name);t.setDaemon(true);t.start();}
}
