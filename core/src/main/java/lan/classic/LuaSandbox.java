package lan.classic;

import java.util.*;

/**
 * A deliberately small, deterministic Lua-like script surface for imported places.
 *
 * This is not a drop-in Roblox Luau runtime.  It accepts the harmless subset that
 * can be mapped to this offline engine (print, wait, ChangeDisaster and a few
 * workspace helpers).  File, process, socket, Java reflection and unbounded loop
 * access are rejected before execution.  The instruction budget is shared by the
 * whole script so a trusted map cannot stall the 30 Hz simulation.
 */
public final class LuaSandbox {
    private LuaSandbox() {}

    public static final class Result {
        public final boolean success;
        public final int instructions;
        public final List<String> output;
        public final String error;
        Result(boolean ok,int count,List<String> lines,String error){success=ok;instructions=count;output=lines;this.error=error;}
    }

    public static Result run(World world,String source,int budget){
        if(world==null)throw new IllegalArgumentException("world");
        if(source==null)source="";
        if(source.length()>262144)return new Result(false,0,new ArrayList<String>(),"Lua source exceeds 256 KiB");
        budget=Math.max(32,Math.min(100000,budget));
        String lower=source.toLowerCase(Locale.ROOT);
        String[] forbidden={"io.","os.","debug.","package.","require(","luajava","dofile(","loadfile(","socket","http","java.","while ","repeat","goto "};
        for(String token:forbidden)if(lower.indexOf(token)>=0)return new Result(false,0,new ArrayList<String>(),"Lua API is not allowed: "+token);
        List<String> output=new ArrayList<String>();int used=0;
        String[] lines=source.replace('\r','\n').split("\\n");
        try{
            for(String raw:lines){
                String line=stripComment(raw).trim();if(line.length()==0)continue;
                if(++used>budget)throw new IllegalStateException("instruction budget exceeded");
                execute(world,line,output);
            }
            return new Result(true,used,output,"");
        }catch(RuntimeException e){return new Result(false,used,output,e.getMessage()==null?e.getClass().getSimpleName():e.getMessage());}
    }

    public static List<Result> runPlace(World world,Place place,int budget){
        List<Result> results=new ArrayList<Result>();if(place==null)return results;
        for(String source:place.scripts)results.add(run(world,source,budget));return results;
    }

    private static String stripComment(String s){
        boolean quote=false;char q=0;
        for(int i=0;i+1<s.length();i++){char c=s.charAt(i);if((c=='\''||c=='\"')){if(!quote){quote=true;q=c;}else if(q==c)quote=false;}if(!quote&&c=='-'&&s.charAt(i+1)=='-')return s.substring(0,i);}
        return s;
    }
    private static void execute(World w,String line,List<String> output){
        if(line.equals("wait()")||line.matches("wait\\s*\\(\\s*[0-9.]+\\s*\\)"))return;
        if(line.startsWith("print")){String v=argument(line,"print");if(v.length()>160)v=v.substring(0,160);output.add(v);w.say("Script",v);return;}
        if(line.startsWith("game:GetService")||line.startsWith("game.GetService"))return;
        if(line.matches("(?i).*ChangeDisaster\\s*\\(.*")){String v=firstStringArgument(line);if(v.length()>0)w.config.forcedDisaster=v;return;}
        if(line.matches("(?i).*SetAttribute\\s*\\(.*"))return;
        if(line.matches("(?i).*spawnPart\\s*\\(.*")){spawnPart(w,line);return;}
        if(line.matches("(?i)^(local\\s+)?[A-Za-z_][A-Za-z0-9_]*\\s*=.*"))return;
        if(line.equals("return")||line.startsWith("return "))return;
        throw new IllegalArgumentException("Unsupported Lua statement");
    }
    private static String argument(String line,String name){int a=line.indexOf('('),b=line.lastIndexOf(')');if(a<0||b<=a)return "";String v=line.substring(a+1,b).trim();if((v.startsWith("\"")&&v.endsWith("\""))||(v.startsWith("'")&&v.endsWith("'")))return v.substring(1,v.length()-1);return v;}
    private static String firstStringArgument(String line){String v=argument(line,line.substring(0,Math.max(0,line.indexOf('('))));int comma=v.indexOf(',');return (comma<0?v:v.substring(0,comma)).trim().replace("\"","").replace("'","");}
    private static void spawnPart(World w,String line){
        String v=argument(line,"spawnPart");String[] p=v.split(",");if(p.length<6)throw new IllegalArgumentException("spawnPart needs x,y,z,sx,sy,sz");
        float x=num(p[0]),y=num(p[1]),z=num(p[2]),sx=Math.max(.1f,Math.min(64,num(p[3]))),sy=Math.max(.1f,Math.min(64,num(p[4]))),sz=Math.max(.1f,Math.min(64,num(p[5])));
        Part part=new Part(x,y,z,sx,sy,sz,p.length>6?color(p[6]):0x96999f);part.anchored=true;part.originX=x;part.originY=y;part.originZ=z;w.parts.add(part);
    }
    private static float num(String s){float f=Float.parseFloat(s.trim());if(Float.isNaN(f)||Float.isInfinite(f)||Math.abs(f)>10000)throw new IllegalArgumentException("number out of range");return f;}
    private static int color(String s){String v=s.trim();if(v.startsWith("0x")||v.startsWith("0X"))return (int)Long.parseLong(v.substring(2),16)&0xffffff;return (int)num(v)&0xffffff;}
}
