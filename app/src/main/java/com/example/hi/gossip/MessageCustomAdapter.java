package com.example.hi.gossip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by HI on 18-July-17.
 */
public class MessageCustomAdapter extends RecyclerView.Adapter<MessageCustomAdapter.myviewholder> {

    Context context;
    ArrayList<Message> list;
    LayoutInflater inflator;
	String chat_user_id;
	DatabaseReference user_reference;



	public MessageCustomAdapter(Context context, ArrayList<Message> list, String chat_user_id, DatabaseReference user_reference) {

        this.context=context;
        this.list=list;
		this.chat_user_id = chat_user_id;
		this.user_reference = user_reference;
		inflator=LayoutInflater.from(context);
    }

    @Override
    public myviewholder onCreateViewHolder(ViewGroup parent, int position) {
        View v=inflator.inflate(R.layout.chat_message_single_row,parent,false);//this view will contain appearance of each layout i.e each row..
        myviewholder holder=new myviewholder(v);// we are passing the view of each row to the myviewholder class
        return holder;
    }

    @Override
    public void onBindViewHolder(final myviewholder holder, int position) {//here we will inflate datas in the widgets i.e image and title..
        //It is called for each row..so every row is inflated here..

	    Message message = list.get(position);
	    String temp[] = message.getTime().split(" ");
	    String t;
	    if(temp.length == 3)
	    {
		     t = temp[1];
		    String a_p = temp[2];
		    String temp2[] = t.split(":");
		    t = temp2[0]+":"+temp2[1]+" "+a_p;
	    }
	    else
	    {
		     t = temp[3];
		    String a_p = temp[4];
		    String temp2[] = t.split(":");
		    t = temp2[0]+":"+temp2[1]+" "+a_p;
	    }

		if(!message.getFrom().equals(chat_user_id))
		{
			holder.row_image.setVisibility(View.GONE);
			holder.row_layout.setGravity(Gravity.RIGHT);
			holder.text_view_background.setBackgroundResource(R.drawable.chat_message_layout);
			holder.row_message.setTextColor(Color.WHITE);
			holder.row_time.setTextColor(Color.WHITE);
		}
		else
		{
			holder.row_image.setVisibility(View.VISIBLE);
			holder.row_layout.setGravity(Gravity.LEFT);
			user_reference.child(chat_user_id).addListenerForSingleValueEvent(new ValueEventListener()
			{
				@Override
				public void onDataChange(DataSnapshot dataSnapshot)
				{
					final String thumb = dataSnapshot.child("thumb_img").getValue().toString();
					if(thumb.equals("default"))
						holder.row_image.setImageResource(R.drawable.avatar);
					else
					{
						Picasso.with(context).load(thumb).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar)
								.into(holder.row_image, new Callback()
								{
									@Override
									public void onSuccess()
									{

									}

									@Override
									public void onError()
									{
										Picasso.with(context).load(thumb).placeholder(R.drawable.avatar).into(holder.row_image);
									}
								});
					}
				}

				@Override
				public void onCancelled(DatabaseError databaseError)
				{

				}
			});
			holder.text_view_background.setBackgroundResource(R.drawable.chat_message_layout2);
			holder.row_message.setTextColor(Color.GRAY);
			holder.row_time.setTextColor(Color.GRAY);
		}


	    holder.row_message.setText(message.getMessage());
	    holder.row_time.setText(t);

    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public class myviewholder extends RecyclerView.ViewHolder
    {
	    // It contains the elements in each row that we will inflate in the recyclerView..
	    TextView row_message,row_time;
	    CircleImageView row_image;
	    LinearLayout row_layout,text_view_background;

	    public myviewholder(View itemView)
	    {
		    super(itemView);
		    row_message = (TextView) itemView.findViewById(R.id.chat_row_text);//here we link with the elements of each view i.e each row and
		    row_time = (TextView) itemView.findViewById(R.id.chat_row_time);//here we link with the elements of each view i.e each row and
		    row_image = (CircleImageView) itemView.findViewById(R.id.chat_row_image);
		    row_layout = (LinearLayout)itemView.findViewById(R.id.row_layout);
		    text_view_background = (LinearLayout)itemView.findViewById(R.id.text_view_background);

	    }



    }



	public void  filterList(ArrayList<Message> newList)
    {
	    list=newList;
	    notifyDataSetChanged();
    }

}
