package ru.usedesk.sdk.data.framework.api.retrofit;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Response;
import ru.usedesk.sdk.data.framework.api.retrofit.entity.ApiError;
import ru.usedesk.sdk.data.framework.api.retrofit.entity.ArticlesBodyPage;
import ru.usedesk.sdk.data.framework.api.retrofit.entity.ViewsAdded;
import ru.usedesk.sdk.data.repository.knowledgebase.IApiLoader;
import ru.usedesk.sdk.domain.entity.exceptions.ApiException;
import ru.usedesk.sdk.domain.entity.knowledgebase.ArticleBody;
import ru.usedesk.sdk.domain.entity.knowledgebase.SearchQuery;
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
        return executeRequest(Section[].class, apiRetrofit.getSections(accountId, token));
    }

    @NonNull
    @Override
    public ArticleBody getArticle(@NonNull String accountId, @NonNull String articleId,
                                  @NonNull String token) throws ApiException {
        return executeRequest(ArticleBody.class, apiRetrofit.getArticleBody(accountId, articleId, token));
    }

    @NonNull
    @Override
    public List<ArticleBody> getArticles(@NonNull String accountId, @NonNull String token,
                                         @NonNull SearchQuery searchQuery) throws ApiException {
        return Arrays.asList(executeRequest(ArticlesBodyPage.class,
                apiRetrofit.getArticlesBody(accountId, token,
                        searchQuery.getSearchQuery(), searchQuery.getCount(),
                        searchQuery.getCollectionIds(), searchQuery.getCategoryIds(),
                        searchQuery.getArticleIds(), searchQuery.getPage(), searchQuery.getType(),
                        searchQuery.getSort(), searchQuery.getOrder()))
                .getArticles());
    }

    @Override
    public int addViews(@NonNull String accountId, @NonNull String token, long articleId, int count) throws ApiException {
        return executeRequest(ViewsAdded.class,
                apiRetrofit.addViews(accountId, articleId, token, count))
                .getViews();
    }

    private <T> T executeRequest(@NonNull Class<T> tClass, @NonNull Call<String> call)
            throws ApiException {
        try {
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
}
