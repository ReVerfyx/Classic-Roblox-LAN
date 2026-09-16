package lan.classic;

import java.io.*;

/** Server/developer-only entry point for user-supplied legal RBXLX files. */
public final class AuthorizedPlaceLoader {
    public static final String ENVIRONMENT_KEY="CLASSIC_ROBLOX_ADMIN_KEY";
    private AuthorizedPlaceLoader(){}
    public static boolean authorized(String key){
        String expected=System.getenv(ENVIRONMENT_KEY);
        if(expected==null||expected.length()<12||key==null)return false;
        if(expected.length()!=key.length())return false;
        int diff=0;for(int i=0;i<expected.length();i++)diff|=expected.charAt(i)^key.charAt(i);return diff==0;
    }
    public static Place read(File file,String key)throws Exception{
        if(!authorized(key))throw new SecurityException("Map import requires the server admin key");
        if(file==null||!file.isFile())throw new FileNotFoundException(String.valueOf(file));
        InputStream in=new BufferedInputStream(new FileInputStream(file));try{return PlaceImporter.read(in);}finally{in.close();}
    }
}
