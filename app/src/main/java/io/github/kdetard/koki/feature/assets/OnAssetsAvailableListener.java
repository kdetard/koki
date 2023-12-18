package io.github.kdetard.koki.feature.assets;

import java.util.List;

import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.AssetAttribute;

public interface OnAssetsAvailableListener {
    void setAssets(List<Asset<AssetAttribute>> assets);
}
