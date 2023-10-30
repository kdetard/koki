package io.github.ktard.koki.network;

import io.github.ktard.koki.model.KeycloakToken;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.adapter.rxjava3.Result;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface KeycloakApiService {
    @POST
    @FormUrlEncoded
    Observable<Result<KeycloakToken>> grantNewAccessToken(
            @Url String url,
            @Field("client_id") String clientId,
            @Field("username") String username,
            @Field("password") String password,
            @Field("grant_type") String grantType // only supports "authorization_code" for now
    );

    @POST
    @FormUrlEncoded
    Completable endSession(
            @Url String url,
            @Field("client_id") String clientId,
            @Field("refresh_token") String refreshToken
    );

    @POST
    @FormUrlEncoded
    Observable<Result<KeycloakToken>> refreshSession(
            @Url String url,
            @Field("client_id") String clientId,
            @Field("refresh_token") String refreshToken,
            @Field("grant_type") String grantType // must be "refresh_token"
    );
}
