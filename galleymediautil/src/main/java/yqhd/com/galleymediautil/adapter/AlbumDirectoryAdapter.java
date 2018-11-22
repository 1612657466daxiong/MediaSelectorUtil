package yqhd.com.galleymediautil.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;


import java.util.ArrayList;
import java.util.List;

import yqhd.com.galleymediautil.R;
import yqhd.com.galleymediautil.config.PictureMimeType;
import yqhd.com.galleymediautil.entity.LocalMedia;
import yqhd.com.galleymediautil.entity.LocalMediaFolder;

/**
 * Created by chenpengxiang on 2018/9/13
 */
public class AlbumDirectoryAdapter extends RecyclerView.Adapter<AlbumDirectoryAdapter.ViewHolder> {
    private Context mContext;
    private List<LocalMediaFolder> folders = new ArrayList<>();
    private int mimeType;

    public AlbumDirectoryAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    public void  bindFolderData(List<LocalMediaFolder> folders){
        this.folders = folders;
        notifyDataSetChanged();
    }

    public void setMimeType(int mimeType) {
        this.mimeType = mimeType;
    }

    public List<LocalMediaFolder> getFolders() {
        return folders;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.picture_album_folder_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final LocalMediaFolder folder = folders.get(position);
        String name = folder.getName();
        int imageNum = folder.getImageNum();
        String imagePath = folder.getFirstImagePath();
        boolean isChecked = folder.isChecked();
        int checkedNum = folder.getCheckedNum();
        holder.tv_sign.setVisibility(checkedNum > 0 ? View.VISIBLE : View.INVISIBLE);
        if (mimeType == PictureMimeType.ofAudio()){
            holder.first_image.setImageResource(R.drawable.audio_placeholder);
        }else {
            Glide.with(mContext)
                    .load(imagePath)
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.zhanweitu)
                    .into(new BitmapImageViewTarget(holder.first_image){
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.
                                            create(mContext.getResources(), resource);
                            circularBitmapDrawable.setCornerRadius(8);
                            holder.first_image.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }
       holder.itemView.setSelected(isChecked);
        holder.image_num.setText("(" + imageNum + ")");
        holder.tv_folder_name.setText(name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    for (LocalMediaFolder mediaFolder : folders) {
                        mediaFolder.setChecked(false);
                    }
                    folder.setChecked(true);
                    notifyDataSetChanged();
                    onItemClickListener.onItemClick(folder.getName(), folder.getImages());
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView first_image;
        TextView tv_folder_name, image_num, tv_sign;

        public ViewHolder(View itemView) {
            super(itemView);
            first_image = (ImageView) itemView.findViewById(R.id.first_image);
            tv_folder_name = (TextView) itemView.findViewById(R.id.tv_folder_name);
            image_num = (TextView) itemView.findViewById(R.id.image_num);
            tv_sign = (TextView) itemView.findViewById(R.id.tv_sign);
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(String folderName, List<LocalMedia> images);
    }
}
