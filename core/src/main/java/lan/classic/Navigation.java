package lan.classic;
import java.util.*;
/** Authored traversal graph. Directed edges preserve ladder and drop semantics. */
public final class Navigation {
    public enum Kind { Ground, Jump, Ladder, Platform, Drop, Danger, Door, Bridge }
    public static final class Node {
        public final float x,y,z; public final Kind kind; public final List<Integer> edges=new ArrayList<Integer>();
        Node(float x,float y,float z,Kind k){this.x=x;this.y=y;this.z=z;kind=k;}
    }
    public final List<Node> nodes=new ArrayList<Node>();
    public int add(float x,float y,float z,Kind k){nodes.add(new Node(x,y,z,k));return nodes.size()-1;}
    public void edge(int a,int b){nodes.get(a).edges.add(b);}
    public List<Integer> path(int start,int goal) {
        float[] dist=new float[nodes.size()];int[] prev=new int[nodes.size()];boolean[] used=new boolean[nodes.size()];
        Arrays.fill(dist,Float.MAX_VALUE);Arrays.fill(prev,-1);dist[start]=0;
        for(int step=0;step<nodes.size();step++){
            int u=-1;for(int i=0;i<dist.length;i++)if(!used[i]&&(u<0||dist[i]<dist[u]))u=i;
            if(u<0||dist[u]==Float.MAX_VALUE)break;if(u==goal)break;used[u]=true;
            Node a=nodes.get(u);
            for(int v:a.edges){Node b=nodes.get(v);float cost=(float)Math.sqrt(sq(a.x-b.x)+sq(a.y-b.y)+sq(a.z-b.z));
                if(b.kind==Kind.Danger)cost+=1000;
                if(dist[u]+cost<dist[v]){dist[v]=dist[u]+cost;prev[v]=u;}}
        }
        List<Integer> out=new ArrayList<Integer>();if(start!=goal&&prev[goal]<0)return out;
        for(int n=goal;n!=-1;n=prev[n])out.add(0,n);return out;
    }
    public int nearest(float x,float y,float z){int best=0;float d=Float.MAX_VALUE;for(int i=0;i<nodes.size();i++){Node n=nodes.get(i);float t=sq(x-n.x)+sq(y-n.y)+sq(z-n.z);if(t<d){d=t;best=i;}}return best;}
    private static float sq(float x){return x*x;}
}
