package course.examples.footprint;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.List;


public class MyAdapter extends BaseAdapter implements Serializable
{
    List<String> IdStringList;
    List<String> TitleStringList;
    List<String> AuthorStringList;
    Context mContext;

    public MyAdapter(Context context, List<String> titleList, List<String>idList, List<String>authorList)
    {
        TitleStringList = titleList;
        AuthorStringList = authorList;
        IdStringList = idList;
        mContext=context;
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        MyViewHolder holder;
        if(convertView==null)
        {
            convertView= View.inflate(mContext,R.layout.item_marker,null);
            holder=new MyViewHolder();
            holder.txt_marker_title= convertView.findViewById(R.id.txt_marker_title);
            holder.txt_marker_author= convertView.findViewById(R.id.txt_marker_auth);
            convertView.setTag(holder);
        }
        else
        {
            holder= (MyAdapter.MyViewHolder) convertView.getTag();
        }
        holder.txt_marker_title.setText(TitleStringList.get(position));
        holder.txt_marker_author.setText(AuthorStringList.get(position));
        return convertView;
    }

    @Override
    public int getCount() {
        return TitleStringList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    public static class  MyViewHolder implements Serializable
    {
        TextView txt_marker_title;
        TextView txt_marker_author;
    }

}
