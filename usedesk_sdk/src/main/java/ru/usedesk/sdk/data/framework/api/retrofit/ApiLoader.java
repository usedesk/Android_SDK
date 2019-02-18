package ru.usedesk.sdk.data.framework.api.retrofit;

import android.support.annotation.NonNull;

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

    @Inject
    public ApiLoader(ApiRetrofit apiRetrofit) {
        this.apiRetrofit = apiRetrofit;
    }

    @NonNull
    @Override
    public Section[] getSections(@NonNull String accountId, @NonNull String token)
            throws ApiException {
        return executeRequest(() -> apiRetrofit.getSections(accountId, token));
    }

    @NonNull
    @Override
    public ArticleBody getArticle(@NonNull String accountId, @NonNull String articleId,
                                  @NonNull String token) throws ApiException {
        return executeRequest(() -> apiRetrofit.getArticleBody(accountId, articleId, token));
    }

    private <T> T executeRequest(@NonNull ExecutableRequest<T> executableRequest) throws ApiException {
        try {
            Call<T> call = executableRequest.getCall();
            Response<T> sectionsResponse = call.execute();

            if (sectionsResponse.isSuccessful() && sectionsResponse.body() != null) {
                return sectionsResponse.body();
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
