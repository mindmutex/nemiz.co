package co.nemiz.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import co.nemiz.R;
import co.nemiz.ui.view.SlidingTabLayout;

public class ContactsActivity extends ActionBarActivity {

    private ContactsPagerAdapter adapter;
    private ViewPager viewPager;

    private Intent shareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.drawable.logo);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        adapter = new ContactsPagerAdapter(getSupportFragmentManager(), getResources());

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(adapter);

        SlidingTabLayout tabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setDrawableIcons(
           getResources().getDrawable(R.drawable.ic_action_person),
           getResources().getDrawable(R.drawable.ic_action_cloud));

        tabLayout.setSelectedIndicatorColors(
            getResources().getColor(R.color.tab_background_indicator));

        tabLayout.setViewPager(viewPager);

        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
            getResources().getText(R.string.txt_share));

    }

    private void filterContactsList(String text) {
        Fragment fragment = adapter.getItem(viewPager.getCurrentItem());
        if (fragment instanceof SearchView.OnQueryTextListener) {
            SearchView.OnQueryTextListener queryTextListener = (SearchView.OnQueryTextListener) fragment;
            queryTextListener.onQueryTextChange(text);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contacts, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterContactsList(s);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                filterContactsList(s);
                return true;
            }
        });

        MenuItem shareItem = menu.findItem(R.id.action_share);
        ShareActionProvider actionProvider =
               (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        actionProvider.setShareIntent(shareIntent);

        return true;
    }

    public static class ContactsPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments = new ArrayList<>();
        private Resources resources;

        public ContactsPagerAdapter(FragmentManager fm, Resources resources) {
            super(fm);

            this.fragments.add(new FriendsFragment());
            this.fragments.add(new ActivityFragment());
            this.resources = resources;
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return resources.getString(R.string.tab_friends);
                case 1: return resources.getString(R.string.tab_activity);
            }
            return "";
        }
    }
}
