package org.micronurse.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.unnamed.b.atv.model.TreeNode;
import org.micronurse.R;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by zhou-shengyun on 16-10-11.
 */

public class ContactListContactHolder extends TreeNode.BaseNodeViewHolder<ContactListContactHolder.IconTextItem> {
    private Context mContext;

    public ContactListContactHolder(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public View createNodeView(TreeNode node, IconTextItem value) {
        View viewRoot = LayoutInflater.from(mContext).inflate(R.layout.item_contactlist_contact, null, false);
        viewRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView itemText = (TextView) viewRoot.findViewById(R.id.contact_name);
        CircleImageView itemIcon = (CircleImageView) viewRoot.findViewById(R.id.contact_portrait);
        itemText.setText(value.text);
        itemIcon.setImageBitmap(value.icon);
        return viewRoot;
    }

    public static class IconTextItem{
        private Bitmap icon;
        private String text;

        public IconTextItem(Bitmap icon, String text) {
            this.icon = icon;
            this.text = text;
        }
    }
}
