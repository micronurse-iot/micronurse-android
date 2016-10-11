package org.micronurse.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import org.micronurse.R;
import org.micronurse.util.GlobalInfo;
import org.micronurse.util.ImageUtil;

/**
 * Created by zhou-shengyun on 16-10-11.
 */

public class ContactListRootHolder extends TreeNode.BaseNodeViewHolder<ContactListRootHolder.IconTextItem> {
    private Context mContext;
    private AndroidTreeView mTreeView;

    public ContactListRootHolder(Context context, AndroidTreeView treeView) {
        super(context);
        mContext = context;
        mTreeView = treeView;
    }

    @Override
    public View createNodeView(final TreeNode node, IconTextItem value) {
        View viewRoot = LayoutInflater.from(mContext).inflate(R.layout.item_contactlist_root, null, false);
        viewRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView itemText = (TextView) viewRoot.findViewById(R.id.item_text);
        ImageView itemIcon = (ImageView) viewRoot.findViewById(R.id.item_icon);
        itemText.setText(value.text);
        itemIcon.setImageResource(value.iconRes);
        final ImageView arrowIcon = (ImageView) viewRoot.findViewById(R.id.item_icon_arrow);
        viewRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!node.isExpanded()){
                    mTreeView.expandNode(node);
                    arrowIcon.setImageBitmap(ImageUtil.getBitmapFromDrawable(mContext, R.drawable.ic_expand_more));
                }else{
                    mTreeView.collapseNode(node);
                    arrowIcon.setImageBitmap(ImageUtil.getBitmapFromDrawable(mContext, R.drawable.ic_chevron_right));
                }
            }
        });

        return viewRoot;
    }

    public static class IconTextItem{
        private int iconRes;
        private String text;

        public IconTextItem(int iconRes, String text) {
            this.iconRes = iconRes;
            this.text = text;
        }
    }
}
