package com.example.hi.gossip;

/**
 * Created by HI on 01-Sep-17.
 */

public class Message
{
	String message,from,type,time;
	boolean seen;

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getFrom()
	{
		return from;
	}

	public void setFrom(String from)
	{
		this.from = from;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getTime()
	{
		return time;
	}

	public void setTime(String time)
	{
		this.time = time;
	}

	public boolean isSeen()
	{
		return seen;
	}

	public void setSeen(boolean seen)
	{
		this.seen = seen;
	}
}
