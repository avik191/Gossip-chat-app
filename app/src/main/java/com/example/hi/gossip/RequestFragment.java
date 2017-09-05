package com.example.hi.gossip;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
public class RequestFragment extends Fragment
{

	RecyclerView recycle;
	private String current_user_id;
	private FirebaseAuth firebaseAuth;
	private DatabaseReference user_reference;
	private DatabaseReference request_reference;
	private Query query;
	FirebaseRecyclerAdapter<Request,RequestViewHolder> recyclerAdapter;

	public RequestFragment()
	{
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_request, container, false);
		firebaseAuth = FirebaseAuth.getInstance();
		current_user_id = firebaseAuth.getCurrentUser().getUid();

		request_reference = FirebaseDatabase.getInstance().getReference().child("friend_request").child(current_user_id);
		user_reference = FirebaseDatabase.getInstance().getReference().child("users");

		query = request_reference.orderByChild("timestamp");

		recycle = (RecyclerView)v.findViewById(R.id.request_recycle);
		recycle.setLayoutManager(new LinearLayoutManager(getActivity()));
		return v;
	}

	@Override
	public void onStart()
	{
		super.onStart();

		recyclerAdapter = new FirebaseRecyclerAdapter<Request, RequestViewHolder>
				(
						Request.class,
						R.layout.all_user_single_row,
						RequestViewHolder.class,
						query
				)
		{
			@Override
			protected void populateViewHolder(final RequestViewHolder viewHolder, Request request, int position)
			{
				String state = request.getRequest_type();
				final String list_user_key = getRef(position).getKey();

				if(state.equals("received"))
				{
					user_reference.child(list_user_key).addValueEventListener(new ValueEventListener()
					{
						@Override
						public void onDataChange(DataSnapshot dataSnapshot)
						{
							String status = dataSnapshot.child("status").getValue().toString();
							viewHolder.setStatus(status);

							String name = dataSnapshot.child("name").getValue().toString();
							viewHolder.setName(name);

							String thumb = dataSnapshot.child("thumb_img").getValue().toString();
							viewHolder.setThumb(thumb,getActivity());

							viewHolder.holder_view.setOnClickListener(new View.OnClickListener()
							{
								@Override
								public void onClick(View view)
								{
									Intent intent = new Intent(getActivity(),UserProfileActivity.class);
									Bundle b = new Bundle();
									b.putString("user_key",list_user_key);
									intent.putExtras(b);
									startActivity(intent);
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
				else
				{
					viewHolder.cardView.setVisibility(View.GONE);
				}
			}
		};
		recyclerAdapter.notifyDataSetChanged();
		recycle.setAdapter(recyclerAdapter);
	}

	public static class RequestViewHolder extends RecyclerView.ViewHolder
	{

		View holder_view;
		CardView cardView ;

		public RequestViewHolder(View itemView)
		{
			super(itemView);

			holder_view = itemView;
			cardView = (CardView) itemView.findViewById(R.id.card_view);
		}

		public void setStatus(String status)
		{
			TextView status_tv = (TextView)holder_view.findViewById(R.id.row_status);
			status_tv.setText(status);
		}

		public void setName(String name)
		{
			TextView date_tv = (TextView)holder_view.findViewById(R.id.row_name);
			date_tv.setText(name);
		}


		public void setThumb(final String thumb, final Context context)
		{
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
