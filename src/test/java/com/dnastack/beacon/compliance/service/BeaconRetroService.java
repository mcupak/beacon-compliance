package com.dnastack.beacon.compliance.service;

import ga4gh.BeaconOuterClass;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
public interface BeaconRetroService {
    String BEACON_INFO_PATH = ".";
    String BEACON_REQUEST_PATH = "query";

    @GET(BEACON_INFO_PATH)
    Call<BeaconOuterClass.Beacon> getBeaconGet();

    @POST(BEACON_INFO_PATH)
    Call<ResponseBody> getBeaconPost();

    @DELETE(BEACON_INFO_PATH)
    Call<ResponseBody> getBeaconDelete();

    @PUT(BEACON_INFO_PATH)
    Call<ResponseBody> getBeaconPut();

    @GET(BEACON_REQUEST_PATH)
    Call<BeaconOuterClass.BeaconAlleleResponse> getBeaconAlleleResponseGet(@Query("referenceName") String referenceName,
                                                                           @Query("start") Long start,
                                                                           @Query("referenceBases") String referenceBases,
                                                                           @Query("alternateBases") String alternateBases,
                                                                           @Query("assemblyId") String assemblyId,
                                                                           @Query("datasetIds") List<String> datasetIds,
                                                                           @Query("includeDatasetResponses") Boolean includeDatasetResponses);

    @POST(BEACON_REQUEST_PATH)
    Call<BeaconOuterClass.BeaconAlleleResponse> getBeaconAlleleResponsePost(@Body BeaconOuterClass.BeaconAlleleRequest request);

    @DELETE(BEACON_REQUEST_PATH)
    Call<BeaconOuterClass.BeaconAlleleResponse> getBeaconAlleleResponseDelete();

    @PUT(BEACON_REQUEST_PATH)
    Call<BeaconOuterClass.BeaconAlleleResponse> getBeaconAlleleResponsePut();
}
