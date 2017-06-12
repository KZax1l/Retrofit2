package org.zsago.retrofit.adapter;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.zsago.retrofit.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Zsago on 2016/9/1.
 */
public class UploadFilesAdapter extends BaseAdapter {
    private Context mContext;
    private Map<Integer, View> mMap = new ArrayMap<>();
    private List<Map<String, Object>> mAttachments = new ArrayList<>(5);

    private final int MAX_COUNT_UPLOAD = 5;
    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_VIDEO = "video";
    public static final String KEY_MEDIA_URL = "mediaUrl";
    public static final String KEY_FILE_NAME = "fileName";
    public static final String KEY_FILE_SIZE = "fileSize";
    public static final String KEY_FILE_TYPE = "fileType";
    private final String KEY_ASYNC_DRAWABLE = "asyncDrawable";

    public UploadFilesAdapter(Context context) {
        mContext = context;
    }

    public void addItem(String type, String mediaUrl, String fileName, String fileSize) {
//        Utility.verifyNull(type, fileName, fileSize);
        if (!type.equals(TYPE_AUDIO) && !type.equals(TYPE_IMAGE) && !type.equals(TYPE_VIDEO))
            return;
        Map<String, Object> map = new ArrayMap<>(4);
        if (mediaUrl != null) {
            map.put(KEY_MEDIA_URL, mediaUrl);
        }
        map.put(KEY_FILE_NAME, fileName);
        map.put(KEY_FILE_SIZE, fileSize);
        map.put(KEY_FILE_TYPE, type);
        if (mAttachments.size() >= MAX_COUNT_UPLOAD) {
            mAttachments.set(MAX_COUNT_UPLOAD - 1, map);
        } else {
            mAttachments.add(map);
        }
        notifyDataSetChanged();
    }

    public void remove(int position) {
        mAttachments.remove(position);
        notifyDataSetChanged();
    }

    public String getMediaUrl(int position) {
        if (position < 0 || mAttachments == null || position > mAttachments.size() - 1) return null;
        return mAttachments.get(position).get(KEY_MEDIA_URL).toString();
    }

    public String getFileName(int position) {
        if (position < 0 || mAttachments == null || position > mAttachments.size() - 1) return null;
        return mAttachments.get(position).get(KEY_FILE_NAME).toString();
    }

    public String getFileSize(int position) {
        if (position < 0 || mAttachments == null || position > mAttachments.size() - 1) return null;
        return mAttachments.get(position).get(KEY_FILE_SIZE).toString();
    }

    public String getFileType(int position) {
        if (position < 0 || mAttachments == null || position > mAttachments.size() - 1) return null;
        return mAttachments.get(position).get(KEY_FILE_TYPE).toString();
    }

    public int getAttachmentSize() {
        return mAttachments.size();
    }

    @Override
    public int getCount() {
        return mAttachments.size() < MAX_COUNT_UPLOAD ? mAttachments.size() + 1 : MAX_COUNT_UPLOAD;
    }

    @Override
    public Object getItem(int position) {
        return position < mAttachments.size() ? mAttachments.get(position) : position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (!mMap.containsKey(position) || mMap.get(position) == null) {
            vh = new ViewHolder();
            convertView = vh.getView(parent);
            convertView.setTag(vh);
            mMap.put(position, convertView);
        } else {
            convertView = mMap.get(position);
            vh = (ViewHolder) convertView.getTag();
        }
        vh.index(position);
        return convertView;
    }

    private class ViewHolder {
        ImageView ivImage;
        ImageView ivDelete;
        ImageView ivPoster;

        View getView(ViewGroup viewGroup) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.grid_upload_item, viewGroup, false);
            ivImage = (ImageView) view.findViewById(R.id.iv_image);
            ivDelete = (ImageView) view.findViewById(R.id.iv_delete);
            ivPoster = (ImageView) view.findViewById(R.id.iv_poster);
            return view;
        }

        void index(final int position) {
            if (position == getCount() - 1 && mAttachments.size() < MAX_COUNT_UPLOAD) {
                ivDelete.setVisibility(View.GONE);
                ivPoster.setVisibility(View.GONE);
                ivImage.setImageResource(R.drawable.smiley_add_btn);
            } else if (position < mAttachments.size()) {
                ivPoster.setVisibility(View.GONE);
                ivDelete.setVisibility(View.VISIBLE);
                switch (getFileType(position)) {
                    case TYPE_AUDIO:
//                        ivImage.setImageResource(R.drawable.attachment_audio);
                        ImageLoader.getInstance().displayImage("drawable://" + R.mipmap.ic_audio, ivImage);
                        break;
                    case TYPE_IMAGE:
                        ImageLoader.getInstance().displayImage("file://" + getMediaUrl(position), ivImage);
                        break;
                    case TYPE_VIDEO:
                        ivPoster.setVisibility(View.VISIBLE);
//                        Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(mContext.getContentResolver(), bmp, null, null));
//                        ImageLoader.getInstance().displayImage(uri.toString(), ivImage);

                        ImageLoader.getInstance().displayImage("file://" + getMediaUrl(position), ivImage);

//                        if (getAsyncDrawable(position) == null) {
//                            Bitmap bmp = ThumbnailUtils.createVideoThumbnail(getMediaUrl(position), MediaStore.Images.Thumbnails.MICRO_KIND);
//                            addAsyncDrawable(position, new AsyncDrawable(bmp, new BitmapWorkerTask(ivImage)));
//                        }
//                        if (getAsyncDrawable(position) != null) {
//                            getAsyncDrawable(position).loadBitmap(ivImage);
//                        }
                        break;
                }
                ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        remove(position);
                        mMap.remove(position);
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }
}

