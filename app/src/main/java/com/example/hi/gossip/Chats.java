package com.example.hi.gossip;

/**
 * Created by HI on 02-Sep-17.
 */

public class Chats
{
	String msg,time,type;
	int time_in_millis;
	boolean seen;

	public boolean isSeen()
	{
		return seen;
	}

	public void setSeen(boolean seen)
	{
		this.seen = seen;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public String getTime()
	{
		return time;
	}

	public void setTime(String time)
	{
		this.time = time;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public int getTime_in_millis()
	{
		return time_in_millis;
	}

	public void setTime_in_millis(int time_in_millis)
	{
		this.time_in_millis = time_in_millis;
	}
}
