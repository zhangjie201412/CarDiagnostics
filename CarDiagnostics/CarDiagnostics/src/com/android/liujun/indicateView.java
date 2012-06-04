package com.android.liujun;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class indicateView extends View{

	private Bitmap[] mBitmapArray = new Bitmap[4];
	InputStream is;
	int[] mBitmapWidth = new int[4];
	int[] mBitmapHeight = new int[4];
	private int mValue;
	
	
	public indicateView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public indicateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mValue = -90;
		//////////////////////////////
		//////////////////////////////
		BitmapFactory.Options opts = new Options();
		opts.inJustDecodeBounds = false;
		setBitMapArray(context, 0, opts, R.drawable.pl);
		setBitMapArray(context, 1, opts, R.drawable.point);
	}
	
	private void setBitMapArray(Context context, int index,
			BitmapFactory.Options opts, int resid) {

		is = context.getResources().openRawResource(resid);
		mBitmapArray[index] = BitmapFactory.decodeStream(is);
		mBitmapWidth[index] = mBitmapArray[index].getWidth();
		mBitmapHeight[index] = mBitmapArray[index].getHeight();
		mBitmapArray[index+2] = BitmapFactory.decodeStream(is, null, opts);
		mBitmapHeight[index+2] = mBitmapArray[index+2].getHeight();
		mBitmapWidth[index+2] = mBitmapArray[index+2].getWidth();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setAntiAlias(true);

		int w = canvas.getWidth();
		int h = canvas.getHeight();
		
		int cx = w/2;
		int cy = h/2;
//		System.out.println("w:"+w+"\nh"+h+"\nx:"+cx+"\ny"+cy+"\n");

		canvas.translate(cx, cy);
		drawPictures(canvas, 0, mValue);
	}
	
   private void drawPictures(Canvas canvas, int idDelta, int value) {
	
	//canvas.rotate(-value);
	canvas.drawBitmap(mBitmapArray[0 + idDelta],
	        -mBitmapWidth[0 + idDelta] / 2,
	        -mBitmapHeight[0 + idDelta] / 2, null);
	
	    canvas.rotate(mValue);
	    canvas.drawBitmap(mBitmapArray[1 + idDelta],
	            -mBitmapWidth[1 + idDelta] / 2,
	            -mBitmapHeight[1 + idDelta] / 2, null);
	
   }
   public void setValue(int value){
	   mValue = value;
   }
   
   public int getVaule() {
	   return mValue;
   }
   

}
