package co.nemiz.ui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import co.nemiz.R;
import co.nemiz.domain.Activity;
import co.nemiz.services.DefaultRestService;

public class ActivityFragment extends ListFragment  implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    private SwipeRefreshLayout swipeLayout;
    private SwipeRefreshLayout emptySwipeLayout;

    private List<Activity> activityList = new ArrayList<>();

    private ActivityListAdapter adapter;

    private Long activityOffset = 0L;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(
                R.color.blue, R.color.orange, R.color.red, R.color.green);

        emptySwipeLayout.setOnRefreshListener(this);
        emptySwipeLayout.setColorSchemeResources(
                R.color.blue, R.color.orange, R.color.red, R.color.green);

        adapter = new ActivityListAdapter(getActivity(),
            R.layout.fragment_activity_item, activityList);

        getListView().setAdapter(adapter);
        getListView().setClickable(false);
        getListView().setOnScrollListener(this);

        updateActivity(0L);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activity, container, false);

        emptySwipeLayout = (SwipeRefreshLayout) view.findViewById(android.R.id.empty);
        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        return view;
    }

    private void updateActivity(Long offset) {
        if (emptySwipeLayout.isShown()) {
            emptySwipeLayout.setRefreshing(true);
        }

        DefaultRestService service = DefaultRestService.get();
        service.getActivity(offset, new DefaultRestService.Result<List<Activity>>() {
            @Override
            public void handle(int statusCode, List<Activity> result, String stringResult) {
                if (result != null) {
                    if (result.size() != activityList.size()) {
                        activityList.clear();
                        activityList.addAll(result);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.data_failed, Toast.LENGTH_LONG).show();
                }
                clearRefreshing();
            }
        });
    }

    private void clearRefreshing() {
        swipeLayout.setRefreshing(false);
        emptySwipeLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        activityOffset = 0L;
        activityList.clear();

        DefaultRestService service = DefaultRestService.get();
        service.clearActivity();

        updateActivity(0L);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        boolean enable = false;
        if(getListView() != null && getListView().getChildCount() > 0) {
            boolean firstItemVisible = getListView().getFirstVisiblePosition() == 0;
            boolean topOfFirstItemVisible = getListView().getChildAt(0).getTop() == 0;

            // enable swipe layout?
            enable = firstItemVisible && topOfFirstItemVisible;

            if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                Long offsetValue = Long.valueOf(activityList.size());
                if (!offsetValue.equals(activityOffset)) {
                    activityOffset = offsetValue;
                    updateActivity(offsetValue);
                }
            }
        }
        swipeLayout.setEnabled(enable);
    }

    /**
     * Adapter for contacts list.
     */
    public static class ActivityListAdapter extends ArrayAdapter<Activity> {
        public ActivityListAdapter(Context context, int resource, List<Activity> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_activity_item, parent, false);
            }

            TextView text = (TextView) convertView.findViewById(R.id.txt_activity_text);
            TextView date = (TextView) convertView.findViewById(R.id.txt_activity_date);

            Resources resources = this.getContext().getResources();
            Activity activity = getItem(position);


            long messagesId = (activity.getId() % 3) + 1;
            int resourceId = resources.getIdentifier(
                    (activity.isReceived() ? "txt_receive_" : "txt_send_") + messagesId,
                "string", getContext().getApplicationInfo().packageName);


            String messageText = resources.getString(resourceId, activity.isReceived()
                ? activity.getFriend().getName() : activity.getUser().getName());

            text.setText(Html.fromHtml(messageText));
            date.setText(DateFormat.getDateTimeInstance().format(activity.getDateCreated()));

            return convertView;
        }
    }
}
