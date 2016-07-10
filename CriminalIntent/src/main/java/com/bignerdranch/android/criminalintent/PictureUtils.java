package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.util.Log;
import java.io.IOException;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/7/7 14:32.
 * @desc: 缩放图片
 */
public class PictureUtils {
    private static final String TAG = "PictureUtils";

    /**
     * 缩放
     * @param path
     * @param destWidth
     * @param destHeight
     * @return
     */
    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        // Read in the dimensions of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        // Figure out how much to scale down by
        int inSampleSize = 1;
        if (srcWidth > destWidth || srcHeight > destHeight) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // Read in and create final bitmap
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     *
     * 问题总是接踵而来。解决了缩放问题，又冒出了新问题：fragment刚启动时，PhotoView究竟有多大无人知道。
     * onCreate(...)、onStart()和onResume()方法启动后，才会有首个实例化布局出现。也就在此时，显示在屏幕上的视图才会有大小尺寸。这也是出现新问题的原因。
     * 解决方案有两个：要么等布局实例化完成并显示，要么干脆使用保守估算值。特定条件下，尽管估算比较主观，但确实是唯一切实可行的办法。
     * 再添加一个getScaledBitmap(String, Activity)静态Bitmap估算方法
     *
     * 方案二：静态Bitmap估算方法
     *
     * 该方法先确认屏幕的尺寸，然后按此缩放图像。这样，就能保证载入的ImageView永远不会过大。
     * 看到没有，无论如何，这是一个比较保守的估算有时就是能解决问题。
     * @param path
     * @param activity
     * @return
     */
    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

    /**
     * Quesion: ImageView showing in landscape but not portrait
     * https://forums.bignerdranch.com/t/imageview-showing-in-landscape-but-not-portrait/7689
     * @param path
     * @return
     */
    public static Bitmap scaleDownAndRotatePic(String path) {
        int orientation;
        if (path == null) {
            return null;
        }

        // decode image size
        BitmapFactory.Options options1 = new BitmapFactory.Options();
        options1.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options1);
        // Find the correct scale value. It should be the power of 2.
        int REQUIRED_SIZE = 70;
        int width_temp = options1.outWidth;
        int height_temp = options1.outHeight;
        int scale = 0;
        while (true) {
            if (width_temp / 2 < REQUIRED_SIZE || height_temp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_temp /= 2;
            height_temp /= 2;
            scale ++;
        }

        // decode with inSampleSize
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inSampleSize = scale;
        Bitmap bm = BitmapFactory.decodeFile(path, options2);
        Bitmap bitmap = bm;

        try {
            ExifInterface exif = new ExifInterface(path);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d(TAG, "ExifInteface rotation =" + orientation);

            //exif.setAttribute(ExifInterface.ORIENTATION_ROTATE_90, 90);
            Matrix m = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                m.postRotate(180);
                //m.postScale((float) bm.getWidth(), (float) bm.getHeight());
                // if(m.preRotate(90)){
                Log.d(TAG, "in orientation=" + orientation);
                bitmap =
                        Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bitmap;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                m.postRotate(90);
                Log.d(TAG, "in orientation=" + orientation);
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),bm.getHeight(), m, true);
                return bitmap;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                m.postRotate(270);
                Log.d(TAG, "in orientation=" + orientation);
                bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),bm.getHeight(), m, true);
                return bitmap;
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据目标图片大小来计算Sample图片大小
     *
     * 有下面一些因素需要考虑：
     * 1. 评估加载完整图片所需要耗费的内存。
     * 2. 程序在加载这张图片时可能涉及到的其他内存需求。
     * 3. 呈现这张图片的控件的尺寸大小。
     * 4. 屏幕大小与当前设备的屏幕密度。
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 高效加载大图
     * http://hukai.me/android-training-course-in-chinese/graphics/displaying-bitmaps/load-bitmap.html
     *
     * @param path
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String path,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}
