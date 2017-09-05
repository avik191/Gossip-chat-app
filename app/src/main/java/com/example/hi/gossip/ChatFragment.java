package com.example.hi.gossip;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment
{
	RecyclerView recycle;
	private String current_user_id;
	private FirebaseAuth firebaseAuth;
	private DatabaseReference chats_reference;
	private DatabaseReference user_reference;
	private Query query;
	FirebaseRecyclerAdapter<Chats,ChatsViewHolder> recyclerAdapter;


	public ChatFragment()
	{
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_chat, container, false);

		firebaseAuth = FirebaseAuth.getInstance();
		current_user_id = firebaseAuth.getCurrentUser().getUid();

		chats_reference = FirebaseDatabase.getInstance().getReference().child("chats").child(current_user_id);
		user_reference = FirebaseDatabase.getInstance().getReference().child("users");

		//user_reference.keepSynced(true);
		//chats_reference.keepSynced(true);

		query = chats_reference.orderByChild("time_in_millis");

		recycle = (RecyclerView)v.findViewById(R.id.chats_recycle);
		recycle.setLayoutManager(new LinearLayoutManager(getActivity()));
		//recycle.setHasFixedSize(true);

		return v;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		 recyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>
				(
						Chats.class,
						R.layout.chat_fragment_row,
						ChatsViewHolder.class,
						query
				)
		{
			@Override
			protected void populateViewHolder(final ChatsViewHolder viewHolder, Chats chats, int position)
			{



				final String list_user_id = getRef(position).getKey();
				viewHolder.setMessage(chats.getMsg());
				String x = chats.getTime();
				String temp[] = x.split(" ");
				String t,d;

				if(temp.length == 3)
				{
					t = temp[1];
					String a_p = temp[2];
					String temp2[] = t.split(":");
					t = temp2[0]+":"+temp2[1]+" "+a_p;

					d = temp[0].split("-")[0]+" "+temp[0].split("-")[1];
				}
				else
				{
					t = temp[3];
					String a_p = temp[4];
					String temp2[] = t.split(":");
					t = temp2[0]+":"+temp2[1]+" "+a_p;

					 d = temp[0]+" "+temp[1];
					d = d.substring(0,d.indexOf(","));
				}

				viewHolder.setTime(t);


				viewHolder.setDate(d);



				user_reference.child(list_user_id).addValueEventListener(new ValueEventListener()
				{
					@Override
					public void onDataChange(DataSnapshot dataSnapshot)
					{
						final String name = dataSnapshot.child("name").getValue().toString();
						//viewHolder.setName(name,2);

						final String thumb = dataSnapshot.child("thumb_img").getValue().toString();
						viewHolder.setThumb(thumb,getActivity());

						viewHolder.holder_view.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View view)
							{
								Intent intent = new Intent(getActivity(),ChatActivity.class);
								Bundle b = new Bundle();
								b.putString("user_key",list_user_id);
								b.putString("user_name",name);
								b.putString("thumb",thumb);
								intent.putExtras(b);
								startActivity(intent);


							}
						});

						chats_reference.child(list_user_id).addValueEventListener(new ValueEventListener()
						{
							@Override
							public void onDataChange(DataSnapshot dataSnapshot)
							{
								try
								{
									if ((boolean) dataSnapshot.child("seen").getValue() == false)
										viewHolder.setName(name, 1);
									else
										viewHolder.setName(name, 2);
								}catch(Exception e)
								{}

							}

							@Override
							public void onCancelled(DatabaseError databaseError)
							{

							}
						});
						//recyclerAdapter.notifyDataSetChanged();

					}

					@Override
					public void onCancelled(DatabaseError databaseError)
					{

					}
				});

			}

		};
		recyclerAdapter.notifyDataSetChanged();
		recycle.setAdapter(recyclerAdapter);
	}

	public static class ChatsViewHolder extends RecyclerView.ViewHolder
	{

		View holder_view;
		Context c;
		public ChatsViewHolder(View itemView)
		{
			super(itemView);

			holder_view = itemView;
		}

		public void setDate(String date)
		{
			TextView date_tv = (TextView)holder_view.findViewById(R.id.row_date);
			date_tv.setText(date);
		}

		public void setTime(String time)
		{
			TextView time_tv = (TextView)holder_view.findViewById(R.id.row_time);
			time_tv.setText(time);
		}

		public void setName(String name, int type)
		{
			TextView name_tv = (TextView)holder_view.findViewById(R.id.row_name);
			TextView chat_tv = (TextView)holder_view.findViewById(R.id.new_chat);
			if(type == 1)
			{
				name_tv.setTextColor(Color.parseColor("#1976d2"));
				chat_tv.setVisibility(View.VISIBLE);
			}
			else
			{
				name_tv.setTextColor(Color.BLACK);
				chat_tv.setVisibility(View.GONE);
			}
			name_tv.setText(name);


		}

		public void setMessage(String message)
		{
			TextView msg_tv = (TextView)holder_view.findViewById(R.id.row_msg);
			msg_tv.setText(message);
		}

		public void setThumb(final String thumb, final Context context)
		{
			c=context;
			final ImageView imageView = (ImageView)holder_view.findViewById(R.id.row_image);
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

	}



}
