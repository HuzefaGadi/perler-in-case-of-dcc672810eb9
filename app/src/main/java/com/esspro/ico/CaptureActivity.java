package com.esspro.ico;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class CaptureActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_capture);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// don't restart if camera has just finished
		if( (System.currentTimeMillis() - InCaseOfApp.camera_done) > 1000 ) {	// 1 sec
			new Timer().schedule( new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							startActivity( new Intent(CaptureActivity.this, com.esspro.ico.CameraActivity.class) );
						}
					});
				}
			}, 500);	// 0.5 sec
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if( id == R.id.action_settings ) {
//			Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
