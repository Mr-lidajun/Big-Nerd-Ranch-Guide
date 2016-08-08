package com.bignerdranch.android.photogallery;

import java.util.List;

/**
 * @author lidajun
 * @email solidajun@gmail.com
 * @date 16/8/7 11:08.
 * @desc: 模型对象类
 */
public class GalleryItemForGson {
    /**
     * page : 1
     * pages : 10
     * perpage : 100
     * total : 1000
     * photo : [{"id":"28197679054","owner":"14779630@N07","secret":"1f1d820453","server":"8792","farm":9,"title":"2016-08-06_09-46-19","ispublic":1,"isfriend":0,"isfamily":0,"url_s":"https://farm9.staticflickr.com/8792/28197679054_1f1d820453_m.jpg","height_s":"220","width_s":"240"},{"id":"28197681994","owner":"50088007@N03","secret":"d6ebe00351","server":"8776","farm":9,"title":"8_8_9_43_42","ispublic":1,"isfriend":0,"isfamily":0,"url_s":"https://farm9.staticflickr.com/8776/28197681994_d6ebe00351_m.jpg","height_s":"192","width_s":"240"}]
     */

    public PhotosBean photos;
    public String stat;

    public static class PhotosBean {
        public int page;
        public int pages;
        public int perpage;
        public int total;
        /**
         * id : 28197679054
         * owner : 14779630@N07
         * secret : 1f1d820453
         * server : 8792
         * farm : 9
         * title : 2016-08-06_09-46-19
         * ispublic : 1
         * isfriend : 0
         * isfamily : 0
         * url_s : https://farm9.staticflickr.com/8792/28197679054_1f1d820453_m.jpg
         * height_s : 220
         * width_s : 240
         */

        public List<PhotoBean> photo;

        public static class PhotoBean {
            public String id;
            public String owner;
            public String secret;
            public String server;
            public int farm;
            public String title;
            public int ispublic;
            public int isfriend;
            public int isfamily;
            public String url_s;
            public String height_s;
            public String width_s;
        }
    }
}
