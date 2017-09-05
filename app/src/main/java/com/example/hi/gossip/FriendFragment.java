package com.example.hi.gossip;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendFragment extends Fragment
{

	RecyclerView recycle;


	private DatabaseReference friends_reference;
	private FirebaseAuth firebaseAuth;
	private ChildEventListener childEventListener;
	private DatabaseReference user_reference;
	FirebaseRecyclerAdapter<Friends,FriendsViewHolder> recyclerAdapter;

	private String current_user_id;

	public FriendFragment()
	{
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_friend, container, false);
		recycle = (RecyclerView)v.findViewById(R.id.friends_recycle);

		firebaseAuth = FirebaseAuth.getInstance();
		current_user_id = firebaseAuth.getCurrentUser().getUid();

		friends_reference = FirebaseDatabase.getInstance().getReference().child("friends").child(current_user_id);
		user_reference =  FirebaseDatabase.getInstance().getReference().child("users");

	//	friends_reference.keepSynced(true);
	//	user_reference.keepSynced(true);

		//recycle.setHasFixedSize(true);
		recycle.setLayoutManager(new LinearLayoutManager(getActivity()));

		return v;
	}


	@Override
	public void onStart()
	{
		super.onStart();

		 recyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
				(
						Friends.class,
						R.layout.friends_single_row,
						FriendsViewHolder.class,
						friends_reference
				)
		{
			@Override
			protected void populateViewHolder(final FriendsViewHolder viewHolder, Friends friend, int position)
			{
				viewHolder.setDate(friend.getDate());

				final String list_user_id = getRef(position).getKey();

				user_reference.child(list_user_id).addValueEventListener(new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot dataSnapshot)
					{
						final String name = dataSnapshot.child("name").getValue().toString();
						final String thumb = dataSnapshot.child("thumb_img").getValue().toString();
						String status = dataSnapshot.child("status").getValue().toString();

						boolean isOnline;
						if(!dataSnapshot.hasChild("online"))
							isOnline = false;
						else
						{
							isOnline = (boolean)dataSnapshot.child("online").getValue();
						}

						viewHolder.setStatus(status);
						viewHolder.setOnline(isOnline);
						viewHolder.setName(name);
						viewHolder.setThumb(thumb,getActivity());



						viewHolder.holder_view.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};

								final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
								builder.setTitle("select option");
								builder.setItems(options, new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialogInterface, int i)
									{
										if(i==0)
										{
											Intent intent = new Intent(getActivity(),UserProfileActivity.class);
											Bundle b = new Bundle();
											b.putString("user_key",list_user_id);
											intent.putExtras(b);
											startActivity(intent);
										}
										else if(i==1)
										{
											Intent intent = new Intent(getActivity(),ChatActivity.class);
											Bundle b = new Bundle();
											b.putString("user_key",list_user_id);
											b.putString("user_name",name);
											b.putString("thumb",thumb);
											intent.putExtras(b);
											startActivity(intent);
										}
									}
								});

								builder.show();
							}
						});
						//recyclerAdapter.notifyDataSetChanged();

					}

					@Override
					public void onCancelled(DatabaseError databaseError)
					{

					}

				});
//				recyclerAdapter.notifyDataSetChanged();
			}
		};

		recyclerAdapter.notifyDataSetChanged();
		recycle.setAdapter(recyclerAdapter);
	}

	public static class FriendsViewHolder extends RecyclerView.ViewHolder
	{

		View holder_view;
		final ImageView imageView;

		public FriendsViewHolder(View itemView)
		{
			super(itemView);

			holder_view = itemView;
			imageView = (ImageView)holder_view.findViewById(R.id.row_image);
		}

		public void setDate(String date)
		{
			TextView date_tv = (TextView)holder_view.findViewById(R.id.row_date);
			date_tv.setText("since : "+date);
		}

		public void setName(String name)
		{
			TextView date_tv = (TextView)holder_view.findViewById(R.id.row_name);
			date_tv.setText(name);
		}

		public void setStatus(String status)
		{
			TextView date_tv = (TextView)holder_view.findViewById(R.id.row_status);
			date_tv.setText(status);
		}

		public void setThumb(final String thumb, final Context context)
		{
//			final ImageView imageView = (ImageView)holder_view.findViewById(R.id.row_image);
			if(thumb.equals("default"))
				imageView.setImageResource(R.drawable.avatar);
			else
			{
				Picasso.with(context).load(thumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar)
						.into(imageView, new Callback()
						{
							@Override
							public void onSuccess()
							{

							}

							@Override
							public void onError()
							{
								Picasso.with(context).load(thumb).placeholder(R.drawable.avatar).into(imageView);
							}
						});
			}
		}

		public void setOnline(boolean isOnline)
		{
			ImageButton imageButton = (ImageButton)holder_view.findViewById(R.id.online_button);
			if(isOnline)
				imageButton.setVisibility(View.VISIBLE);
			else
				imageButton.setVisibility(View.INVISIBLE);
		}
	}




}
