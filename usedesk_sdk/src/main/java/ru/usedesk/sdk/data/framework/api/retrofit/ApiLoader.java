package ru.usedesk.sdk.data.framework.api.retrofit;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Response;
import ru.usedesk.sdk.data.repository.knowledgebase.IApiLoader;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.Section;

public class ApiLoader implements IApiLoader {

    private ApiRetrofit apiRetrofit;
    private Gson gson;

    @Inject
    ApiLoader(ApiRetrofit apiRetrofit, Gson gson) {
        this.apiRetrofit = apiRetrofit;
        this.gson = gson;
    }

    @NonNull
    @Override
    public Section[] getSections(@NonNull String accountId, @NonNull String token)
            throws ApiException {
        return executeRequest(Section[].class,
                () -> apiRetrofit.getSections(accountId, token));
    }

    @NonNull
    @Override
    public ArticleBody getArticle(@NonNull String accountId, @NonNull String articleId,
                                  @NonNull String token) throws ApiException {
        return executeRequest(ArticleBody.class,
                () -> apiRetrofit.getArticleBody(accountId, articleId, token));
    }

    private <T> T executeRequest(@NonNull Class<T> tClass,
                                 @NonNull ExecutableRequest<String> executableRequest) throws ApiException {
        try {
            Call<String> call = executableRequest.getCall();
            Response<String> sectionsResponse = call.execute();

            if (sectionsResponse.isSuccessful() && sectionsResponse.body() != null) {
                try {
                    return gson.fromJson(sectionsResponse.body(), tClass);
                } catch (JsonSyntaxException | IllegalStateException e) {
                    ApiError apiError = gson.fromJson(sectionsResponse.body(), ApiError.class);
                    throw new ApiException(apiError.getError());
                }
            }
        } catch (IOException | JsonSyntaxException | IllegalStateException e) {
            e.printStackTrace();
            throw new ApiException(e.getMessage());
        }
        throw new ApiException("Unhandled response");
    }

    interface ExecutableRequest<T> {
        @NonNull
        Call<T> getCall();
    }
}
