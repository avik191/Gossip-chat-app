package com.example.hi.gossip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by HI on 28-Aug-17.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter
{
	public ViewPagerAdapter(FragmentManager fm)
	{
		super(fm);
	}

	@Override
	public Fragment getItem(int position)
	{
		switch (position)
		{
			case 2 : RequestFragment requestFragment = new RequestFragment();
						return  requestFragment;

			case 1 : ChatFragment chatFragment = new ChatFragment();
						return chatFragment;

			case 0 : FriendFragment friendFragment = new FriendFragment();
						return friendFragment;

			default: return null;
		}
	}

	@Override
	public int getCount()
	{
		return 3;
	}

	public CharSequence getPageTitle(int position)
	{
		switch (position)
		{
			case 0 :
				return "Friends";

			case 1 :
				return "Chats";

			case 2 :
				return "Requests";

			default: return null;
		}
	}
}
