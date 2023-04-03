package ru.usedesk.knowledgebase_gui.screen

import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R

open class UsedeskKnowledgeBaseCustomization(
    @StringRes val textIdSectionsTitle: Int = R.string.usedesk_sections_title,
    @StringRes val textIdSearchPlaceholder: Int = R.string.usedesk_enter_your_query,
    @StringRes val textIdSearchCancel: Int = R.string.usedesk_cancel,
    @StringRes val textIdLoadError: Int = R.string.usedesk_sections_load_error,
    @StringRes val textIdTryAgain: Int = R.string.usedesk_try_again,
    @StringRes val textIdSearchIsEmpty: Int = R.string.usedesk_search_fail,
    @StringRes val textIdArticleRating: Int = R.string.usedesk_rating_question,
    @StringRes val textIdArticleReviewTitle: Int = R.string.usedesk_article_review_title,
    @StringRes val textIdArticleReviewSend: Int = R.string.usedesk_send,
    @StringRes val textIdArticleReviewYes: Int = R.string.usedesk_rating_yes,
    @StringRes val textIdArticleReviewNo: Int = R.string.usedesk_rating_no,
    @StringRes val textIdArticleRatingThanks: Int = R.string.usedesk_rating_thanks,
    @StringRes val textIdArticleReviewPlaceholder: Int = R.string.usedesk_article_review_placeholder,
    @ArrayRes val arrayIdReviewTags: Int = R.array.usedesk_article_review_tags,
    @ColorRes val colorIdWhite1: Int = R.color.usedesk_white_1,
    @ColorRes val colorIdWhite2: Int = R.color.usedesk_white_2,
    @ColorRes val colorIdBlack2: Int = R.color.usedesk_black_2,
    @ColorRes val colorIdBlack3: Int = R.color.usedesk_black_3,
    @ColorRes val colorIdGray1: Int = R.color.usedesk_gray_1,
    @ColorRes val colorIdGray12: Int = R.color.usedesk_gray_12,
    @ColorRes val colorIdGray2: Int = R.color.usedesk_gray_2,
    @ColorRes val colorIdGray3: Int = R.color.usedesk_gray_3,
    @ColorRes val colorIdGrayCold1: Int = R.color.usedesk_gray_cold_1,
    @ColorRes val colorIdGrayCold2: Int = R.color.usedesk_gray_cold_2,
    @ColorRes val colorIdRed: Int = R.color.usedesk_red,
    @ColorRes val colorIdGreen: Int = R.color.usedesk_green,
    @ColorRes val colorIdBlue: Int = R.color.usedesk_blue,
    @DrawableRes val iconIdBack: Int = R.drawable.usedesk_ic_back,
    @DrawableRes val iconIdSupport: Int = R.drawable.usedesk_ic_support,
    @DrawableRes val iconIdSearch: Int = R.drawable.usedesk_ic_search,
    @DrawableRes val iconIdSearchCancel: Int = R.drawable.usedesk_ic_cancel_round,
    @DrawableRes val iconIdSearchPaginationError: Int = R.drawable.usedesk_ic_error_round,
    @DrawableRes val iconIdListItemArrowForward: Int = R.drawable.usedesk_ic_arrow_forward,
    @DrawableRes val iconIdRatingGood: Int = R.drawable.usedesk_ic_rating_good,
    @DrawableRes val iconIdRatingBad: Int = R.drawable.usedesk_ic_rating_bad,
    @DrawableRes val iconIdRatingError: Int = R.drawable.usedesk_ic_error_round,
    @DrawableRes val iconIdReviewError: Int = R.drawable.usedesk_ic_error_round,
    @DrawableRes val imageIdCantLoad: Int = R.drawable.usedesk_image_cant_load,
    val progressBarStrokeWidth: Dp = 2.dp,
    val fontSfProDisplay: FontFamily = FontFamily(
        Font(R.font.sf_pro_display_regular),
        Font(R.font.sf_pro_display_medium, FontWeight.Medium),
        Font(R.font.sf_pro_display_semibold, FontWeight.SemiBold),
        Font(R.font.sf_pro_display_semibold, FontWeight.Bold)
    ),
    val fontSfUiDisplay: FontFamily = FontFamily(
        Font(R.font.sf_ui_display_regular),
        Font(R.font.sf_ui_display_medium, FontWeight.Medium),
        Font(R.font.sf_ui_display_semibold, FontWeight.SemiBold),
        Font(R.font.sf_ui_display_semibold, FontWeight.Bold)
    ),
    val fontRoboto: FontFamily = FontFamily(
        Font(R.font.roboto_regular),
        Font(R.font.roboto_medium, FontWeight.Medium)
    )
) {
    @Composable
    open fun textStyleSectionTitleItem() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = colorResource(colorIdGrayCold2)
    )

    @Composable
    open fun textStyleSectionTextItem() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        color = colorResource(colorIdBlack2)
    )

    @Composable
    open fun textStyleSearchText() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        color = colorResource(colorIdBlack2)
    )

    @Composable
    open fun textStyleSearchPlaceholder() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        color = colorResource(colorIdGray3)
    )

    @Composable
    open fun textStyleSearchCancel() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        color = colorResource(colorIdBlue)
    )

    @Composable
    open fun textStyleSearchIsEmpty() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        color = colorResource(colorIdGrayCold2)
    )

    @Composable
    open fun textStyleLoadError() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        color = colorResource(colorIdBlack2)
    )

    @Composable
    open fun textStyleTryAgain() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        color = colorResource(colorIdBlue)
    )

    @Composable
    open fun textStyleSearchItemTitle() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontSize = 17.sp,
        fontWeight = FontWeight.Medium,
        color = colorResource(colorIdBlack2)
    )

    @Composable
    open fun textStyleSearchItemDescription() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = colorResource(colorIdBlack3)
    )

    @Composable
    open fun textStyleSearchItemPath() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        color = colorResource(colorIdGrayCold2)
    )

    @Composable
    open fun textStyleCategoriesTitle() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontSize = 17.sp,
        textAlign = TextAlign.Start,
        color = colorResource(colorIdBlack2)
    )

    @Composable
    open fun textStyleCategoriesDescription() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontSize = 14.sp,
        textAlign = TextAlign.Start,
        color = colorResource(colorIdGrayCold2)
    )

    @Composable
    open fun textStyleArticlesItemTitle() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontSize = 17.sp,
        textAlign = TextAlign.Start,
        color = colorResource(colorIdBlack2)
    )

    @Composable
    open fun textStyleArticleRatingTitle() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = colorResource(colorIdGrayCold2)
    )

    @Composable
    open fun textStyleArticleRatingGood() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = colorResource(colorIdGreen)
    )

    @Composable
    open fun textStyleArticleRatingBad() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = colorResource(colorIdRed)
    )

    @Composable
    open fun textStyleArticleRatingThanks() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = colorResource(colorIdBlack3)
    )

    @Composable
    open fun textStyleArticleReviewTag() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = colorResource(colorIdWhite2)
    )

    @Composable
    open fun textStyleArticleReviewTagSelected() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = colorResource(colorIdBlack2)
    )

    @Composable
    open fun textStyleArticleReviewCommentText() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = colorResource(colorIdBlack2)
    )

    @Composable
    open fun textStyleArticleReviewCommentPlaceholder() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        color = colorResource(colorIdGrayCold2)
    )

    @Composable
    open fun textStyleArticleReviewSend() = TextStyle(
        fontFamily = fontRoboto,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        color = colorResource(colorIdWhite2)
    )

    companion object {
        var provider: () -> UsedeskKnowledgeBaseCustomization =
            { UsedeskKnowledgeBaseCustomization() }
    }
}