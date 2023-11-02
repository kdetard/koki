package io.github.kdetard.koki.network;

import io.github.kdetard.koki.model.Keycloak.KeycloakToken;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface KeycloakApiService {
    @GET
    Single<ResponseBody> stepOnSignInPage(
            @Url String url,
            @Query("client_id") String clientId,
            @Query("redirect_uri") String username,
            @Query("response_type") String responseType // only supports "code" for now
    );

    @GET
    Single<ResponseBody> stepOnSignUpPage(@Url String url);

    @POST
    @FormUrlEncoded
    Single<KeycloakToken> newSession(
            @Url String url,
            @Field("client_id") String clientId,
            @Field("username") String username,
            @Field("password") String password,
            @Field("grant_type") String grantType // only supports "password" for now
    );

    @POST
    @FormUrlEncoded
    Single<ResponseBody> createUser(
            @Url String url,
            @Field("username") String username,
            @Field("email") String email,
            @Field("password") String password,
            @Field("password-confirm") String confirmPassword,
            @Field("register") String register // default is empty
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
    Single<KeycloakToken> refreshSession(
            @Url String url,
            @Field("client_id") String clientId,
            @Field("refresh_token") String refreshToken,
            @Field("grant_type") String grantType // must be "refresh_token"
    );
}
