package com.libre.irremote.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.libre.irremote.R;

import java.util.List;

public class ConfigureSpeakerWifiListImageAdapter extends PagerAdapter {


    private Context context;
    private List<Integer> imageList;
    LayoutInflater layoutInflater;


    public ConfigureSpeakerWifiListImageAdapter(Context context, List<Integer> imageList) {
        this.context = context;
        this.imageList = imageList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == ((LinearLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View itemView = layoutInflater.inflate(R.layout.configure_speaker_wifi_image_adapter, container, false);

        AppCompatImageView ivConfigureWifiImage = itemView.findViewById(R.id.iv_configure_wifi_image);
        ivConfigureWifiImage.setImageResource(imageList.get(position));

        container.addView(itemView);


        return itemView;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
