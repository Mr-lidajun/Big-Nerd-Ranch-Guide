package com.bignerdranch.android.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.net.Uri;
import android.util.Log;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/8/6 21:11.
 * @desc: 基本网络连接
 */
public class FlickFetchr {
    private static final String TAG = "FlickFetchr";
    private static final String API_KEY = "b50e0964aecf982fd03a6999f6b8b9da";
    private static final int TIME_OUT = 15000;

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(TIME_OUT);
        connection.setReadTimeout(TIME_OUT);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItem> fetchItems(int page) {

        List<GalleryItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    //.appendQueryParameter("per_page", "3")
                    .appendQueryParameter("page", Integer.toString(page))
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            //JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonString);
        } catch (JsonSyntaxException jye) {
            Log.e(TAG, "Failed to parse JSON", jye);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if (!photoJsonObject.has("url_s")) {
                continue;
            }

            item.setUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }

    private void parseItems(List<GalleryItem> items, String jsonString) throws JsonSyntaxException {
        GalleryItemForGson galleryItem = new Gson().fromJson(jsonString, GalleryItemForGson.class);
        List<GalleryItemForGson.PhotosBean.PhotoBean> photos = galleryItem.photos.photo;
        for (GalleryItemForGson.PhotosBean.PhotoBean photo : photos) {
            GalleryItem item = new GalleryItem();
            item.setId(photo.id);
            item.setCaption(photo.title);
            item.setUrl(photo.url_s);
            items.add(item);
        }
    }

}
