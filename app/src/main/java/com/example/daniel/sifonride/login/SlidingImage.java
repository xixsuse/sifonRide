package com.example.daniel.sifonride.login;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.daniel.sifonride.R;

import java.util.ArrayList;

public class SlidingImage extends PagerAdapter {

    private ArrayList<Integer> images;
    private LayoutInflater inflater;

    public SlidingImage (Context context, ArrayList<Integer> images){
        this.images = images;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = inflater.inflate(R.layout.sliding_image, container, false);
        ImageView slidingImage = view.findViewById(R.id.slidingImageV);
        slidingImage.setImageResource(images.get(position));
        container.addView(view, 0);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view.equals(o);
    }

}
