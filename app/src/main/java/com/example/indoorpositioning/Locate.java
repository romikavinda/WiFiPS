package com.example.indoorpositioning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.widget.AdapterView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class Locate extends Activity {

	ArrayList<String> buildings;
	DatabaseHelper db;
	ArrayAdapter<String> arrayAdapter;
	ArrayList<PositionData> positionsData;
	String building;
	TextView result;
	Button locate;
	LinearLayout everyThingElse;

	public void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
		setContentView(R.layout.locate);
		db = new DatabaseHelper(this);
		buildings = db.getBuildings();
		locate = (Button) findViewById(R.id.locate);
		everyThingElse=(LinearLayout) findViewById(R.id.everythingelse);
		result = (TextView) findViewById(R.id.result);
		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, buildings);

		locate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), Scan.class);
				intent.putExtra("isLearning", false);
				startActivityForResult(intent,0);
				
			}
		});
	
		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, buildings);
		// Set The Adapter
        if (buildings.size()==0) {
            Toast.makeText(this, "No building data available.", Toast.LENGTH_LONG).show();
            locate.setEnabled(false);
        }
        else{


		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
		builder.setTitle("Choose building");
		builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// the user clicked on colors[which]
				building = buildings.get(which);

							
				
			}
		});
		builder.show();
        }

	}



    @Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// TODO Auto-generated method stub
        if(resultCode==RESULT_OK){


		PositionData positionData = (PositionData) intent
				.getSerializableExtra("PositionData");
		positionsData=db.getReadings(building);

		String closestPosition = null;
		ArrayList<Router> wifis = db.getFriendlyWifis(building);

		int min_distance = positionData.uDistance(positionsData.get(0), wifis);
		closestPosition = positionsData.get(0).getName();
		String res = "";
		res += closestPosition + "\n" + min_distance;
		res += "\n" + positionsData.get(0).toString();
		for (int i = 1; i < positionsData.size(); i++) {
			int distance = positionData.uDistance(positionsData.get(i), wifis);
			res += "\n" + positionsData.get(i).getName() + "\n" + distance;
			res += "\n" + positionsData.get(i).toString();
			if (distance <= min_distance) {
				min_distance = distance;

				closestPosition = positionsData.get(i).getName();

			}

		}
		if (min_distance == PositionData.MAX_DISTANCE){
			closestPosition = "OUTOFRANGE";

		}

		
			
		res += "\nCurrent:\n" + positionData.toString();
		Log.v("Result",res);
		
		result.setText("You are at " + closestPosition + "\n" + res);
		//everyThingElse.setVisibility(LinearLayout.GONE);
		
		super.onActivityResult(requestCode, resultCode, intent);
        }
	}


	public class CustomOnItemSelectedListener implements
			AdapterView.OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {

			building = parent.getItemAtPosition(pos).toString();
			locate.setEnabled(true);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			locate.setEnabled(false);
		}

	}



}
