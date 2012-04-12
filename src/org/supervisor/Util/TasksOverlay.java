package org.supervisor.Util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.supervisor.R;
import org.supervisor.Activities.SingleTaskActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class TasksOverlay extends ItemizedOverlay<OverlayItem> {

	private Context mContext;
	private ArrayList<OverlayItem> tasksOverlays = new ArrayList<OverlayItem>();
	private Long redirectToTaskNo;
	
	public TasksOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		
	}

	@Override
	protected OverlayItem createItem(int i) {
		return tasksOverlays.get(i);
	}

	@Override
	public int size() {
		return tasksOverlays.size();
	}
	
	public void removeOverlay(OverlayItem overlay) {
		tasksOverlays.remove(overlay);
	}

	
	public void addOverlay(OverlayItem overlay) {
		tasksOverlays.add(overlay);
	    populate();
	}
	
	public boolean onTap(int index) {
		OverlayItem item = tasksOverlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		int i = item.getTitle().indexOf("(id:");
		if (i > -1) {
			String tmp = item.getTitle().substring(i);
			Pattern intsOnly = Pattern.compile("\\d+");
			Matcher makeMatch = intsOnly.matcher(tmp);
			makeMatch.find();
			String inputInt = makeMatch.group();
			redirectToTaskNo = Long.parseLong(inputInt);
			dialog.setPositiveButton(mContext.getString(R.string.task_details), new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent intent = new Intent(mContext, SingleTaskActivity.class);
					intent.putExtra("getTaskById", redirectToTaskNo);
					mContext.startActivity(intent);
					
				}
			});
		}
		  dialog.show();
		  return true;
	}

}
