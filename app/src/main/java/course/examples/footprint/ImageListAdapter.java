package course.examples.footprint;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;



public class ImageListAdapter extends ArrayAdapter {
    private Context context;
    private LayoutInflater inflater;

    private String[] imageUrls2;
    private ArrayList<String> imageUrls;


//    public ImageListAdapter(Context context, String[] imageUrls) {
    public ImageListAdapter(Context context, ArrayList<String> imageUrls) {
        super(context, R.layout.item_photo_picker, imageUrls);

        this.context = context;
        this.imageUrls = imageUrls;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.item_photo_picker, parent, false);
        }

        Glide
            .with(context)
            .load(imageUrls.get(position))
            .into((ImageView) convertView);

        return convertView;
    }
}
