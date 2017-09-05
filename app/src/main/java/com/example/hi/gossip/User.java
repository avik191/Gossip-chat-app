package com.example.hi.gossip;

/**
 * Created by HI on 29-Aug-17.
 */

public class User
{
	String name,default_img,thumb_img,status,user_key,token_id;
	Long Last_Seen;
	boolean online;

	public boolean isOnline()
	{
		return online;
	}

	public Long getLast_Seen()
	{
		return Last_Seen;
	}

	public void setLast_Seen(Long last_Seen)
	{
		Last_Seen = last_Seen;
	}

	public void setOnline(boolean online)
	{
		this.online = online;
	}

	public String getUser_key()
	{
		return user_key;
	}

	public String getToken_id()
	{
		return token_id;
	}

	public void setToken_id(String token_id)
	{
		this.token_id = token_id;
	}

	public void setUser_key(String user_key)
	{
		this.user_key = user_key;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDefault_img()
	{
		return default_img;
	}

	public void setDefault_img(String default_img)
	{
		this.default_img = default_img;
	}

	public String getThumb_img()
	{
		return thumb_img;
	}

	public void setThumb_img(String tumb_img)
	{
		this.thumb_img = tumb_img;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
}
