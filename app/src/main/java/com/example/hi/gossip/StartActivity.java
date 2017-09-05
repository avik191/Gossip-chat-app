package com.example.hi.gossip;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity
{
	Button registerbtn,loginbtn;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		registerbtn = (Button)findViewById(R.id.register);
		loginbtn = (Button)findViewById(R.id.login);

		loginbtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startActivity(new Intent(StartActivity.this,LoginActivity.class));
				//finish();
			}
		});


		registerbtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				startActivity(new Intent(StartActivity.this,RegisterActivity.class));
				//finish();
			}
		});
	}
}
