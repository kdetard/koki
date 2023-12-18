package io.github.kdetard.koki.openremote;

import java.util.List;

import io.github.kdetard.koki.openremote.models.Asset;
import io.github.kdetard.koki.openremote.models.AssetAttribute;
import io.github.kdetard.koki.openremote.models.Dashboard;
import io.github.kdetard.koki.openremote.models.Datapoint;
import io.github.kdetard.koki.openremote.models.DatapointQuery;
import io.github.kdetard.koki.openremote.models.Realm;
import io.github.kdetard.koki.openremote.models.User;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OpenRemoteService {
    @GET("/api/master/user/user")
    Single<User> getUser();

    @GET("/api/master/asset/{assetId}")
    Single<Asset<AssetAttribute>> getAsset(@Path("assetId") String assetId);

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

    @POST("/api/master/asset/datapoint/{assetId}/attribute/{attributeName}")
    Single<List<Datapoint>> getDatapoint(
        @Path("assetId") String assetId,
        @Path("attributeName") String attributeName,
        @Body DatapointQuery body
    );
}
