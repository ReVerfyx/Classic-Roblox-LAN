package lan.classic;
import java.util.*;
/** Portable place plus explicit directed navigation annotations. */
public final class Place {
    public String name="Imported place";
    public final List<Part> parts=new ArrayList<Part>();
    /** Source text collected from trusted Script/LocalScript/ModuleScript items. */
    public final List<String> scripts=new ArrayList<String>();
    public final List<String> scriptNames=new ArrayList<String>();
    public final Navigation navigation=new Navigation();
    public void buildNavigation(){
        navigation.nodes.clear();
        for(Part p:parts)if(p.canCollide&&p.anchored&&p.sx>=3&&p.sz>=3&&!p.kill&&p.transparency<1){navigation.add(p.x,p.y+p.sy/2,p.z,Navigation.Kind.Ground);if(navigation.nodes.size()>=192)break;}
        for(Part p:parts)if(p.ladder&&navigation.nodes.size()<240){
            float z=p.z+p.sz/2+1;int a=navigation.add(p.x,p.y-p.sy/2,z,Navigation.Kind.Ground);int b=navigation.add(p.x,p.y+p.sy/2+.3f,z,Navigation.Kind.Ladder);navigation.edge(a,b);navigation.edge(b,a);
        }
        if(navigation.nodes.isEmpty())navigation.add(0,0,0,Navigation.Kind.Ground);
        for(int a=0;a<navigation.nodes.size();a++)for(int b=0;b<navigation.nodes.size();b++)if(a!=b){Navigation.Node x=navigation.nodes.get(a),y=navigation.nodes.get(b);float horizontal=(float)Math.hypot(x.x-y.x,x.z-y.z),dy=y.y-x.y;if(horizontal<24&&dy<3&&dy>-6&&!navigation.nodes.get(a).edges.contains(b))navigation.edge(a,b);}
    }
}
