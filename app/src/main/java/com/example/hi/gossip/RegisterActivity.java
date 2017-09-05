package com.example.hi.gossip;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity
{
	TextInputLayout username,password,email;
	Button createAccount;
	Toolbar toolbar;
	ActionBar actionBar;

	FirebaseAuth firebaseAuth;
	ProgressDialog dialog;

	DatabaseReference databaseReference;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		toolbar = (Toolbar)findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();
		actionBar.setTitle("Create Account");

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		firebaseAuth = FirebaseAuth.getInstance();
		dialog = new ProgressDialog(RegisterActivity.this);

		username = (TextInputLayout)findViewById(R.id.username);
		password = (TextInputLayout)findViewById(R.id.password);
		email = (TextInputLayout)findViewById(R.id.email);

		createAccount = (Button)findViewById(R.id.create_acc_btn);
		createAccount.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String e = email.getEditText().getText().toString();
				String p = password.getEditText().getText().toString();
				String name = username.getEditText().getText().toString();

				if(!TextUtils.isEmpty(e) && !TextUtils.isEmpty(p) && !TextUtils.isEmpty(name))
				{
					dialog.setMessage("creating account..");
					dialog.setCancelable(false);
					dialog.show();
					create_account(e,p,name);
				}
			}
		});

	}

	private void create_account(String e, String p, final String name)
	{
		firebaseAuth.createUserWithEmailAndPassword(e,p).addOnCompleteListener(new OnCompleteListener<AuthResult>()
		{
			@Override
			public void onComplete(@NonNull Task<AuthResult> task)
			{
				dialog.dismiss();
				if(task.isSuccessful())
				{
					String uid = firebaseAuth.getCurrentUser().getUid();
					databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

					// device token is used for notification purpose//
					String device_token = FirebaseInstanceId.getInstance().getToken();

					User user = new User();
					user.setName(name);
					user.setToken_id(device_token);
					user.setDefault_img("default");
					user.setStatus("Hi there ! I am using Gossip Chat..");
					user.setThumb_img("default");

					databaseReference.setValue(user);

					Toast.makeText(RegisterActivity.this,"account created",Toast.LENGTH_LONG).show();
					Intent i = new Intent(RegisterActivity.this,MainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(i);
					finish();
				}

			}
		}).addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				dialog.dismiss();
				Toast.makeText(RegisterActivity.this,e.toString(),Toast.LENGTH_LONG).show();
			}
		});
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item)
//	{
//		if(item.getItemId() == android.R.id.home)
//		{
//			onBackPressed();
//			finish();
//		}
//		return super.onOptionsItemSelected(item);
//	}
}
