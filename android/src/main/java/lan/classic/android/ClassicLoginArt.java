package lan.classic.android;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.Random;

/** Original vector artwork based on the early mobile client's composition. No web assets. */
final class ClassicLoginArt extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final boolean logo;
    ClassicLoginArt(Context context, boolean logo) {
        super(context);
        this.logo = logo;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (logo) { drawLogo(canvas, w, h); return; }
        paint.setShader(new LinearGradient(0, 0, 0, h, new int[]{0xff169fd9, 0xff81def1, 0xff247923}, null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        float cx=w*.5f, cy=h*.33f, radius=Math.max(w,h)*2;
        paint.setColor(0x22ffffff);
        for (int i=0;i<22;i++) {
            double a=i*Math.PI/11, b=a+.075;
            path.reset();path.moveTo(cx,cy);
            path.lineTo(cx+(float)Math.cos(a)*radius,cy+(float)Math.sin(a)*radius);
            path.lineTo(cx+(float)Math.cos(b)*radius,cy+(float)Math.sin(b)*radius);
            path.close();canvas.drawPath(path,paint);
        }
        // Miniature skyline at the sides, leaving the form clear.
        Random random=new Random(2012);
        float horizon=h*.70f;
        for (int side=0;side<2;side++) for(int i=0;i<7;i++) {
            float bw=w*.024f, bh=h*(.05f+random.nextFloat()*.11f);
            float bx=side==0?i*bw: w-(i+1)*bw;
            paint.setColor(i%2==0?0xffc4d9df:0xffedf5f5);
            canvas.drawRect(bx,horizon-bh,bx+bw*.8f,horizon,paint);
            paint.setColor(0xff7f9fa8);
            for(float yy=horizon-bh+4;yy<horizon;yy+=7) canvas.drawRect(bx+bw*.2f,yy,bx+bw*.55f,yy+2,paint);
        }
        path.reset();path.moveTo(0,h*.73f);path.quadTo(w*.5f,h*.53f,w,h*.73f);path.lineTo(w,h);path.lineTo(0,h);path.close();
        paint.setShader(new LinearGradient(0,h*.62f,0,h,0xff68a42a,0xff16370b,Shader.TileMode.CLAMP));
        canvas.drawPath(path,paint);paint.setShader(null);
        canvas.save();canvas.clipPath(path);paint.setStrokeWidth(Math.max(1,w/550));
        for(int i=0;i<2100;i++) {
            float x=random.nextFloat()*w,y=h*.6f+random.nextFloat()*h*.4f;
            paint.setColor(i%3==0?0x776fab37:i%3==1?0x88618f28:0x88406620);
            canvas.drawLine(x,y,x+random.nextFloat()*5-2,y-3-random.nextFloat()*9,paint);
        }
        canvas.restore();
    }
    private void drawLogo(Canvas c,float w,float h) {
        paint.setTypeface(Typeface.create(Typeface.SANS_SERIF,Typeface.BOLD));
        paint.setTextSize(w*.22f);paint.setTextAlign(Paint.Align.LEFT);paint.setStrokeJoin(Paint.Join.ROUND);
        float measure=paint.measureText("ROBLOX");
        float scale=(w*.91f)/measure;
        c.save();c.translate(w*.045f,h*.74f);c.scale(scale,scale);
        float x=0;String letters="ROBLOX";float[] angles={-8,4,-5,3,-5,9};
        for(int i=0;i<letters.length();i++) {
            String letter=letters.substring(i,i+1);float advance=paint.measureText(letter);
            c.save();c.translate(x,0);c.rotate(angles[i],advance*.5f,-paint.getTextSize()*.35f);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(w*.018f);paint.setColor(0xff861918);c.drawText(letter,2,4,paint);
            paint.setStrokeWidth(w*.013f);paint.setColor(0xffd62327);c.drawText(letter,0,0,paint);
            paint.setStyle(Paint.Style.FILL);paint.setColor(Color.WHITE);c.drawText(letter,0,0,paint);
            c.restore();x+=advance;
        }
        c.restore();paint.setStyle(Paint.Style.FILL);
    }
}
