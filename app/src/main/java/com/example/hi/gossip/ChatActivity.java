package com.example.hi.gossip;

import android.content.Context;
import android.os.*;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{

	Toolbar toolbar;
	ActionBar actionBar;

	String chat_user_id;
	String current_user_id;
	String chat_username;
	String chat_thumb;

	RecyclerView recycle;
	MessageCustomAdapter adapter;
	ArrayList<Message> list;

	TextView display_name,display_Seen;
	CircleImageView display_pic;

	ImageButton add_file,send_msg;
	EditText add_msg;

	private DatabaseReference user_reference;
	private DatabaseReference message_reference;
	private DatabaseReference chat_reference;
	private DatabaseReference chat_notification_reference;
	private FirebaseAuth firebaseAuth;

	static boolean isChatOpen =true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		firebaseAuth = FirebaseAuth.getInstance();
		current_user_id = firebaseAuth.getCurrentUser().getUid();
		user_reference = FirebaseDatabase.getInstance().getReference().child("users");
		message_reference = FirebaseDatabase.getInstance().getReference().child("messages");
		chat_reference = FirebaseDatabase.getInstance().getReference().child("chats");
		chat_notification_reference = FirebaseDatabase.getInstance().getReference().child("chat_notifications");


		user_reference.keepSynced(true);
		message_reference.keepSynced(true);
		chat_reference.keepSynced(true);

		toolbar = (Toolbar)findViewById(R.id.chat_app_bar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);

		Bundle b = getIntent().getExtras();
		chat_user_id = b.getString("user_key");
		chat_username = b.getString("user_name");
		chat_thumb = b.getString("thumb");

		//Setting app bar details...

		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View app_bar_view = inflater.inflate(R.layout.chat_bar_layout,null);

		actionBar.setCustomView(app_bar_view);

		display_name = (TextView)app_bar_view.findViewById(R.id.display_name);
		display_Seen = (TextView)app_bar_view.findViewById(R.id.display_seen);
		display_pic = (CircleImageView)app_bar_view.findViewById(R.id.chatPic);

	//	add_file = (ImageButton)findViewById(R.id.add_files);
		add_msg = (EditText)findViewById(R.id.add_msg);
		send_msg = (ImageButton) findViewById(R.id.send_msg);

		list = new ArrayList<>();
		adapter = new MessageCustomAdapter(ChatActivity.this,list,chat_user_id,user_reference);
		recycle = (RecyclerView)findViewById(R.id.recycle_message);
		recycle.setHasFixedSize(true);
		recycle.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
		adapter.setHasStableIds(false);
		recycle.setAdapter(adapter);

		loadPreviousMessages();

		send_msg.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				sendMessage();
			}
		});

		display_name.setText(chat_username);

		if(chat_thumb.equals("default"))
			display_pic.setImageResource(R.drawable.avatar);
		else
		{
			Picasso.with(ChatActivity.this).load(chat_thumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar)
					.into(display_pic, new Callback()
					{
						@Override
						public void onSuccess() {}
						@Override
						public void onError()
						{
							Picasso.with(ChatActivity.this).load(chat_thumb).placeholder(R.drawable.avatar).into(display_pic);
						}
					});
		}

		user_reference.child(chat_user_id).addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				String last_seen=" ";
				boolean isOnline = (boolean)dataSnapshot.child("online").getValue();
				if(isOnline)
				{
					display_Seen.setText("Online");
				}
				else
				{
					if(dataSnapshot.hasChild("Last_Seen"))
					{
						GetTimeStamp getTimeStamp = new GetTimeStamp();
						long passed_time = Long.parseLong(dataSnapshot.child("Last_Seen").getValue().toString());
						String x = getTimeStamp.getTimeAgo(passed_time,getApplicationContext());

						if(x == null)
							last_seen ="last seen - just now";
						else
							last_seen = "last seen - "+x;

						display_Seen.setText(last_seen);
					}
					else
					{
						last_seen = " ";
						display_Seen.setText(last_seen);
					}
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{

			}
		});

	}

	private void loadPreviousMessages()
	{
		message_reference.child(current_user_id).child(chat_user_id).addChildEventListener(new ChildEventListener()
		{
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s)
			{
				Message message = dataSnapshot.getValue(Message.class);
				list.add(message);

				int pos = recycle.getAdapter().getItemCount()-1;
				recycle.smoothScrollToPosition(pos);
				adapter.notifyDataSetChanged();

			}
			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {}
			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
			@Override
			public void onCancelled(DatabaseError databaseError) {}
		});

		chat_reference.child(current_user_id).child(chat_user_id).addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				if(dataSnapshot.hasChild("seen") && isChatOpen)
				{
					if ((boolean) dataSnapshot.child("seen").getValue() == false)
						chat_reference.child(current_user_id).child(chat_user_id).child("seen").setValue(true);
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{

			}
		});

	}

	private void sendMessage()
	{
		final String msg = add_msg.getText().toString();
		if(!TextUtils.isEmpty(msg))
		{
			send_msg.setEnabled(false);
			final String time = DateFormat.getDateTimeInstance().format(new Date()).toString();
			final Message message = new Message();
			message.setMessage(msg);
			message.setFrom(current_user_id);
			message.setSeen(false);
			message.setTime(time);
			message.setType("text");

			add_msg.setText("");
			View view = ChatActivity.this.getCurrentFocus();
			if (view != null) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}

			message_reference.child(current_user_id).child(chat_user_id).push()
					.setValue(message).addOnSuccessListener(new OnSuccessListener<Void>()
			{
				@Override
				public void onSuccess(Void aVoid)
				{

					message_reference.child(chat_user_id).child(current_user_id).push()
							.setValue(message).addOnSuccessListener(new OnSuccessListener<Void>()
					{
						@Override
						public void onSuccess(Void aVoid)
						{
							int time_in_millis = (int)System.currentTimeMillis() * (-1);
							final Chats chats = new Chats();
							chats.setTime(time);
							chats.setMsg(msg);
							chats.setType("text");
							chats.setSeen(true);
							chats.setTime_in_millis(time_in_millis);

							send_msg.setEnabled(true);
							chat_reference.child(current_user_id).child(chat_user_id).setValue(chats)
									.addOnSuccessListener(new OnSuccessListener<Void>()
									{
										@Override
										public void onSuccess(Void aVoid)
										{
											chats.setSeen(false);
											chat_reference.child(chat_user_id).child(current_user_id).setValue(chats)
													.addOnSuccessListener(new OnSuccessListener<Void>()
													{
														@Override
														public void onSuccess(Void aVoid)
														{
															int ts= (int)System.currentTimeMillis();
															String t = Integer.toString(ts);
															HashMap<String,String> map = new HashMap<String, String>();
															map.put("type","chat_message");
															map.put("msg",msg);
															map.put("timestamp",t);
															chat_notification_reference.child(chat_user_id).child(current_user_id)
																	.setValue(map);
														}
													});
										}
									}).addOnFailureListener(new OnFailureListener()
							{
								@Override
								public void onFailure(@NonNull Exception e)
								{
									Toast.makeText(ChatActivity.this,"unable to store chat.. try later",Toast.LENGTH_SHORT).show();
									send_msg.setEnabled(true);
								}
							});

						}
					});
				}
			}).addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					Toast.makeText(ChatActivity.this,"unable to send message.. try later",Toast.LENGTH_SHORT).show();
					send_msg.setEnabled(true);
				}
			});
		}
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		isChatOpen =false;
		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		user_reference.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue(false);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		isChatOpen = true;
		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		user_reference.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue(true);

	}

	@Override
	protected void onResume()
	{
		super.onResume();
		isChatOpen = true;
		FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
		user_reference.child(firebaseAuth.getCurrentUser().getUid()).child("online").setValue(true);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		isChatOpen = false;
	}
}
