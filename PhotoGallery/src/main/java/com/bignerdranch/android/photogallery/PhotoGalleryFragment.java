package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/8/6 18:27.
 * @desc: ${todo}
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private GridLayoutManager mLayoutManager;

    private int spanCount = 3;
    private static final int COL_WIDTH = 300;
    private int lastFetchedPage = 1;
    private boolean loading = true;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);// 保留fragment，思考为什么要保留fragment？答案会在23.7节揭晓
        new FetchItemsTask().execute(lastFetchedPage);

        /*
         * 关联使用反馈Handler
         * Handler默认与当前线程的Looper相关联，这个Handler是在onCreate(...)方法中创建的，
         * 因此它会与主线程的Looper相关联（已传出Handler负责处理的所有消息都将在主线程的消息队列中处理）
         */
        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                if (bitmap != null) {
                    BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
                    photoHolder.bindDrawable(drawable);
                }
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        mPhotoRecyclerView.setLayoutManager(mLayoutManager);
        mPhotoRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int numColumns = mPhotoRecyclerView.getWidth() / COL_WIDTH;
                GridLayoutManager layoutManager = (GridLayoutManager)mPhotoRecyclerView.getLayoutManager();
                layoutManager.setSpanCount(numColumns);
            }
        });

        /**
         * 参考：Solution Challenge 2: paging
         * https://forums.bignerdranch.com/t/solution-challenge-2-paging/7839
         */
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) { //check for scroll down
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                    if (loading) {
                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                            loading = false;
                            Log.d(TAG, "Last Item Wow !");
                            new FetchItemsTask().execute(lastFetchedPage);
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        mThumbnailDownloader.clearPreloadQueue();
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        GridLayoutManager gridLayoutManager =
                                (GridLayoutManager) mPhotoRecyclerView.getLayoutManager();
                        PhotoAdapter photoAdapter = (PhotoAdapter) mPhotoRecyclerView.getAdapter();
                        int startingPos = gridLayoutManager.findLastVisibleItemPosition() + 1;
                        int upperLimit = Math.min(startingPos + 10, photoAdapter.getItemCount());
                        for (int i = startingPos; i < upperLimit; i++) {
                            mThumbnailDownloader.preloadThumbnail(
                                    photoAdapter.getGalleryItem(i).getUrl());
                        }

                        startingPos = gridLayoutManager.findFirstVisibleItemPosition() - 1;
                        int lowerLimit = Math.max(startingPos - 10, 0);
                        for (int i = startingPos; i > lowerLimit; i--) {
                            mThumbnailDownloader.preloadThumbnail(
                                    photoAdapter.getGalleryItem(i).getUrl());
                        }

                        break;
                }
            }
        });
        setupAdapter();
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 如果用户旋转屏幕，因PhotoHolder视图的失效，ThumbnailDownloader可能会挂起。
        // 如果点击这些ImageView，就会发生异常。
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 结束线程，这非常关键，如不终止HandlerThread，它会一直运行下去
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {
        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView = (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }
        
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }
    
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            loadBitmap(galleryItem.getUrl(), holder);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

        public GalleryItem getGalleryItem(int position) {
            return mGalleryItems.get(position);
        }
    }

    private void loadBitmap(String url, PhotoHolder holder) {
        final Bitmap bitmap = mThumbnailDownloader.getBitmapFromMemCache(url);
        if (bitmap != null) {
            Log.i(TAG, "Loaded image from cache");
            BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
            holder.bindDrawable(drawable);
        } else {
            Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.bill_up_close, null);
            holder.bindDrawable(drawable);
            mThumbnailDownloader.queueThumbnail(holder, url);
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            return new FlickFetchr().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            if (lastFetchedPage > 1) {
                mItems.addAll(items);
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            } else {
                mItems = items;
                setupAdapter();
            }
            lastFetchedPage++;
            loading = true;
        }
    }
}
