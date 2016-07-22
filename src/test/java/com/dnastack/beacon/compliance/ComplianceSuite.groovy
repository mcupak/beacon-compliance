package com.dnastack.beacon.compliance

import com.dnastack.beacon.compliance.util.ProtoJsonConverter
import com.google.protobuf.GeneratedMessage
import com.google.protobuf.util.JsonFormat
import ga4gh.BeaconOuterClass
import okhttp3.ResponseBody
import org.testng.annotations.Test
import retrofit2.Retrofit

import java.lang.reflect.Method

import static org.assertj.core.api.Assertions.assertThat
import static org.assertj.core.api.Assertions.not

/**
 * Beacon Compliance Suite.
 * The tests check various beacon logic against the specified server.
 *
 * @author Artem (tema.voskoboynick@gmail.com)
 * @version 1.0
 */
class ComplianceSuite {
    final static int OK_HTTP_STATUS = 200;
    final static int BAD_REQUEST_HTTP_STATUS = 400;

    final static String API_VERSION = "0.3.0";

    final static RetroService SERVICE

    static {
        def serverToTestUrl = System.properties.getProperty("serverToTest.url")
        if (serverToTestUrl == null) {
            serverToTestUrl = "http://localhost:8180/beacon-java/" // Default.
        }

        SERVICE = new Retrofit.Builder()
                .addConverterFactory(ProtoJsonConverter.create())
                .baseUrl(serverToTestUrl)
                .build()
                .create(RetroService.class);
    }

    /**
     * Tests if we can retrieve a beacon, and whether the beacon response is valid.
     */
    @Test
    public void testGetBeacon() {
        def beacon = SERVICE.getBeaconGet().execute().body()

        assertThat(beacon.getName()).isNotNull();
        assertThat(beacon.getApiVersion()).isEqualTo(API_VERSION);
        assertThat(beacon.getId()).isNotNull();
        assertThat(beacon.hasOrganization());
        assertThat(beacon.getDatasetsCount()).isGreaterThanOrEqualTo(1);
    }

    /**
     * Ensures that posts to the beacon endpoint are not supported.
     */
    @Test
    public void testPostBeaconNotSupported() {
        def statusCode = SERVICE.getBeaconPost().execute().code()
        assertThat(statusCode).isNotEqualTo(OK_HTTP_STATUS)
    }

    /**
     * Ensures that deletes to the beacon endpoint are not supported.
     */
    @Test
    public void testDeleteBeaconNotSupported() {
        def statusCode = SERVICE.getBeaconDelete().execute().code()
        assertThat(statusCode).isNotEqualTo(OK_HTTP_STATUS)
    }

    /**
     * Ensures that puts to the beacon endpoint are not supported.
     */
    @Test
    public void testPutBeaconNotSupported() {
        def statusCode = SERVICE.getBeaconPut().execute().code()
        assertThat(statusCode).isNotEqualTo(OK_HTTP_STATUS)
    }

    /**
     * Tests you can get a BeaconAlleleResponse to /query endpoint, and that it complies
     * with the current beacon specification. Uses the sampleAlleleRequest provided by the beacon.
     */
    @Test
    public void testGetAllele() throws InterruptedException {
        def beacon = SERVICE.getBeaconGet().execute().body()
        def request = beacon.getSampleAlleleRequests(0)

        def response = SERVICE.getBeaconAlleleResponseGet(
                request.getReferenceName(),
                request.getStart(),
                request.getReferenceBases(),
                request.getAlternateBases(),
                request.getAssemblyId(),
                request.getDatasetIdsList(),
                request.getIncludeDatasetResponses())
                .execute().body()

        assertThat(response.hasAlleleRequest());
        assertThat(response.getExists()).isTrue();
        if (request.getIncludeDatasetResponses()) {
            assertThat(response.getDatasetAlleleResponsesList()).isNotEmpty();
        }
        assertThat(response.getBeaconId()).isEqualTo(beacon.getId());
        assertThat(not(response.hasError()));
    }

    /**
     * Tests you can post a BeaconAlleleResponse to /query endpoint, and that it complies
     * with the current beacon specification. Uses the sampleAlleleRequest provided by the beacon.
     */
    @Test
    public void testPostAllele() {
        def beacon = SERVICE.getBeaconGet().execute().body()
        def request = beacon.getSampleAlleleRequests(0)

        def response = SERVICE.getBeaconAlleleResponsePost(request).execute().body()

        assertThat(response.getAlleleRequest()).isEqualTo(request);
        assertThat(response.getExists()).isTrue();
        if (request.getIncludeDatasetResponses()) {
            assertThat(response.getDatasetAlleleResponsesList()).isNotEmpty();
        }
        assertThat(response.getBeaconId()).isEqualTo(beacon.getId());
        assertThat(not(response.hasError()));
    }

    /**
     * Tests Delete is not supported.
     */
    @Test
    public void testDeleteAlleleNotSupported() {
        def statusCode = SERVICE.getBeaconAlleleResponseDelete().execute().code()
        assertThat(statusCode).isNotEqualTo(OK_HTTP_STATUS)
    }

