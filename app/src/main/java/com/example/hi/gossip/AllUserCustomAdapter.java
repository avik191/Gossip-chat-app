package com.example.hi.gossip;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


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

import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.os.Looper.getMainLooper;

/**
 * Created by HI on 18-July-17.
 */
public class AllUserCustomAdapter extends RecyclerView.Adapter<AllUserCustomAdapter.myviewholder> {

    Context context;
    ArrayList<User> list;
    LayoutInflater inflator;
	int lastPosition = 1;




	public AllUserCustomAdapter(Context context, ArrayList<User> list) {

        this.context=context;
        this.list=list;
		inflator=LayoutInflater.from(context);

    }

    @Override
    public myviewholder onCreateViewHolder(ViewGroup parent, int position) {
        View v=inflator.inflate(R.layout.all_user_single_row,parent,false);//this view will contain appearance of each layout i.e each row..
        myviewholder holder=new myviewholder(v);// we are passing the view of each row to the myviewholder class
        return holder;
    }

    @Override
    public void onBindViewHolder(final myviewholder holder, int position) {//here we will inflate datas in the widgets i.e image and title..
        //It is called for each row..so every row is inflated here..

	     User u=list.get(position);
	    holder.row_name.setText(u.getName());
	    holder.row_status.setText(u.getStatus());

	  final String s = u.getThumb_img();
	    if(s.equals("default"))
	    	holder.row_image.setImageResource(R.drawable.avatar);
	    else
	    {
		    Picasso.with(context).load(s).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.avatar)
				    .into(holder.row_image, new Callback()
				    {
					    @Override
					    public void onSuccess()
					    {

					    }

					    @Override
					    public void onError()
					    {
						    Picasso.with(context).load(s).placeholder(R.drawable.avatar).into(holder.row_image);
					    }
				    });
	    }
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public class myviewholder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
	    // It contains the elements in each row that we will inflate in the recyclerView..
	    TextView row_name,row_status;
	    CircleImageView row_image;

	    public myviewholder(View itemView)
	    {
		    super(itemView);
		    row_name = (TextView) itemView.findViewById(R.id.row_name);//here we link with the elements of each view i.e each row and
		    row_status = (TextView) itemView.findViewById(R.id.row_status);//here we link with the elements of each view i.e each row and

		    row_image = (CircleImageView) itemView.findViewById(R.id.row_image);

	        itemView.setOnClickListener(this);
		    row_image.setOnClickListener(this);
		    row_name.setOnClickListener(this);
		    row_status.setOnClickListener(this);
	    }


	    @Override
	    public void onClick(View view)
	    {
			int pos = getAdapterPosition();
		    User u = list.get(pos);
		    Intent i = new Intent(context,UserProfileActivity.class);
		    Bundle b = new Bundle();
		    b.putString("user_key",u.getUser_key());
		    i.putExtras(b);
		    context.startActivity(i);
	    }


    }



	public void  filterList(ArrayList<User> newList)
    {
	    list=newList;
	    notifyDataSetChanged();
    }

}
