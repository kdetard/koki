package io.github.kdetard.koki.openremote.models;

import android.view.View;

import androidx.annotation.NonNull;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.Collections;
import java.util.List;

public class AssetQuery extends AbstractItem<AssetQuery.ViewHolder> {
    public String id = null;
    public int version = 0;
    public long createdOn = 0L;
    public String name = null;
    public boolean accessPublicRead = false;
    public String realm = null;
    public String type = null;
    public List<String> path = Collections.emptyList();
    public AssetAttributes attributes = null;

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public int getLayoutRes() {
        return 0;
    }

    @NonNull
    @Override
    public AssetQuery.ViewHolder getViewHolder(@NonNull final View view) {
        return new AssetQuery.ViewHolder(view);
    }

    public class ViewHolder extends FastAdapter.ViewHolder<AssetQuery> {
        public ViewHolder(final android.view.View view) {
            super(view);
        }

        @Override
        public void bindView(@NonNull AssetQuery item, @NonNull List<?> list) {
            id = item.id;
            version = item.version;
            createdOn = item.createdOn;
            name = item.name;
            accessPublicRead = item.accessPublicRead;
            realm = item.realm;
            type = item.type;
            path = item.path;
            attributes = item.attributes;
        }

        @Override
        public void unbindView(@NonNull AssetQuery item) {
            id = null;
            version = 0;
            createdOn = 0L;
            name = null;
            accessPublicRead = false;
            realm = null;
            type = null;
            path = Collections.emptyList();
            attributes = null;
        }
    }
}
