package io.github.kdetard.koki.openremote.Asset;

import java.util.Collections;
import java.util.List;

public class AssetQuery {
    public String id = null;
    public int version = 0;
    public long createdOn = 0L;
    public String name = null;
    public boolean accessPublicRead = false;
    public String realm = null;
    public String type = null;
    public List<String> path = Collections.emptyList();
    public AssetAttributes attributes = null;
}
