package ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import ru.usedesk.common_sdk.external.entity.exceptions.UsedeskHttpException;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskArticleBody;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSearchQuery;
import ru.usedesk.knowledgebase_sdk.external.entity.UsedeskSection;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.entity.ApiError;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.entity.ArticlesBodyPage;
import ru.usedesk.knowledgebase_sdk.internal.data.framework.retrofit.entity.ViewsAdded;

public class ApiLoader implements IApiLoader {
    private static final String SERVER_ERROR = "111";
    private static final String INVALID_TOKEN = "112";
    private static final String ACCESS_ERROR = "115";

    private ApiRetrofit apiRetrofit;
    private Gson gson;

    @Inject
    ApiLoader(@NonNull ApiRetrofit apiRetrofit,
              @NonNull Gson gson) {
        this.apiRetrofit = apiRetrofit;
        this.gson = gson;
    }

    @NonNull
    @Override
    public UsedeskSection[] getSections(@NonNull String accountId, @NonNull String token)
            throws UsedeskHttpException {
        return executeRequest(UsedeskSection[].class, apiRetrofit.getSections(accountId, token));
    }

    @NonNull
    @Override
    public UsedeskArticleBody getArticle(@NonNull String accountId, @NonNull String articleId,
                                         @NonNull String token) throws UsedeskHttpException {
        return executeRequest(UsedeskArticleBody.class, apiRetrofit.getArticleBody(accountId, articleId, token));
    }

    @NonNull
    @Override
    public List<UsedeskArticleBody> getArticles(@NonNull String accountId, @NonNull String token,
                                                @NonNull UsedeskSearchQuery searchQuery) throws UsedeskHttpException {
        return Arrays.asList(executeRequest(ArticlesBodyPage.class,
                apiRetrofit.getArticlesBody(accountId, token,
                        searchQuery.getSearchQuery(), searchQuery.getCount(),
                        searchQuery.getCollectionIds(), searchQuery.getCategoryIds(),
                        searchQuery.getArticleIds(), searchQuery.getPage(), searchQuery.getType(),
                        searchQuery.getSort(), searchQuery.getOrder()))
                .getArticles());
    }

    @Override
    public int addViews(@NonNull String accountId, @NonNull String token, long articleId, int count) throws UsedeskHttpException {
        return executeRequest(ViewsAdded.class,
                apiRetrofit.addViews(accountId, articleId, token, count))
                .getViews();
    }

    private <T> T executeRequest(@NonNull Class<T> tClass, @NonNull Call<ResponseBody> call)
            throws UsedeskHttpException {
        try {
            Response<ResponseBody> sectionsResponse = call.execute();

            if (sectionsResponse.isSuccessful() && sectionsResponse.body() != null) {
                try {
                    return gson.fromJson(sectionsResponse.body().string(), tClass);
                } catch (JsonSyntaxException | IllegalStateException e) {
                    ApiError apiError = gson.fromJson(sectionsResponse.body().string(), ApiError.class);
                    UsedeskHttpException usedeskHttpException;
                    switch (apiError.getCode()) {
                        case SERVER_ERROR:
                            usedeskHttpException = new UsedeskHttpException(UsedeskHttpException.Error.SERVER_ERROR, apiError.getError());
                            break;
                        case INVALID_TOKEN:
                            usedeskHttpException = new UsedeskHttpException(UsedeskHttpException.Error.INVALID_TOKEN, apiError.getError());
                            break;
                        case ACCESS_ERROR:
                            usedeskHttpException = new UsedeskHttpException(UsedeskHttpException.Error.ACCESS_ERROR, apiError.getError());
                            break;
                        default:
                            usedeskHttpException = new UsedeskHttpException(apiError.getError());
                            break;
                    }
                    throw usedeskHttpException;
                }
            }
        } catch (IOException | IllegalStateException e) {
            throw new UsedeskHttpException(UsedeskHttpException.Error.IO_ERROR, e.getMessage());
        } catch (JsonSyntaxException e) {
            throw new UsedeskHttpException(UsedeskHttpException.Error.JSON_ERROR, e.getMessage());
        }
        throw new UsedeskHttpException("Unhandled response");
    }
}
