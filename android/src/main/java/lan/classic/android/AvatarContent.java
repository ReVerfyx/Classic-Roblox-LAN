package lan.classic.android;

import android.graphics.BitmapFactory;
import java.io.*;
import java.util.zip.*;
import lan.classic.ClassicMesh;

/** Imports a user's local content pack, without fetching or executing remote code. */
final class AvatarContent {
    static final String[] NAMES={"head","torso","leftarm","rightarm","leftleg","rightleg"};
    static void importPack(File archive,File destination) throws IOException {
        if(!destination.mkdirs())throw new IOException("Cannot create avatar folder");
        boolean complete=false;
        try {
            ZipFile zip=new ZipFile(archive);
            try {
                for(int i=0;i<NAMES.length;i++){
                    String name=NAMES[i];String path="content/avatar/"+(i==0?"heads/":"meshes/")+name+".mesh";
                    File target=new File(destination,name+".mesh");copy(zip,path,target);
                    InputStream in=new FileInputStream(target);
                    try{ClassicMesh.read(in);}finally{in.close();}
                }
                File face=new File(destination,"face.png");copy(zip,"content/textures/face.png",face);
                BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeFile(face.getPath(),options);
                if(options.outWidth<=0||options.outHeight<=0||options.outWidth>2048||options.outHeight>2048)throw new IOException("Invalid face texture");
                complete=true;
            }finally{zip.close();}
        }finally{if(!complete)remove(destination);}
    }
    private static void copy(ZipFile zip,String path,File target)throws IOException {
        ZipEntry entry=zip.getEntry(path);if(entry==null)throw new IOException("Missing content: "+path);
        InputStream in=zip.getInputStream(entry);
        try{OutputStream out=new FileOutputStream(target);try{byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}finally{out.close();}}finally{in.close();}
    }
    static void remove(File directory){File[] files=directory.listFiles();if(files!=null)for(File f:files)f.delete();directory.delete();}
}
