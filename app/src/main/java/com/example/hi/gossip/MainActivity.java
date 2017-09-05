package com.example.hi.gossip;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.test.mock.MockPackageManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.DateFormat;
import java.util.Date;
import java.util.jar.*;

public class MainActivity extends AppCompatActivity
{
	Toolbar toolbar;
	FirebaseAuth firebaseAuth;
	ActionBar actionBar;

	TabLayout tabLayout;
	ViewPager viewPager;
	ViewPagerAdapter viewPagerAdapter;
	DatabaseReference databaseReference;
	private LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar = (Toolbar)findViewById(R.id.main_app_bar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();
		actionBar.setTitle("Gossip");

		viewPager = (ViewPager)findViewById(R.id.viewpager);
		viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter( viewPagerAdapter);



//		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//			builder.setMessage("Enable Gps for security purpose")
//					.setCancelable(false)
//					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int id) {
//							Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//							startActivity(intent);
//						}
//					})
//					.setNegativeButton("No", new DialogInterface.OnClickListener() {
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.cancel();
//						}
//					});
//			AlertDialog alert = builder.create();
//			alert.show();
//		}
//		if ((ContextCompat.checkSelfPermission(MainActivity.this,
//				Manifest.permission.ACCESS_COARSE_LOCATION)
//				!= PackageManager.PERMISSION_GRANTED)){
//
//			ActivityCompat.requestPermissions(MainActivity.this,
//					new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},2);
//
//		}
//		else
//		{
//			startService(new Intent(MainActivity.this,GPSTracker.class));
//		}



		tabLayout = (TabLayout)findViewById(R.id.tablayout);
		tabLayout.setupWithViewPager(viewPager);


		firebaseAuth = FirebaseAuth.getInstance();

		if(firebaseAuth.getCurrentUser() != null)
		 databaseReference = FirebaseDatabase.getInstance().getReference().child("users")
				.child(firebaseAuth.getCurrentUser().getUid());
	}

	@Override
	protected void onStart()
	{
		super.onStart();

		FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
		if(firebaseUser == null)
		{
			startActivity(new Intent(MainActivity.this,StartActivity.class));
			finish();
		}
		else
		{
			databaseReference.child("online").setValue(true);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if(firebaseAuth.getCurrentUser() != null)
		databaseReference.child("online").setValue(true);
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		if(firebaseAuth.getCurrentUser() != null)
		{
			databaseReference.child("online").setValue(false);
			String time = DateFormat.getDateTimeInstance().format(new Date()).toString();
			databaseReference.child("Last_Seen").setValue(ServerValue.TIMESTAMP);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_menu,menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.logout)
		{
			databaseReference.child("online").setValue(false);
			databaseReference.child("token_id").setValue("default");
			firebaseAuth.signOut();
			startActivity(new Intent(MainActivity.this,StartActivity.class));
			finish();
		}
		if(item.getItemId() == R.id.account)
		{
			startActivity(new Intent(MainActivity.this,SettingsActivity.class));
		}
		if(item.getItemId() == R.id.users)
		{
			startActivity(new Intent(MainActivity.this,AllUsersActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}
}
