package com.vodafone.idtmlib.lib.network;

import com.vodafone.idtmlib.lib.network.models.bodies.GetAccessTokenBody;
import com.vodafone.idtmlib.lib.network.models.bodies.RefreshAccessTokenBody;
import com.vodafone.idtmlib.lib.network.models.bodies.SetupBody;
import com.vodafone.idtmlib.lib.network.models.responses.JwtResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface IdtmApi {
    //Client Manage Controller

    @POST("sdk")
    Call<JwtResponse> setup(@Header("clientId") String clientId, @Body SetupBody setupBody);

    @GET("sdk/{sdkId}/nonce")
    Call<JwtResponse> getNonce(@Header("clientId") String clientId,
                               @Header("Authorization") String sdkIdJwtToken,
                               @Path("sdkId") String sdkId);

    @POST("sdk/{sdkId}/code")
    Call<JwtResponse> getAccessToken(@Header("clientId") String clientId,
                                     @Header("Authorization") String sdkIdJwtToken,
                                     @Path("sdkId") String sdkId,
                                     @Body GetAccessTokenBody body);

    @POST("sdk/{sdkId}/refresh")
    Call<JwtResponse> refreshAccessToken(@Header("clientId") String clientId,
                                         @Header("Authorization") String sdkIdJwtToken,
                                         @Path("sdkId") String sdkId,
                                         @Body RefreshAccessTokenBody body);

    @GET("sdk/{sdkId}")
    Call<JwtResponse> getClientDetails(@Header("Authorization") String authorization,
                                       @Header("clientId") String clientId,
                                       @Path("sdkId") String sdkId);
}
