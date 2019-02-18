package ru.usedesk.sdk.data.framework.api.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiRetrofit {
    @GET("{account_id}/list")
    Call<String> getSections(@Path(value = "account_id", encoded = true) String accountId,
                             @Query("api_token") String token);

    @GET("{account_id}/articles/{article_id}")
    Call<String> getArticleBody(@Path(value = "account_id", encoded = true) String accountId,
                                @Path(value = "article_id", encoded = true) String articleId,
                                @Query("api_token") String token);
}
