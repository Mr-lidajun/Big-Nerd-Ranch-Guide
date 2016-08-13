package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/8/9 23:20.
 * @desc: 后台线程-下载图片
 *
 * ThumbnailDownloader的泛型参数支持任何对象，但在这里，PhotoHolder最合适，因为该视图是最终显示下载图片的地方
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PRELOAD = 1;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentHashMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;

    private LruCache<String, Bitmap> mMemoryCache;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap bitmap);
    }
    
    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
        initLruCache();
    }

    private void initLruCache() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * 在Looper首次检查消息队列之前调用，该方法成了创建Handler实现的好地方。
     */
    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                } else if (msg.what == MESSAGE_PRELOAD) {
                    //A request to preload an image for future use was created.
                    //obj is a string (with a url to the image)
                    //we just need to download it and put it in cache (if it's not there already)
                    String url = (String) msg.obj;
                    downloadImage(url);
                }

            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void preloadThumbnail(String url) {
        mRequestHandler.obtainMessage(MESSAGE_PRELOAD, url).sendToTarget();
    }

    /**
     * 通知后台线程下载图片
     * @param target 标识具体那次下载
     * @param url 下载链接
     */
    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            // Handler会将这个Message放置在Looper消息队列的尾部
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                .sendToTarget();
        }
    }

    /**
     * 清除队列中的所有请求
     */
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    /**
     * 清除队列中预加载请求
     */
    public void clearPreloadQueue() {
        mRequestHandler.removeMessages(MESSAGE_PRELOAD);
    }

    private void handleRequest(final T target) {
        final String url = mRequestMap.get(target);

        if (url == null) {
            Log.i(TAG, "handleRequest url is null");
            return;
        }

        final Bitmap bitmap = downloadImage(url);

        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRequestMap.get(target) != url || mHasQuit) {
                    Log.d(TAG, "(mRequestMap.get(target) != url) is " + (mRequestMap.get(target) != url)
                            + ", mHasQuit=" + mHasQuit);
                    return;
                }
                mRequestMap.remove(target);
                mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
            }
        });
        Log.i(TAG, "Bitmap created");
    }

    private Bitmap downloadImage(String url) {
        Bitmap bitmap;
        if (url == null) {
            Log.i(TAG, "downloadImage url is null");
            return null;
        }

        //If the image is already in cache, no need to download it, just return it.
        bitmap = mMemoryCache.get(url);
        if (bitmap != null) {
            return bitmap;
        }

        //download and cache the image. Then return it in case it's needed right away.
        try {
            byte[] bitmapBytes = new FlickFetchr().getUrlBytes(url);
            bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            addBitmapToMemoryCache(url, bitmap);
            Log.i(TAG, "Downloaded & cached image: " + url);
            return bitmap;
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
            return null;
        }
    }
}
