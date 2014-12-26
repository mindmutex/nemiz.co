package co.nemiz.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import co.nemiz.R;

import co.nemiz.domain.User;
import co.nemiz.services.DefaultRestService;


public class FriendsFragment extends android.support.v4.app.ListFragment
        implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener, SearchView.OnQueryTextListener {

    private final static String TAG = FriendsFragment.class.getSimpleName();

    private List<User> contactsList = new ArrayList<>();
    private ContactsListAdapter adapter;

    private SwipeRefreshLayout swipeLayout;
    private SwipeRefreshLayout emptySwipeLayout;

    private String filter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(R.color.blue, R.color.orange, R.color.red, R.color.green);

        emptySwipeLayout.setOnRefreshListener(this);
        emptySwipeLayout.setColorSchemeResources(R.color.blue, R.color.orange, R.color.red, R.color.green);

        adapter = new ContactsListAdapter(getActivity(), R.layout.fragment_friends_item, contactsList);
        updateContacts();

        setListAdapter(adapter);
        getListView().setOnScrollListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        emptySwipeLayout = (SwipeRefreshLayout) view.findViewById(android.R.id.empty);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        return view;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        filter = s;
        updateContacts();

        return true;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i2, int i3) {
        boolean enable = false;
        if(getListView()!= null && getListView().getChildCount() > 0){
            boolean firstItemVisible = getListView().getFirstVisiblePosition() == 0;
            boolean topOfFirstItemVisible = getListView().getChildAt(0).getTop() == 0;

            enable = firstItemVisible && topOfFirstItemVisible;
        }
        swipeLayout.setEnabled(enable);
    }

    @Override
    public void onListItemClick(ListView listView, final View view, int position, long id) {
        final int mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        final User contact = contactsList.get(position);

        final View layoutDisplay = view.findViewById(R.id.layoutDisplayFriend);
        final View layoutLoading = view.findViewById(R.id.layoutPoking);

        if (layoutDisplay.getVisibility() != View.VISIBLE) {
            Log.i(TAG, "Click ignored. Poke in progress");
            return;
        }
        layoutDisplay.animate()
            .alpha(0f)
            .setDuration(mShortAnimationDuration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layoutDisplay.setVisibility(View.GONE);
                    layoutLoading.setVisibility(View.VISIBLE);

                    new AsyncTask<Void,Void,Void>() {
                        @Override
                        protected Void doInBackground(Void[] objects) {
                            pokeUser(contact);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                            }
                            return null;
                        }

                        protected void onPostExecute(Void result) {
                            layoutLoading.setVisibility(View.GONE);
                            layoutDisplay.setVisibility(View.VISIBLE);
                            layoutDisplay.animate()
                                .alpha(1f)
                                .setDuration(mShortAnimationDuration)
                                .setListener(null);

                            Toast.makeText(getActivity(), R.string.txt_poke_sent, Toast.LENGTH_SHORT).show();
                        }
                    }.execute();
                }
            });
    }

    private void pokeUser(User user) {
        DefaultRestService service = DefaultRestService.get();
        service.pokeSync(user);
    }

    private void updateContacts() {
        DefaultRestService service = DefaultRestService.get();
        service.getContacts(filter, new DefaultRestService.Result<List<User>>() {
            @Override
            public void handle(int statusCode, List<User> result, String stringResult) {
                if (result != null) {
                    contactsList.clear();
                    contactsList.addAll(result);

                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getActivity(), R.string.data_failed, Toast.LENGTH_LONG).show();
                }
                clearRefreshing();
            }

            @Override
            public void retry(int attempt) {
                updateContacts();
            }
        });
    }

    private void clearRefreshing() {
        swipeLayout.setRefreshing(false);
        emptySwipeLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        DefaultRestService service = DefaultRestService.get();
        service.clearContacts();

        updateContacts();
    }

    /**
     * Adapter for contacts list.
     */
    public static class ContactsListAdapter extends ArrayAdapter<User> {
        public ContactsListAdapter(Context context, int resource, List<User> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            User contact = getItem(position);
            convertView = LayoutInflater.from(getContext()).inflate(
                R.layout.fragment_friends_item, parent, false);

            TextView name = (TextView) convertView.findViewById(R.id.listText);
            name.setText(contact.getName());

            return convertView;
        }
    }
}
