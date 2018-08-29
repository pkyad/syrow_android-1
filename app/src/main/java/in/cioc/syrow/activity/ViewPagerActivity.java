package in.cioc.syrow.activity;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import in.cioc.syrow.R;
import in.cioc.syrow.adapter.ChatRoomThreadAdapter;
import in.cioc.syrow.fragments.HackyViewPager;
import in.cioc.syrow.photoview.view.PhotoView;


public class ViewPagerActivity extends Activity {

    private static final String ISLOCKED_ARG = "isLocked";
    private ViewPager mViewPager;
    private int position;
    private String imageUrl;
    private static ArrayList<String> sDrawables;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        sDrawables = new ArrayList<>();
        mViewPager = (HackyViewPager) findViewById(R.id.view_pager);
        setContentView(mViewPager);

        if (getIntent() != null) {
            position = getIntent().getIntExtra("position", 0);
            imageUrl = getIntent().getStringExtra("imageUrl");
            mViewPager.setCurrentItem(position);
            sDrawables.add(imageUrl);
        }
        mViewPager.setAdapter(new SamplePagerAdapter(imageUrl));

        if (savedInstanceState != null) {
            boolean isLocked = savedInstanceState.getBoolean(ISLOCKED_ARG, false);
            ((HackyViewPager) mViewPager).setLocked(isLocked);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    static class SamplePagerAdapter extends PagerAdapter {
        /* Here I'm adding the demo pics, but you can add your Item related pics , just get your pics based on itemID (use asynctask) and
         fill the urls in arraylist*/
//        String imageUrl;
//        private static final ArrayList<String> sDrawables = new ArrayList<>();
                //d= ChatRoomThreadAdapter..get(0).getImageUrl();//ImageUrlUtils.getImageUrls();

        public SamplePagerAdapter(String url) {
//            imageUrl = url;
//            sDrawables.add(imageUrl);
        }
//        JSONArray file = ItemDetailsActivity.lite.getImageUrl();

        @Override
        public int getCount() {
            return sDrawables.size();
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            PhotoView photoView = new PhotoView(container.getContext());
            photoView.setImageUri(sDrawables.get(position));

            // Now just add PhotoView to ViewPager and return it
            container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

    private boolean isViewPagerActive() {
        return (mViewPager != null && mViewPager instanceof HackyViewPager);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (isViewPagerActive()) {
            outState.putBoolean(ISLOCKED_ARG, ((HackyViewPager) mViewPager).isLocked());
        }
        super.onSaveInstanceState(outState);
    }

}