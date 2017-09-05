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

public class LoginActivity extends AppCompatActivity
{
	TextInputLayout password,email;
	Button login;
	Toolbar toolbar;
	ActionBar actionBar;

	FirebaseAuth firebaseAuth;
	DatabaseReference databaseReference;
	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		toolbar = (Toolbar)findViewById(R.id.app_bar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();
		actionBar.setTitle("Login");

		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		firebaseAuth = FirebaseAuth.getInstance();
		databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

		dialog = new ProgressDialog(LoginActivity.this);

		password = (TextInputLayout)findViewById(R.id.password);
		email = (TextInputLayout)findViewById(R.id.email);

		login = (Button)findViewById(R.id.login_btn);
		login.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String e = email.getEditText().getText().toString();
				String p = password.getEditText().getText().toString();

				if(!TextUtils.isEmpty(e) && !TextUtils.isEmpty(p))
				{
					dialog.setMessage("signing in..");
					dialog.setCancelable(false);
					dialog.show();
					login_account(e,p);
				}
			}
		});
	}

	private void login_account(String e, String p)
	{
		firebaseAuth.signInWithEmailAndPassword(e,p).addOnCompleteListener(new OnCompleteListener<AuthResult>()
		{
			@Override
			public void onComplete(@NonNull Task<AuthResult> task)
			{
				dialog.dismiss();
				if(task.isSuccessful())
				{
					String device_token = FirebaseInstanceId.getInstance().getToken();
					databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("token_id").setValue(device_token);

					Toast.makeText(LoginActivity.this,"successfully logged in..",Toast.LENGTH_LONG).show();
					Intent i = new Intent(LoginActivity.this,MainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					startActivity(i);
				}
			}
		}).addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(LoginActivity.this,e.toString(),Toast.LENGTH_LONG).show();
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
