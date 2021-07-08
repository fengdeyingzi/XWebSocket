package com.xl.view;

import java.io.IOException;

import com.xl.graphics.Bitmap;
import com.xl.graphics.BitmapFactory;
import com.xl.graphics.Canvas;
import com.xl.graphics.Paint;
import com.xl.graphics.Rect;

import android.content.Context;
import android.view.View;

public class TestView extends View{

	public TestView(Context context) {
		super(context);
		initView();
	}
	
	private void initView(){
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		try {
			Bitmap bitmap = BitmapFactory.decodeStream(getContext().getResources().getAssets().open("icon.png"));
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			Rect src = new Rect(0,0,140,140);
			Rect dst = new Rect(0,0,320,320);
			canvas.drawBitmap(bitmap, src, dst, paint);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		super.onDraw(canvas);
		
	}

}