    /**
     * Tests  put is not supported.
     */
    @Test
    public void testPutAlleleNotSupported() {
        def statusCode = SERVICE.getBeaconAlleleResponsePut().execute().code()
        assertThat(statusCode).isNotEqualTo(OK_HTTP_STATUS)
    }

    /**
     * Tests a post with an invalid request returns a beacon error.
     */
    @Test
    public void testPostInvalidRequest() {
        def request = getSampleAlleleRequest().toBuilder()
                .setReferenceName("")
                .setReferenceBases("")
                .build()

        def rawResponse = SERVICE.getBeaconAlleleResponsePost(request).execute()
        def response = fromJson(rawResponse.errorBody(), BeaconOuterClass.BeaconAlleleResponse.class)

        assertThat(response.getExists()).isFalse();
        assertThat(response.hasError());
        assertThat(response.getError().getErrorCode()).isEqualTo(BAD_REQUEST_HTTP_STATUS);
    }

    /**
     * Tests a get with missing required params returns a BeaconError.
     */
    @Test
    public void testGetAlleleWithMissingRequiredParams() {
        def request = getSampleAlleleRequest();

        def rawResponse = SERVICE.getBeaconAlleleResponseGet(
                request.getReferenceName(),
                null,
                null,
                request.getAlternateBases(),
                request.getAssemblyId(),
                request.getDatasetIdsList(),
                request.getIncludeDatasetResponses())
                .execute()
        def response = fromJson(rawResponse.errorBody(), BeaconOuterClass.BeaconAlleleResponse.class)

        assertThat(response.getExists()).isFalse();
        assertThat(response.hasError());
        assertThat(response.getError().getErrorCode()).isEqualTo(BAD_REQUEST_HTTP_STATUS);
    }

    /**
     * Tests we can still get an allele response if optional parameters are not defined.
     */
    @Test
    public void testGetAlleleWithMissingOptionalParams() {
        def request = getSampleAlleleRequest().toBuilder()
                .setIncludeDatasetResponses(false)
                .build()

        def response = SERVICE.getBeaconAlleleResponseGet(
                request.getReferenceName(),
                request.getStart(),
                request.getReferenceBases(),
                request.getAlternateBases(),
                request.getAssemblyId(),
                request.getDatasetIdsList(),
                request.getIncludeDatasetResponses())
                .execute().body()

        assertThat(response.getAlleleRequest()).isEqualTo(request);
        assertThat(response.getExists()).isTrue();
        assertThat(response.getDatasetAlleleResponsesList()).isNullOrEmpty();
    }

    /**
     * Tests we can retrieve an allele with the datasets listed.
     */
    @Test
    public void testGetAlleleWithDataSets() {
        def request = getSampleAlleleRequest().toBuilder()
                .setIncludeDatasetResponses(true)
                .build()

        def response = SERVICE.getBeaconAlleleResponseGet(
                request.getReferenceName(),
                request.getStart(),
                request.getReferenceBases(),
                request.getAlternateBases(),
                request.getAssemblyId(),
                request.getDatasetIdsList(),
                request.getIncludeDatasetResponses())
                .execute().body()

        assertThat(response.getAlleleRequest()).isEqualTo(request);
        assertThat(response.getExists()).isTrue();
        assertThat(response.getDatasetAlleleResponsesList()).isNotEmpty()
    }

    /**
     * Tests we can retrieve an allele response without the datasets listed.
     */
    @Test
    public void testGetAlleleWithoutDatasets() {
        def request = getSampleAlleleRequest().toBuilder()
                .setIncludeDatasetResponses(false)
                .build()

        def response = SERVICE.getBeaconAlleleResponseGet(
                request.getReferenceName(),
                request.getStart(),
                request.getReferenceBases(),
                request.getAlternateBases(),
                request.getAssemblyId(),
                request.getDatasetIdsList(),
                request.getIncludeDatasetResponses())
                .execute().body()

        assertThat(response.getAlleleRequest()).isEqualTo(request);
        assertThat(response.getExists()).isTrue();
        assertThat(response.getDatasetAlleleResponsesList()).isNullOrEmpty();
    }

    private BeaconOuterClass.BeaconAlleleRequest getSampleAlleleRequest() {
        BeaconOuterClass.Beacon beacon = SERVICE.getBeaconGet().execute().body()
        return beacon.getSampleAlleleRequests(0)
    }

    /**
     * Converts error response body to protobuf DTO.
     * <p>
     * This is because successful bodies are automatically converted to protobuf DTOs by ProtoJsonConverter,
     * but error bodies should be converted manually when required.
     */
    private static <T> T fromJson(ResponseBody responseBody, Class<T> clazz) {
        Method newBuilderMethod = clazz.getMethod("newBuilder");
        GeneratedMessage.Builder newBuilder = (GeneratedMessage.Builder) newBuilderMethod.invoke(null, null)

        def json = responseBody.string()
        JsonFormat.parser().merge(json, newBuilder)
        return (T) newBuilder.build()
    }
}
