/*
 * Copyright (c) 2015 Evan Kale
 * Email: EvanKale91@gmail.com
 * Website: www.ISeeDeadPixel.com
 * 
 * This file is part of WebImageListViewSample.
 *
 * WebImageListViewSample is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.isdp.webimagelistviewsample;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

public class WebImageLoader {

	private static WebImageLoader instance;

	// Hold a map of ImageView to AsyncTasks, to ensure that each view only has
	// one active AsyncTask
	private final HashMap<ImageView, WebImageAsyncTask> imageThreadMap;

	private WebImageLoader() {
		imageThreadMap = new HashMap<ImageView, WebImageAsyncTask>();
	}

	public static WebImageLoader getInstance() {
		if (instance == null) {
			instance = new WebImageLoader();
		}
		return instance;
	}

	public void bindWebImageToImageView(final String imageUrl,
			final ImageView imageView) {

		WebImageAsyncTask imageThread = imageThreadMap.get(imageView);

		if (imageThread == null) {
			// Spawn an image download thread if one doesn't already exist for
			// the image view
			imageThread = new WebImageAsyncTask(imageView, imageThreadMap);
			imageThread.pushUrl(imageUrl);
			imageThreadMap.put(imageView, imageThread);
			imageThread.execute();
		} else {
			// If image download thread already exists, then push the new url to
			// the thread
			imageThread.pushUrl(imageUrl);
		}
	}
}

class WebImageAsyncTask extends AsyncTask {

	// This is the delay time before the async task fetches the image. This is
	// done to prevent the async task to fetch every single image that was
	// scrolled past too quickly.
	private static final long DELAY_TIME = 300;
	private static final String TAG = "WebImageAsyncTask";

	private String latestURL;
	private long lastQueueTime;
	private final ImageView imageView;
	private final HashMap<ImageView, WebImageAsyncTask> imageThreadMap;

	public WebImageAsyncTask(ImageView imageView,
			HashMap<ImageView, WebImageAsyncTask> imageThreadMap) {
		this.imageView = imageView;
		this.imageThreadMap = imageThreadMap;
	}

	public void pushUrl(String queueURL) {
		latestURL = queueURL;
		lastQueueTime = System.currentTimeMillis();
	}

	private class Status {
		boolean status;
	}

	private void sleep() {
		try {
			Thread.sleep(50);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}

	private boolean setImageViewInMainThread(final ImageView imageView,
			final Bitmap bmp, final String downloadedURL) {

		final Semaphore semaphore = new Semaphore(0);
		final Status isNewImageSet = new Status();

		// Handle the UI update in the main thread
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (downloadedURL.equals(latestURL)) {

					// If the downloaded image url mathes the latest url pushed,
					// then this thread has retrieved the latest (correct)
					// image. So we set this image to the ImageView.
					if (bmp != null) {
						imageView.setImageBitmap(bmp);
					} else {
						imageView.setImageResource(R.drawable.not_found);
					}

					// Remove the reference to this thread in thread map so that
					// it can be garbage collected.
					imageThreadMap.remove(imageView);
					isNewImageSet.status = true;

				} else {
					isNewImageSet.status = false;
				}
				semaphore.release();
			}
		});

		// Block until main thread is done
		try {
			semaphore.acquire();
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}

		return isNewImageSet.status;
	}

	@Override
	protected Object doInBackground(Object... params) {
		boolean isImageViewSet = false;

		while (!isImageViewSet) {
			// Sleep until ImageView has stabilized
			while ((System.currentTimeMillis() - lastQueueTime) < DELAY_TIME) {
				sleep();
			}

			String downloadURL = latestURL;
			Bitmap bmp = null;

			try {
				HttpURLConnection connection = (HttpURLConnection) new URL(
						downloadURL).openConnection();
				connection.connect();
				InputStream input = connection.getInputStream();
				bmp = BitmapFactory.decodeStream(input);
			} catch (Exception e) {
				Log.d(TAG, e.toString());
			}

			isImageViewSet = setImageViewInMainThread(imageView, bmp,
					downloadURL);

			// If setImageViewInMainThread returns false, then a new image url
			// was pushed while one was being downloaded, and no bitmap was set
			// on the image view, in that case, we continue the loop to download
			// the image from the newly pushed url.
		}
		return null;
	}
}
