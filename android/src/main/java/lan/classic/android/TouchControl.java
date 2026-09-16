package lan.classic.android;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;

/** Independent pointer ownership allows joystick + jump + camera at the same time. */
final class TouchControl extends View {
    interface Listener { void move(float x,float z); void jump(); }
    private final Listener listener;
    private final boolean jump;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path arrow=new Path();
    private float dx,dz;
    private int pointer=-1;
    TouchControl(Context context,boolean jump,Listener listener) {
        super(context);this.jump=jump;this.listener=listener;
        setContentDescription(jump?"Jump":"Movement joystick");
        setFocusable(true);
    }
    protected void onDraw(Canvas c) {
        float cx=getWidth()*.5f,cy=getHeight()*.5f,r=Math.min(cx,cy)*.83f;
        paint.setStyle(Paint.Style.FILL);paint.setColor(pointer<0?0x33404f55:0x55404f55);c.drawCircle(cx,cy,r,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(r*.07f);paint.setColor(0x998da7b2);c.drawCircle(cx,cy,r,paint);
        paint.setStrokeWidth(r*.025f);paint.setColor(0xbbd9e4e9);c.drawCircle(cx,cy,r*.92f,paint);
        paint.setStyle(Paint.Style.FILL);
        if(jump) {
            paint.setColor(0xcceaf0f2);arrow.reset();arrow.moveTo(cx,cy-r*.5f);arrow.lineTo(cx+r*.43f,cy);
            arrow.lineTo(cx+r*.16f,cy);arrow.lineTo(cx+r*.16f,cy+r*.38f);arrow.lineTo(cx-r*.16f,cy+r*.38f);
            arrow.lineTo(cx-r*.16f,cy);arrow.lineTo(cx-r*.43f,cy);arrow.close();c.drawPath(arrow,paint);
        } else {
            float x=cx+dx*r*.5f,y=cy+dz*r*.5f;
            paint.setColor(0x664e788d);c.drawCircle(x,y,r*.43f,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(r*.045f);paint.setColor(0xaaadc9d6);c.drawCircle(x,y,r*.43f,paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }
    public boolean onTouchEvent(MotionEvent e) {
        int action=e.getActionMasked();
        if(action==MotionEvent.ACTION_DOWN) {
            pointer=e.getPointerId(0);getParent().requestDisallowInterceptTouchEvent(true);
            if(jump)listener.jump();else update(e,0);
        } else if(action==MotionEvent.ACTION_MOVE && !jump) {
            int i=e.findPointerIndex(pointer);if(i>=0)update(e,i);
        } else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_CANCEL ||
                (action==MotionEvent.ACTION_POINTER_UP && e.getPointerId(e.getActionIndex())==pointer)) {
            reset();if(action==MotionEvent.ACTION_UP)performClick();
        }
        invalidate();return true;
    }
    private void update(MotionEvent e,int i) {
        float radius=Math.min(getWidth(),getHeight())*.32f;
        dx=(e.getX(i)-getWidth()*.5f)/Math.max(1,radius);
        dz=(e.getY(i)-getHeight()*.5f)/Math.max(1,radius);
        float length=(float)Math.sqrt(dx*dx+dz*dz);
        if(length<.12f){dx=dz=0;}else if(length>1){dx/=length;dz/=length;}
        listener.move(dx,dz);
    }
    void reset(){pointer=-1;dx=dz=0;if(!jump)listener.move(0,0);invalidate();}
    public boolean performClick(){super.performClick();return true;}
    protected void onDetachedFromWindow(){reset();super.onDetachedFromWindow();}
}
