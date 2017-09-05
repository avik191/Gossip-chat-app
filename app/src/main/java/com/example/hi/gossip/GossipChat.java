package com.example.hi.gossip;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by HI on 31-Aug-17.
 */

public class GossipChat extends Application
{

	FirebaseAuth firebaseAuth;
	DatabaseReference databaseReference;
	@Override
	public void onCreate()
	{
		super.onCreate();

		FirebaseDatabase.getInstance().setPersistenceEnabled(true);

		/*picasso */
		Picasso.Builder builder = new Picasso.Builder(this);
		builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
		Picasso built = builder.build();
		built.setIndicatorsEnabled(true);
		built.setLoggingEnabled(true);
		Picasso.setSingletonInstance(built);

		firebaseAuth = FirebaseAuth.getInstance();
		if(firebaseAuth.getCurrentUser() != null)
		{
			databaseReference = FirebaseDatabase.getInstance().getReference().child("users")
					.child(firebaseAuth.getCurrentUser().getUid());
			databaseReference.addValueEventListener(new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot dataSnapshot)
				{
					if (dataSnapshot != null)
					{
						databaseReference.child("online").onDisconnect().setValue(false);
						String time = DateFormat.getDateTimeInstance().format(new Date()).toString();
						databaseReference.child("Last_Seen").setValue(ServerValue.TIMESTAMP);					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError)
				{

				}
			});

		}
	}
}
