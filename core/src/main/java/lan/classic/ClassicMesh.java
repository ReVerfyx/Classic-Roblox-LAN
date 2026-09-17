package lan.classic;

import java.io.*;
import java.nio.*;

/** Reader for the version 2.00 mesh records found in the 2017 local content pack. */
public final class ClassicMesh {
    public final float[] positions, normals;
    public final int vertexCount;
    private ClassicMesh(float[] p,float[] n){positions=p;normals=n;vertexCount=p.length/3;}
    public static ClassicMesh read(InputStream in) throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] block=new byte[8192];int count;
        while((count=in.read(block))!=-1)out.write(block,0,count);
        byte[] data=out.toByteArray();byte[] signature="version 2.00\n".getBytes("US-ASCII");
        if(data.length<signature.length+12)throw new IOException("Truncated mesh");
        for(int i=0;i<signature.length;i++)if(data[i]!=signature[i])throw new IOException("Expected mesh version 2.00");
        ByteBuffer b=ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);int start=signature.length;
        int header=b.getShort(start)&65535,stride=b.get(start+2)&255,faceStride=b.get(start+3)&255;
        int vertices=b.getInt(start+4),faces=b.getInt(start+8);
        long faceOffset=(long)start+header+(long)vertices*stride;
        if(header<12||stride<32||faceStride<12||vertices<1||faces<1||faceOffset<0||faceOffset+(long)faces*faceStride>data.length||faces>Integer.MAX_VALUE/9)
            throw new IOException("Invalid mesh layout");
        float[] p=new float[faces*9],n=new float[faces*9];
        for(int f=0;f<faces;f++)for(int k=0;k<3;k++){
            int index=b.getInt((int)faceOffset+f*faceStride+k*4);
            if(index<0||index>=vertices)throw new IOException("Invalid mesh vertex index");
            int source=start+header+index*stride,target=f*9+k*3;
            for(int axis=0;axis<3;axis++){
                float position=b.getFloat(source+axis*4),normal=b.getFloat(source+12+axis*4);
                if(Float.isNaN(position)||Float.isInfinite(position)||Float.isNaN(normal)||Float.isInfinite(normal))throw new IOException("Non-finite mesh vertex");
                p[target+axis]=position;n[target+axis]=normal;
            }
        }
        return new ClassicMesh(p,n);
    }
}
