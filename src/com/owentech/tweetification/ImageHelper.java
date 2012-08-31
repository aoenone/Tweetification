package com.owentech.tweetification;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

public class ImageHelper
{

	private static final String TAG = "Tweetification";

	public static String path = Environment.getExternalStorageDirectory()
			.toString() + "/data/com.owentech.tweetification/";

	/*******************************************************/
	/* Method to resize bitmap (too small in notification) */
	/*******************************************************/
	public Bitmap resizeBitmap(Bitmap bm, int newHeight, int newWidth)
			throws Exception
	{

		Log.i(TAG, "Running resizeBitmp");

		int width = bm.getWidth();
		int height = bm.getHeight();

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();

		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, false);

		return resizedBitmap;

	}

	/**************************************************************/
	/* Method to download and save jpg avatar to external storage */
	/**************************************************************/
	public Bitmap downloadImageFromUrl(String imageURL, String fileName)
			throws Exception
	{

		Bitmap b = null;

		// Connect to the URL
		URL myImageURL = new URL(imageURL);
		HttpURLConnection connection = (HttpURLConnection) myImageURL
				.openConnection();
		connection.setDoInput(true);
		connection.connect();
		InputStream input = connection.getInputStream();

		// Get the bitmap
		b = BitmapFactory.decodeStream(input);

		// Save the bitmap to the file
		Log.i(TAG, path);
		OutputStream fOut = null;
		File folder = new File(path);
		File file = new File(path, fileName);
		folder.mkdirs();
		fOut = new FileOutputStream(file);

		b.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
		fOut.flush();
		fOut.close();

		Log.i(TAG, "Saved " + fileName + " to SD");

		return b;
	}

	/***************************************/
	/* Method to get bitmap from local jpg */
	/***************************************/
	public Bitmap getLocal(String username, Boolean resize)
	{
		Bitmap b = null;

		try
		{
			b = BitmapFactory.decodeFile(path + username + ".jpg");
			if(resize)
			{
				b = resizeBitmap(b, b.getHeight() * 2, b.getWidth() * 2);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return b;
	}

	/*********************************************************/
	/* Method to check if the avatar jpg file exists locally */
	/*********************************************************/
	public boolean avatarExists(String username)
	{
		File file = new File(path + username + ".jpg");
		return file.exists();
	}
	
	
	/*************************************/
	/* Method to make collage of avatars */
	/*************************************/
	public Bitmap makeCollage(Bitmap[] bitmapArray)
	{
		//final bitmap
		Bitmap b = null;
		
		int width = bitmapArray[0].getWidth() * 2;
		int height = bitmapArray[0].getHeight() * 2;
		
		int mHeight = bitmapArray[0].getHeight();
		int mWidth = bitmapArray[0].getWidth();

		b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888); 

		Canvas comboImage = new Canvas(b); 
		
		switch (bitmapArray.length)
		{
			case 2:
			{
				//top left
				comboImage.drawBitmap(bitmapArray[0], 0f, 0f, null); 
			    //top right
				comboImage.drawBitmap(bitmapArray[1], mWidth, 0f, null); 
			    //bottom left
				comboImage.drawBitmap(bitmapArray[1], 0f, mHeight, null);
			    //bottom right
				comboImage.drawBitmap(bitmapArray[0], mWidth, mHeight, null);
				
				break;
			}
			case 3:
			{
				//top left
				comboImage.drawBitmap(bitmapArray[0], 0f, 0f, null); 
			    //top right
				comboImage.drawBitmap(bitmapArray[1], mWidth, 0f, null); 
			    //bottom left
				comboImage.drawBitmap(bitmapArray[2], 0f, mHeight, null);
			    //bottom right
				comboImage.drawBitmap(bitmapArray[0], mWidth, mHeight, null);
				
				break;
			}
			case 4:
			{
				//top left
				comboImage.drawBitmap(bitmapArray[0], 0f, 0f, null); 
			    //top right
				comboImage.drawBitmap(bitmapArray[1], mWidth, 0f, null); 
			    //bottom left
				comboImage.drawBitmap(bitmapArray[2], 0f, mHeight, null);
			    //bottom right
				comboImage.drawBitmap(bitmapArray[3], mWidth, mHeight, null);
				
				break;
			}
		}
	    
		return b;
	}
	
	
	
	
	
	
	
	
	
}
