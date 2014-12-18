package com.npi.blureffect;

import java.io.File;
import java.io.FileNotFoundException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	private static final String BLURRED_IMG_PATH = "blurred_imageadjgk.png";
	private static final int TOP_HEIGHT = 700;
	private ListView mList;
	private ImageView mBlurredImage;
	private Button btn;
	private View headerView;
	private ImageView mNormalImage;
	private ScrollableImageView mBlurredImageHeader;
	private Switch mSwitch;
	private float alpha;
	
	//take img from gallery 
	
	final int RQS_IMAGE1 = 1;
	 Button btnLoadImage;
	 ImageView imageResult;


	 Uri source;
	 Bitmap bitmapMaster;
	 Canvas canvasMaster;
	 
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);

		// Get the screen width

		// Find the view
		mBlurredImage = (ImageView) findViewById(R.id.blurred_image);
		mNormalImage = (ImageView) findViewById(R.id.normal_image);
		mBlurredImageHeader = (ScrollableImageView) findViewById(R.id.blurred_image_header);
		btn = (Button)findViewById(R.id.btn_pick_img);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);//
				startActivityForResult(Intent.createChooser(intent, "Select Picture"),RQS_IMAGE1);
			}
		});
		

		// prepare the header ScrollableImageView
		
		
	}
	@Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
		final int screenWidth = ImageUtils.getScreenWidth(this);
		mNormalImage.setDrawingCacheEnabled(true);
		mBlurredImage.setDrawingCacheEnabled(true);
		
	  if (resultCode == RESULT_OK) {
	   switch (requestCode) {
	   case RQS_IMAGE1:
	    source = data.getData();
	    try {
	     bitmapMaster = BitmapFactory
	       .decodeStream(getContentResolver().openInputStream(
	         source));
	     mNormalImage.setImageBitmap(bitmapMaster);
	     mBlurredImageHeader.setScreenWidth(screenWidth);
			mBlurredImage.setAlpha(alpha);
			// Try to find the blurred image
			final File blurredImage = new File(getFilesDir() + BLURRED_IMG_PATH);
			if (!blurredImage.exists()) {
				
 				// launch the progressbar in ActionBar
				setProgressBarIndeterminateVisibility(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						// No image found => let's generate it!
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize = 2;
						//Bitmap image = BitmapFactory.decodeResource(getResources(), bitmapMaster, options);
						Bitmap newImg = Blur.fastblur(MainActivity.this, bitmapMaster, 12);
						ImageUtils.storeImage(newImg, blurredImage);
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								updateView(screenWidth);
								// And finally stop the progressbar
								setProgressBarIndeterminateVisibility(false);
							}
						});

					}
				}).start();

			} else {

				// The image has been found. Let's update the view
				updateView(screenWidth);

			}

			String[] strings = getResources().getStringArray(R.array.list_content);

			// Prepare the header view for our list
			headerView = new View(this);
			headerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, TOP_HEIGHT));
			alpha = (float) -headerView.getTop() / (float) TOP_HEIGHT;
			// Apply a ceil
			if (alpha > 1) {
				alpha = 1;
			}

			// Apply on the ImageView if needed
//			if (mSwitch.isChecked()) {
//				mBlurredImage.setAlpha(alpha);
//			}

			// Parallax effect : we apply half the scroll amount to our
			// three views
			mBlurredImage.setTop(headerView.getTop() / 2);
			mNormalImage.setTop(headerView.getTop() / 2);
			mBlurredImageHeader.handleScroll(headerView.getTop() / 2);
	     
	    } catch (FileNotFoundException e) {
	     // TODO Auto-generated catch block
	     e.printStackTrace();
	    }

	    break;
	   }
	
	  }
		mNormalImage.setDrawingCacheEnabled(false);
		mBlurredImage.setDrawingCacheEnabled(false);
		}
	private void updateView(final int screenWidth) {
		mNormalImage.setDrawingCacheEnabled(true);
		mBlurredImage.setDrawingCacheEnabled(true);

		Bitmap bmpBlurred = BitmapFactory.decodeFile(getFilesDir() + BLURRED_IMG_PATH);
		bmpBlurred = Bitmap.createScaledBitmap(bmpBlurred, screenWidth, (int) (bmpBlurred.getHeight()
				* ((float) screenWidth) / (float) bmpBlurred.getWidth()), false);

		mBlurredImage.setImageBitmap(bmpBlurred);
		mBlurredImageHeader.setoriginalImage(bmpBlurred);
		mNormalImage.setDrawingCacheEnabled(false);
		mBlurredImage.setDrawingCacheEnabled(false);


	}
}
