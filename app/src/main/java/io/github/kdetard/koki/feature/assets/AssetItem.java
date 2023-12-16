package io.github.kdetard.koki.feature.assets;

import android.view.View;

import androidx.annotation.NonNull;

import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ItemAssetBinding;

public class AssetItem extends AbstractItem<AssetItem.ViewHolder> {
    String id;
    String name;
    String description;
    int iconId;

    @Override
    public int getType() {
        return R.id.asset_item_id;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_asset;
    }

    public AssetItem withId(String id) {
        this.id = id;
        return this;
    }

    public AssetItem withIconId(int iconId) {
        this.iconId = iconId;
        return this;
    }

    public AssetItem withName(String name) {
        this.name = name;
        return this;
    }

    public AssetItem withDescription(String description) {
        this.description = description;
        return this;
    }

    public AssetItem withIdentifier(long identifier) {
        this.setIdentifier(identifier);
        return this;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View view) {
        return new ViewHolder(view);
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<AssetItem> {
        ItemAssetBinding binding;

        public ViewHolder(View view) {
            super(view);
            binding = ItemAssetBinding.bind(view);
        }

        @Override
        public void bindView(@NonNull AssetItem item, @NonNull List<?> list) {
            binding.assetIcon.setImageResource(item.iconId);
            binding.assetTitle.setText(item.name);
            binding.assetDescription.setText(item.description);
        }

        @Override
        public void unbindView(@NonNull AssetItem item) {
            binding.assetTitle.setText(null);
            binding.assetDescription.setText(null);
        }
    }
}
