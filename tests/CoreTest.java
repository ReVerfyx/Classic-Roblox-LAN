import lan.classic.*;
import java.io.*;
public class CoreTest {
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    public static void main(String[] args)throws Exception{
        World w=new World();Actor b=w.add("Builder",true,"ru");
        for(int i=0;i<30*14;i++)w.tick(1f/30);
        check(b.y>14&&b.z<-18,"Bot must climb and exit onto platform: "+b.y+" "+b.z);
        Actor p=w.add("Tester",false,"ru");for(int i=0;i<30;i++)w.tick(1f/30);float y=p.y;w.input(p.id,0,0,true);w.tick(1f/30);check(p.y>y,"Jump");
        w.message(p.id,"иди за мной");check(b.followId==p.id,"Follow intent");check(b.memory.size()>0,"Memory");
        for(int i=0;i<30*56;i++)w.tick(1f/30);check(b.health>0,"Bot survives flood");
        check(w.nav.path(0,3).size()==4,"Ladder route");
        String xml="<roblox><Item class=\"Part\"><Properties><string name=\"Name\">Spawn</string><Vector3 name=\"size\"><X>8</X><Y>1</Y><Z>8</Z></Vector3><CoordinateFrame name=\"CFrame\"><X>0</X><Y>0</Y><Z>0</Z></CoordinateFrame><bool name=\"Anchored\">true</bool></Properties></Item><Item class=\"Script\"><Properties><string name=\"Name\">RoundScript</string><string name=\"Source\">print('hello')\ngame:ChangeDisaster('TSUNAMI')</string></Properties></Item></roblox>";
        Place imported=PlaceImporter.read(new ByteArrayInputStream(xml.getBytes("UTF-8")));check(imported.parts.size()==1&&imported.scripts.size()==1,"RBXLX Part and Script import");GameConfig trusted=new GameConfig();trusted.trustedScripts=true;World scripted=new World(trusted);scripted.loadPlace(imported);check("TSUNAMI".equals(trusted.forcedDisaster)&&!scripted.chat.isEmpty(),"Trusted Lua subset");
        Net.Host host=new Net.Host(new World(),true,Net.PORT);Net.Client c=null,d=null;
        try{
            check(!Net.search().isEmpty(),"UDP discovery");c=new Net.Client("127.0.0.1",Net.PORT,"Tester","ru");d=new Net.Client("127.0.0.1",Net.PORT,"Guest","en");
            long end=System.currentTimeMillis()+4000;while(c.latest.actors.size()<2&&System.currentTimeMillis()<end)Thread.sleep(20);
            check(c.latest.actors.size()==2,"Two LAN clients: "+c.error);c.x=1;Thread.sleep(400);check(host.world.actors.size()==2,"Both connected");
            c.chat("привет");Thread.sleep(200);check(!d.latest.chat.isEmpty(),"Replicated chat");
            c.resetCharacter();Thread.sleep(200);boolean dead=false;for(Actor a:c.latest.actors)if(a.id==c.latest.you)dead=a.health<=0;check(dead,"Server-authoritative reset");
        }finally{if(c!=null)c.close();if(d!=null)d.close();host.close();}
        System.out.println("PASS: climb, platform, jump, flood survival, intent, memory, graph, discovery, two peers, chat, network reset");
    }
}
