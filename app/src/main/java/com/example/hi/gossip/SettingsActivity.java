package com.example.hi.gossip;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity
{
	CircleImageView circleImageView;
	TextView display_name,display_status;
	Button change_image,change_status;

	DatabaseReference databaseReference;
	FirebaseAuth firebaseAuth;
	StorageReference storageReference;

	ProgressDialog progressDialog;
	Uri resultUri;

	final int GALLERY_PIC = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		firebaseAuth = FirebaseAuth.getInstance();
		String uid = firebaseAuth.getCurrentUser().getUid();

		databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

		databaseReference.keepSynced(true);

		storageReference = FirebaseStorage.getInstance().getReference();

		progressDialog = new ProgressDialog(SettingsActivity.this);
		progressDialog.setMessage("Getting account details..");
		progressDialog.setCancelable(false);
		progressDialog.show();

		circleImageView = (CircleImageView)findViewById(R.id.display_image);

		// Get the data from an ImageView as bytes
		circleImageView.setDrawingCacheEnabled(true);
		circleImageView.buildDrawingCache();

		display_name = (TextView)findViewById(R.id.display_name);
		display_status = (TextView)findViewById(R.id.display_status);

		change_image = (Button)findViewById(R.id.change_image);
		change_status = (Button)findViewById(R.id.change_status);

		databaseReference.addValueEventListener(new ValueEventListener()
		{
			@Override
			public void onDataChange(DataSnapshot dataSnapshot)
			{
				display_name.setText(dataSnapshot.child("name").getValue().toString());
				display_status.setText(dataSnapshot.child("status").getValue().toString());
				final String s = dataSnapshot.child("default_img").getValue().toString();
				if(s.equals("default"))
				{
					circleImageView.setImageResource(R.drawable.avatar);
					progressDialog.dismiss();
				}
				else{

					Picasso.with(SettingsActivity.this).load(s).networkPolicy(NetworkPolicy.OFFLINE)
							.placeholder(R.drawable.avatar).into(circleImageView, new Callback()
					{
						@Override
						public void onSuccess()
						{
							progressDialog.dismiss();
						}

						@Override
						public void onError()
						{
							Picasso.with(SettingsActivity.this).load(s).placeholder(R.drawable.avatar).into(circleImageView);
							progressDialog.dismiss();
						}
					});
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError)
			{

			}
		});

		change_status.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				// Create custom dialog object
				final Dialog dialog = new Dialog(SettingsActivity.this);
				// Include dialog.xml file
				dialog.setContentView(R.layout.status_dialog);
				// Set dialog title
				dialog.setTitle("Custom Dialog");

				// set values for custom dialog components - text, image and button
				final EditText text = (EditText) dialog.findViewById(R.id.et_status);
				text.setText(display_status.getText().toString());

				dialog.show();

				Button okbtn = (Button) dialog.findViewById(R.id.okbtn);
				// if decline button is clicked, close the custom dialog
				okbtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// Close dialog
						final String txt = text.getText().toString();
						if(!TextUtils.isEmpty(txt))
						{
							progressDialog.setMessage("updating status...");
							progressDialog.show();
							databaseReference.child("status").setValue(txt).addOnCompleteListener(new OnCompleteListener<Void>()
							{
								@Override
								public void onComplete(@NonNull Task<Void> task)
								{
									progressDialog.dismiss();
									Toast.makeText(SettingsActivity.this,"status updated..",Toast.LENGTH_SHORT).show();
								}
							}).addOnFailureListener(new OnFailureListener()
							{
								@Override
								public void onFailure(@NonNull Exception e)
								{
									progressDialog.dismiss();
									Toast.makeText(SettingsActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
								}
							});
						}
						else
						{
							Toast.makeText(SettingsActivity.this,"enter some text",Toast.LENGTH_SHORT).show();
						}
						dialog.dismiss();
					}
				});
			}
		});

		circleImageView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if ((ContextCompat.checkSelfPermission(SettingsActivity.this,
						android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
						!= PackageManager.PERMISSION_GRANTED)){

					ActivityCompat.requestPermissions(SettingsActivity.this,
							new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},10);

				}
				else
				{
					Intent i = new Intent(Intent.ACTION_GET_CONTENT);
					i.setType("image/*");
					startActivityForResult(i, GALLERY_PIC);
				}
			}
		});

		change_image.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(resultUri != null)
				{
					progressDialog.setMessage("updating image..");
					progressDialog.setCancelable(false);
					progressDialog.show();

					File file = new File(resultUri.getPath());
					byte[] data = new byte[1024];
					try
					{
						Bitmap compressedImageBitmap = new Compressor(SettingsActivity.this)
								.setMaxHeight(200)
								.setMaxWidth(200)
								.setQuality(60)
								.compressToBitmap(file);

						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG,75, baos);
						data = baos.toByteArray();

					}
					catch (IOException e)
					{
						e.printStackTrace();
					}

					final byte[] thumb_byte = data;
					final String n = firebaseAuth.getCurrentUser().getUid().toString()+".jpg";
					StorageReference reference = storageReference.child("display_pics").child(n);

					final StorageReference thumbreference = storageReference.child("thumb_pics").child(n);

					UploadTask uploadTask = reference.putFile(resultUri);

					uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
					{
						@Override
						public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
						{
							final String downloaduri = taskSnapshot.getDownloadUrl().toString();

							thumbreference.putBytes(thumb_byte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
							{
								@Override
								public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
								{
									String thumb_uri = taskSnapshot.getDownloadUrl().toString();
									databaseReference.child("default_img").setValue(downloaduri);
									databaseReference.child("thumb_img").setValue(thumb_uri);
									progressDialog.dismiss();
									Toast.makeText(SettingsActivity.this,"image updated..",Toast.LENGTH_SHORT).show();
								}
							}).addOnFailureListener(new OnFailureListener()
							{
								@Override
								public void onFailure(@NonNull Exception e)
								{
									progressDialog.dismiss();
									Toast.makeText(SettingsActivity.this,e.toString(),Toast.LENGTH_LONG).show();
								}
							});


						}
					}).addOnFailureListener(new OnFailureListener()
					{
						@Override
						public void onFailure(@NonNull Exception e)
						{
							progressDialog.dismiss();
							Toast.makeText(SettingsActivity.this,e.toString(),Toast.LENGTH_LONG).show();
						}
					});
				}
				else
					Toast.makeText(SettingsActivity.this,"select a pic first.",Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == GALLERY_PIC && resultCode == RESULT_OK)
		{
			Uri imageUri = data.getData();
			CropImage.activity(imageUri)
					.setAspectRatio(1,1)
					.setMinCropWindowSize(500,500)
					.start(this);
		}
		if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
			CropImage.ActivityResult result = CropImage.getActivityResult(data);
			if (resultCode == RESULT_OK) {
				 resultUri = result.getUri();
				circleImageView.setImageURI(resultUri);
			} else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
				Exception error = result.getError();
			}
		}
	}

	public static String random() {
		Random generator = new Random();
		StringBuilder randomStringBuilder = new StringBuilder();
		int randomLength = generator.nextInt(10);
		char tempChar;
		for (int i = 0; i < randomLength; i++){
			tempChar = (char) (generator.nextInt(96) + 32);
			randomStringBuilder.append(tempChar);
		}
		return randomStringBuilder.toString();
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		databaseReference.child("online").setValue(true);
	}

//	@Override
//	protected void onStop()
//	{
//		super.onStop();
//		databaseReference.child("online").setValue(false);
//	}
}
