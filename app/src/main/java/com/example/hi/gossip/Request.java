package com.example.hi.gossip;

/**
 * Created by HI on 02-Sep-17.
 */

public class Request
{
	String request_type;
	int timestamp;

	public String getRequest_type()
	{
		return request_type;
	}

	public void setRequest_type(String request_type)
	{
		this.request_type = request_type;
	}

	public int getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(int timestamp)
	{
		this.timestamp = timestamp;
	}
}
