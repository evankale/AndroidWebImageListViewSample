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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private ArrayList<ImageAndCaption> imageAndCaptions;
	private ImageRowAdapter imageRowAdapter;
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imageAndCaptions = ImageAndCaption.getList();
		imageRowAdapter = new ImageRowAdapter(this, imageAndCaptions);
		listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(imageRowAdapter);
	}
}

class ImageRowAdapter extends BaseAdapter {
	private final ArrayList<ImageAndCaption> itemRows;
	private final LayoutInflater inflater;

	public ImageRowAdapter(Context context, ArrayList<ImageAndCaption> itemRows) {
		this.itemRows = itemRows;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return itemRows.size();
	}

	@Override
	public Object getItem(int position) {
		return itemRows.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = inflater.inflate(R.layout.listview_row, null);
		}

		final TextView text = (TextView) view.findViewById(R.id.text);
		final ImageView icon = (ImageView) view.findViewById(R.id.icon);

		text.setText(itemRows.get(position).getImageCaption());
		icon.setImageResource(R.drawable.loading);
		WebImageLoader.getInstance().bindWebImageToImageView(
				itemRows.get(position).getImageURL(), icon);

		return view;
	}
}