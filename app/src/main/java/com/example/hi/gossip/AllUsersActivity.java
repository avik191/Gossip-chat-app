package com.example.hi.gossip;

import android.app.ProgressDialog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AllUsersActivity extends AppCompatActivity
{

	ActionBar actionBar;
	Toolbar toolbar;
	RecyclerView recycle;
	AllUserCustomAdapter adapter;

	ArrayList<User> list;

	private DatabaseReference databaseReference;
	private ChildEventListener childEventListener;
	private FirebaseAuth firebaseAuth;
	SearchView searchView;

	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_users);

		databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
		//databaseReference.keepSynced(true);
		firebaseAuth = FirebaseAuth.getInstance();
		databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue(true);


		dialog = new ProgressDialog(AllUsersActivity.this);
		dialog.setCancelable(false);
		dialog.setMessage("Fetching all users...");
		dialog.show();

		toolbar = (Toolbar)findViewById(R.id.appBar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();
		actionBar.setTitle("All Users");
		actionBar.setDisplayHomeAsUpEnabled(true);

		recycle = (RecyclerView)findViewById(R.id.all_users_recycle);

		list = new ArrayList<>();

		recycle.setLayoutManager(new LinearLayoutManager(AllUsersActivity.this));
		adapter = new AllUserCustomAdapter(AllUsersActivity.this,list);
		recycle.setAdapter(adapter);

		childEventListener = new ChildEventListener()
		{
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s)
			{
				if(dialog != null)
				{
					dialog.dismiss();
					dialog = null;
				}

				User user = dataSnapshot.getValue(User.class);
				//User user = new User();
				String key = dataSnapshot.getKey().toString();
				if(!key.equals(firebaseAuth.getCurrentUser().getUid().toString()))
				{
					user.setUser_key(key);
					list.add(user);
					adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {}
			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
			@Override
			public void onCancelled(DatabaseError databaseError) {}
		};
		databaseReference.addChildEventListener(childEventListener);

	}

	@Override
	protected void onStart()
	{
		super.onStart();
		databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue(true);
	}

//	@Override
//	protected void onStop()
//	{
//		super.onStop();
//		databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue(false);
//	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.alluser_main_menu,menu);
		searchView = (SearchView) menu.findItem(R.id.searchbtn).getActionView();
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
		{
			@Override
			public boolean onQueryTextSubmit(String query)
			{
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText)
			{
				if(adapter != null)
				{
					newText = newText.toLowerCase();
					ArrayList<User> newList = new ArrayList<>();
					for (User user : list)
					{
						if (user.getName().toLowerCase().contains(newText))
							newList.add(user);
					}
					adapter.filterList(newList);
				}
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

}
