package lan.classic;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
/** Bounded UTF-8 RBXLX static Part importer. Scripts, meshes and external URLs are not executed. */
public final class PlaceImporter {
    public static Place read(InputStream stream)throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;
        while((n=stream.read(buf))!=-1){bytes.write(buf,0,n);}
        if(bytes.size()>7&&new String(bytes.toByteArray(),0,7,"UTF-8").equals("<roblox")&&bytes.toByteArray()[7]=='!')throw new IOException("Binary RBXL is not supported yet. Export as XML (.rbxlx) in Studio.");
        String xml=new String(bytes.toByteArray(),"UTF-8");String upper=xml.toUpperCase(Locale.ROOT);
        if(xml.indexOf('\0')>=0||upper.contains("<!DOCTYPE")||upper.contains("<!ENTITY"))throw new IOException("Only UTF-8 XML without DTD/entities is accepted");
        final Place out=new Place();SAXParserFactory factory=SAXParserFactory.newInstance();factory.setNamespaceAware(false);
        SAXParser parser=factory.newSAXParser();final List<Entry> stack=new ArrayList<Entry>();
        parser.parse(new ByteArrayInputStream(bytes.toByteArray()),new DefaultHandler(){
            int depth;String property="",leaf="";StringBuilder text=new StringBuilder();Map<String,Float> values=new HashMap<String,Float>();
            public InputSource resolveEntity(String a,String b)throws SAXException{throw new SAXException("External entities disabled");}
            public void startElement(String uri,String local,String name,Attributes attrs)throws SAXException{
                ++depth;text.setLength(0);leaf=name;
                if(name.equals("Item")){stack.add(new Entry(attrs.getValue("class")));}
                String p=attrs.getValue("name");if(p!=null){property=p;values.clear();}
            }
            public void characters(char[] ch,int start,int len)throws SAXException{text.append(ch,start,len);}
            public void endElement(String uri,String local,String name)throws SAXException{
                depth--;if(name.equals("Item")){Entry e=stack.remove(stack.size()-1);if(e.part!=null){e.part.originX=e.part.x;e.part.originY=e.part.y;e.part.originZ=e.part.z;out.parts.add(e.part);}if(e.source!=null){out.scripts.add(e.source);out.scriptNames.add(e.name==null?e.className:e.name);}return;}
                if(stack.isEmpty())return;Entry current=stack.get(stack.size()-1);String s=text.toString().trim();
                if((name.equals("string")||name.equals("ProtectedString"))&&property.equals("Source")&&current.isScript()){current.source=s;return;}
                if(name.equals("string")&&property.equals("Name")){current.name=s;return;}
                if(current.part==null)return;Part p=current.part;
                try{
                    if(name.equals("X")||name.equals("Y")||name.equals("Z")||name.matches("R[012][012]")||name.equals("R")||name.equals("G")||name.equals("B")){float v=Float.parseFloat(s);if(Float.isNaN(v)||Float.isInfinite(v))throw new NumberFormatException();values.put(name,v);}
                    if(name.equals("Vector3")&&property.equalsIgnoreCase("size")){p.sx=Math.max(.001f,get("X"));p.sy=Math.max(.001f,get("Y"));p.sz=Math.max(.001f,get("Z"));}
                    if(name.equals("CoordinateFrame")){p.x=get("X");p.y=get("Y");p.z=get("Z");p.ry=(float)Math.toDegrees(Math.atan2(get("R02"),get("R22")));p.rx=(float)Math.toDegrees(Math.asin(-clamp(get("R12"),-1,1)));p.rz=(float)Math.toDegrees(Math.atan2(get("R10"),get("R11")));}
                    if(name.equals("Color3"))p.color=((int)(clamp(get("R"),0,1)*255)<<16)|((int)(clamp(get("G"),0,1)*255)<<8)|(int)(clamp(get("B"),0,1)*255);
                    if(name.equals("Color3uint8"))p.color=(int)Long.parseLong(s)&0xffffff;
                    if(name.equals("bool")){if(property.equals("Anchored"))p.anchored=Boolean.parseBoolean(s);if(property.equals("CanCollide"))p.canCollide=Boolean.parseBoolean(s);}
                    if(name.equals("float")&&property.equals("Transparency"))p.transparency=clamp(Float.parseFloat(s),0,1);
                    if(name.equals("token")&&property.equals("shape")){int k=Integer.parseInt(s);p.shape=k==0?Part.Shape.Ball:k==2?Part.Shape.Cylinder:Part.Shape.Block;}
                    if(name.equals("token")&&property.equals("Material")){int k=Integer.parseInt(s);p.material=k==256?Part.Material.Plastic:k==272?Part.Material.SmoothPlastic:k==512?Part.Material.Wood:k==1040?Part.Material.Brick:k==816?Part.Material.Concrete:k==1088?Part.Material.Metal:k==1280?Part.Material.Grass:k==1296?Part.Material.Sand:Part.Material.Slate;}
                    if(name.equals("token")&&property.equals("TopSurface")){int k=Integer.parseInt(s);p.surface=k==3?Part.Surface.Studs:k==4?Part.Surface.Inlets:Part.Surface.Smooth;}
                }catch(NumberFormatException e){throw new SAXException("Invalid numeric Part property");}
            }
            float get(String name){Float v=values.get(name);return v==null?0:v;}
        });
        if(out.parts.isEmpty())throw new IOException("No supported Parts in this place");out.buildNavigation();return out;
    }
    private static float clamp(float f,float lo,float hi){return Math.max(lo,Math.min(hi,f));}
    private static final class Entry {
        final String className;Part part;String name,source;
        Entry(String cls){className=cls==null?"":cls;if("Part".equals(cls)||"WedgePart".equals(cls)||"TrussPart".equals(cls)||"SpawnLocation".equals(cls)||"Seat".equals(cls)){part=new Part(0,0,0,4,1,2,0x96999f);part.shape="WedgePart".equals(cls)?Part.Shape.Wedge:Part.Shape.Block;part.ladder="TrussPart".equals(cls);part.canCollide=!part.ladder;part.spawn="SpawnLocation".equals(cls);part.seat="Seat".equals(cls);}}
        boolean isScript(){return "Script".equals(className)||"LocalScript".equals(className)||"ModuleScript".equals(className);}
    }
}
