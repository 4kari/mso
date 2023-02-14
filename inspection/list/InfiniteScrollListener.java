package id.sisi.si.mso.ui.inspection.list;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by harrisfz
 * ubilih@gmail.com
 * harris.febryantony@sisi.id
 * on 10/11/17.
 */
public abstract class InfiniteScrollListener extends RecyclerView.OnScrollListener {

    LinearLayoutManager mLayoutManager;
    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private int visibleTreshold = 1;
    // Sets the starting page index
    private int startingPageIndex = 0;

    public InfiniteScrollListener(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        final int visibleItemCount = view.getChildCount();
        final int totalItemCount = mLayoutManager.getItemCount();
        final int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

        if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleTreshold)) {
            onLoadMore();
        }
    }

    // Defines the process for actually loading more data based on page
    public abstract void onLoadMore();


}