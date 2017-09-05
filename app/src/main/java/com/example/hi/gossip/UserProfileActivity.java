package com.example.hi.gossip;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity
{
	DatabaseReference databaseReference;
	TextView profile_name,profile_status,total_friends;
	ImageView profile_img;
	Button send_req,decline_req;
	String uid;
	ProgressDialog dialog;

	DatabaseReference friend_request_reference;
	DatabaseReference friends_reference;
	DatabaseReference notification_reference;
	FirebaseAuth firebaseAuth;

	String current_State = "not_friends";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);

		profile_name = (TextView)findViewById(R.id.profile_name);
		profile_status = (TextView)findViewById(R.id.profile_status);
		total_friends = (TextView)findViewById(R.id.total_friends);

		profile_img = (ImageView)findViewById(R.id.profile_img);

		send_req = (Button)findViewById(R.id.sendbtn);
		decline_req =(Button)findViewById(R.id.declinebtn);


		Bundle b =getIntent().getExtras();
		uid = b.getString("user_key");

		databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
		//databaseReference.keepSynced(true);
		firebaseAuth = FirebaseAuth.getInstance();
		friend_request_reference = FirebaseDatabase.getInstance().getReference().child("friend_request");
	//	friend_request_reference.keepSynced(true);
		friends_reference = FirebaseDatabase.getInstance().getReference().child("friends");
		//friends_reference.keepSynced(true);

		notification_reference = FirebaseDatabase.getInstance().getReference().child("notification");


		dialog = new ProgressDialog(UserProfileActivity.this);
		dialog.setMessage("Fetching user details..");
		dialog.setCancelable(false);
		dialog.show();

		databaseReference.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				profile_name.setText(dataSnapshot.child("name").getValue().toString());
				profile_status.setText(dataSnapshot.child("status").getValue().toString());

				final String url = dataSnapshot.child("default_img").getValue().toString();

				if(url.equals("default"))
					profile_img.setImageResource(R.drawable.avatar);
				else
				{
					Picasso.with(UserProfileActivity.this).load(url).networkPolicy(NetworkPolicy.OFFLINE).
							placeholder(R.drawable.avatar).into(profile_img, new Callback()
					{
						@Override
						public void onSuccess()
						{

						}

						@Override
						public void onError()
						{
							Picasso.with(UserProfileActivity.this).load(url).placeholder(R.drawable.avatar).into(profile_img);
						}
					});

				}


				friend_request_reference.child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot dataSnapshot)
					{
						if(dataSnapshot.hasChild(uid))
						{
							String request_type = dataSnapshot.child(uid).child("request_type").getValue().toString();
							if(request_type.equals("received"))
							{
								send_req.setText("Accept Request");
								current_State = "request_received";
								decline_req.setVisibility(View.VISIBLE);
								decline_req.setEnabled(true);

							}
							else if(request_type.equals("sent"))
							{
								send_req.setText("cancel request");
								current_State = "request_sent";
							}
						}
						dialog.dismiss();
					}

					@Override
					public void onCancelled(DatabaseError databaseError)
					{

					}
				});

				friends_reference.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot dataSnapshot)
					{
						if(dataSnapshot.hasChild(uid))
						{
							send_req.setText("Remove friend");
							current_State = "friends";

							if(dialog.isShowing())
								dialog.dismiss();
						}
					}

					@Override
					public void onCancelled(DatabaseError databaseError)
					{

					}
				});

				if(dialog.isShowing())
					dialog.dismiss();

			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{

			}
		});

		send_req.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{

			//  not friends state //
				if(current_State.equals("not_friends"))
				{
					send_req.setEnabled(false);

					dialog.setCancelable(false);
					dialog.setMessage("please wait");
					dialog.show();

					final Request request = new Request();
					request.setRequest_type("sent");
					int time = (int)(System.currentTimeMillis()) * (-1);
					request.setTimestamp(time);

					friend_request_reference.child(firebaseAuth.getCurrentUser().getUid()).child(uid).setValue(request)
						.addOnCompleteListener(new OnCompleteListener<Void>()
						{
							@Override
							public void onComplete(@NonNull Task<Void> task)
							{
								if(task.isSuccessful())
								{
									request.setRequest_type("received");
									friend_request_reference.child(uid).child(firebaseAuth.getCurrentUser().getUid())
											.setValue(request).addOnSuccessListener(new OnSuccessListener<Void>()
									{
										@Override
										public void onSuccess(Void aVoid)
										{
											HashMap<String,String> notification_data = new HashMap<String, String>();
											notification_data.put("from",firebaseAuth.getCurrentUser().getUid());
											notification_data.put("type","request");

											notification_reference.child(uid).push().setValue(notification_data)
													.addOnCompleteListener(new OnCompleteListener<Void>()
													{
														@Override
														public void onComplete(@NonNull Task<Void> task)
														{
															if(task.isSuccessful())
															{
																dialog.dismiss();
																send_req.setEnabled(true);
																send_req.setText("cancel request");
																current_State = "request_sent";
																Toast.makeText(UserProfileActivity.this, "request sent", Toast.LENGTH_SHORT).show();
															}
															else
																Toast.makeText(UserProfileActivity.this, "something went wrong..", Toast.LENGTH_SHORT).show();

														}
													});

										}
									});
								}
								else
								{
									dialog.dismiss();
									send_req.setEnabled(true);
									Toast.makeText(UserProfileActivity.this, "cannot sent request..", Toast.LENGTH_SHORT).show();
								}
							}
						});
				}

				// cancel friend request state
				else if(current_State.equals("request_sent"))
				{
					send_req.setEnabled(false);
					friend_request_reference.child(firebaseAuth.getCurrentUser().getUid()).child(uid)
							.removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
					{
						@Override
						public void onSuccess(Void aVoid)
						{
							friend_request_reference.child(uid).child(firebaseAuth.getCurrentUser().getUid())
									.removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
							{
								@Override
								public void onSuccess(Void aVoid)
								{
									send_req.setEnabled(true);
									send_req.setText("send request");
									current_State = "not_friends";
									Toast.makeText(UserProfileActivity.this, "request cancelled", Toast.LENGTH_SHORT).show();
								}
							}).addOnFailureListener(new OnFailureListener()
							{
								@Override
								public void onFailure(@NonNull Exception e)
								{
									Toast.makeText(UserProfileActivity.this,e.toString(), Toast.LENGTH_SHORT).show();
								}
							});
						}
					});
					send_req.setEnabled(true);
				}

				// accept friend request
				else if(current_State.equals("request_received"))
				{
					dialog.setCancelable(false);
					dialog.setMessage("please wait");
					dialog.show();

					send_req.setEnabled(false);
					final String time = DateFormat.getDateTimeInstance().format(new Date()).toString();
					friends_reference.child(firebaseAuth.getCurrentUser().getUid()).child(uid).child("date")
							.setValue(time).addOnSuccessListener(new OnSuccessListener<Void>()
					{
						@Override
						public void onSuccess(Void aVoid)
						{
							friends_reference.child(uid).child(firebaseAuth.getCurrentUser().getUid()).child("date")
									.setValue(time).addOnSuccessListener(new OnSuccessListener<Void>()
							{
								@Override
								public void onSuccess(Void aVoid)
								{
									friend_request_reference.child(firebaseAuth.getCurrentUser().getUid()).child(uid)
											.removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
									{
										@Override
										public void onSuccess(Void aVoid)
										{
											friend_request_reference.child(uid).child(firebaseAuth.getCurrentUser().getUid())
													.removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
											{
												@Override
												public void onSuccess(Void aVoid)
												{
													dialog.dismiss();
													send_req.setEnabled(true);
													send_req.setText("Remove Friend");
													current_State = "friends";
													decline_req.setVisibility(View.GONE);
													Toast.makeText(UserProfileActivity.this, "you are now connected..", Toast.LENGTH_SHORT).show();
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
									dialog.dismiss();
								}
							});
						}
					});
					send_req.setEnabled(true);

				}

				// remove friend
				else if(current_State.equals("friends"))
				{
					send_req.setEnabled(false);
					friends_reference.child(firebaseAuth.getCurrentUser().getUid()).child(uid)
							.removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
					{
						@Override
						public void onSuccess(Void aVoid)
						{
							friends_reference.child(uid).child(firebaseAuth.getCurrentUser().getUid())
									.removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
							{
								@Override
								public void onSuccess(Void aVoid)
								{
									send_req.setText("send request");
									current_State = "not_friends";
									send_req.setEnabled(true);
								}
							}).addOnFailureListener(new OnFailureListener()
							{
								@Override
								public void onFailure(@NonNull Exception e)
								{
									Toast.makeText(UserProfileActivity.this,e.toString(), Toast.LENGTH_SHORT).show();
								}
							});
						}
					});
					send_req.setEnabled(true);
				}

				dialog.dismiss();
			}
		});

		decline_req.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				decline_req.setEnabled(false);
				friend_request_reference.child(firebaseAuth.getCurrentUser().getUid()).child(uid)
						.removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
				{
					@Override
					public void onSuccess(Void aVoid)
					{
						friend_request_reference.child(uid).child(firebaseAuth.getCurrentUser().getUid())
								.removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
						{
							@Override
							public void onSuccess(Void aVoid)
							{
								send_req.setEnabled(true);
								send_req.setText("send request");
								current_State = "not_friends";
								decline_req.setVisibility(View.GONE);
								Toast.makeText(UserProfileActivity.this, "request declined", Toast.LENGTH_SHORT).show();
							}
						}).addOnFailureListener(new OnFailureListener()
						{
							@Override
							public void onFailure(@NonNull Exception e)
							{
								Toast.makeText(UserProfileActivity.this,e.toString(), Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
				decline_req.setEnabled(true);
			}
		});
	}
}
