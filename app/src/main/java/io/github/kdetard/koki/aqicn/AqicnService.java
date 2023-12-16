package io.github.kdetard.koki.aqicn;

import io.github.kdetard.koki.aqicn.models.AqicnResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AqicnService {
    @GET("https://api.waqi.info/feed/{cityOrStationId}/")
    Single<AqicnResponse> fromCityOrStationId(@Path("cityOrStationId") String cityOrStationId, @Query("token") String token);
}
