package org.supervisor;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class TasksOverlay extends ItemizedOverlay<OverlayItem> {

	private Context mContext;
	private ArrayList<OverlayItem> tasksOverlays = new ArrayList<OverlayItem>();
	
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
		  dialog.show();
		  return true;
	}

}
