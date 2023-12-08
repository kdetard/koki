package io.github.kdetard.koki.openremote;

import java.util.List;

import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.AssetAttribute;
import io.github.kdetard.koki.openremote.models.Dashboard;
import io.github.kdetard.koki.openremote.models.Realm;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenRemoteService {
    @POST("/api/master/asset/query")
    @Headers({
            "accept: application/json",
            "Content-Type: application/json"
    })
    Single<List<Asset<AssetAttribute>>> getAssets();

    @GET("/api/master/dashboard/all/master")
    Single<List<Dashboard>> getDashboards();

    @GET("/realm/accessible")
    Single<List<Realm>> getRealms();
}
