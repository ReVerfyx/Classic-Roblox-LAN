package lan.classic;
import java.io.*;
/** One R6 appearance shared by editor, renderer and network. No downloaded assets required. */
public final class Appearance {
    public final int[] colors={0xf5cd30,0x0d69ac,0xf5cd30,0xf5cd30,0xa4bd47,0xa4bd47};
    public int hat,face,shirt,pants;
    /** 0 Classic 2012, 1 Blocky 2013, 2 Cartoon. Kept as a tiny enum so it
     * works on API16 and can be sent with the normal LAN appearance packet. */
    public int animationPack;
    public Appearance copy(){Appearance a=new Appearance();System.arraycopy(colors,0,a.colors,0,6);a.hat=hat;a.face=face;a.shirt=shirt;a.pants=pants;a.animationPack=animationPack;return a;}
    public String encode(){StringBuilder s=new StringBuilder();for(int c:colors)s.append(c).append(',');return s.append(hat).append(',').append(face).append(',').append(shirt).append(',').append(pants).append(',').append(animationPack).toString();}
    public static Appearance decode(String s){Appearance a=new Appearance();try{String[] p=s.split(",");if(p.length<10)return a;for(int i=0;i<6;i++)a.colors[i]=Integer.parseInt(p[i])&0xffffff;a.hat=limit(Integer.parseInt(p[6]),3);a.face=limit(Integer.parseInt(p[7]),2);a.shirt=limit(Integer.parseInt(p[8]),3);a.pants=limit(Integer.parseInt(p[9]),2);a.animationPack=p.length>10?limit(Integer.parseInt(p[10]),2):0;}catch(RuntimeException ignored){}return a;}
    private static int limit(int x,int max){return Math.max(0,Math.min(max,x));}
    public void write(DataOutputStream o)throws IOException{for(int c:colors)o.writeInt(c&0xffffff);o.writeByte(hat);o.writeByte(face);o.writeByte(shirt);o.writeByte(pants);o.writeByte(animationPack);}
    public static Appearance read(DataInputStream i)throws IOException{Appearance a=new Appearance();for(int n=0;n<6;n++)a.colors[n]=i.readInt()&0xffffff;a.hat=i.readUnsignedByte();a.face=i.readUnsignedByte();a.shirt=i.readUnsignedByte();a.pants=i.readUnsignedByte();a.animationPack=i.readUnsignedByte();if(a.hat>3||a.face>2||a.shirt>3||a.pants>2||a.animationPack>2)throw new IOException("Appearance out of range");return a;}
}
