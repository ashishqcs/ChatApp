package com.midcon.chatapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by miDnight on 06/10/2017.
 */

class SectionPageAdapter extends FragmentPagerAdapter {
    public SectionPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 0 :
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 1 :
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            case 2 :
                FriendsFragment friendsFragment= new FriendsFragment();
                return friendsFragment;

            default :
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){

        switch(position){

            case 0 :
                return "CHATS";
            case 1 :
                return "REQUESTS";
            case 2 :
                return "FRIENDS";

            default:
                return  null;
        }
    }
}
