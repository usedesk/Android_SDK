package ru.usedesk.knowledgebase_gui.screen

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R

class UsedeskKnowledgeBaseTheme(
    val strings: Strings = Strings(),
    val colors: Colors = Colors(),
    val drawables: Drawables = Drawables(),
    val fonts: Fonts = Fonts(),
    val textStyles: TextStyles = TextStyles(fonts, colors),
    val dimensions: Dimensions = Dimensions()
) {
    class Strings(
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
        @ArrayRes val arrayIdReviewTags: Int = R.array.usedesk_article_review_tags
    )

    class Colors(
        val white1: Color = Color(0xFFFFFFFF),
        val white2: Color = Color(0xFFF7F7F7),
        val black2: Color = Color(0xFF333333),
        val black3: Color = Color(0xFF666666),
        val gray1: Color = Color(0xFFEEEEEE),
        val gray12: Color = Color(0xFFD5D5D5),
        val gray2: Color = Color(0xFFBCBCBC),
        val gray3: Color = Color(0xFF828282),
        val grayCold1: Color = Color(0xFFF2F4FA),
        val grayCold2: Color = Color(0xFF989FB3),
        val red: Color = Color(0xFFEB5757),
        val green: Color = Color(0xFF26BC00),
        val blue: Color = Color(0xFF2F80ED)
    )

    class Drawables(
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
        @DrawableRes val imageIdCantLoad: Int = R.drawable.usedesk_image_cant_load
    )

    class Fonts(
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
    )

    class TextStyles(
        val fonts: Fonts,
        val colors: Colors,
        val toolbarTitle: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            color = colors.black2
        ),
        val sectionTitleItem: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = colors.grayCold2
        ),
        val sectionTextItem: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = colors.black2
        ),
        val searchText: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = colors.black2
        ),
        val searchPlaceholder: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = colors.gray3
        ),
        val searchCancel: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = colors.blue
        ),
        val searchIsEmpty: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            color = colors.grayCold2
        ),
        val loadError: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = colors.black2
        ),
        val tryAgain: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = colors.blue
        ),
        val searchItemTitle: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = colors.black2
        ),
        val searchItemDescription: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = colors.black3
        ),
        val searchItemPath: TextStyle = TextStyle(
            fontFamily = fonts.fontSfUiDisplay,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = colors.grayCold2
        ),
        val categoriesTitle: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontSize = 17.sp,
            textAlign = TextAlign.Start,
            color = colors.black2
        ),
        val categoriesDescription: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
            color = colors.grayCold2
        ),
        val articlesItemTitle: TextStyle = TextStyle(
            fontFamily = fonts.fontSfProDisplay,
            fontSize = 17.sp,
            textAlign = TextAlign.Start,
            color = colors.black2
        ),
        val articleRatingTitle: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.grayCold2
        ),
        val articleRatingGood: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.green
        ),
        val articleRatingBad: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.red
        ),
        val articleRatingThanks: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = colors.black3
        ),
        val articleReviewTag: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.white2
        ),
        val articleReviewTagSelected: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.black2
        ),
        val articleReviewCommentText: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.black2
        ),
        val articleReviewCommentPlaceholder: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = colors.grayCold2
        ),
        val articleReviewSend: TextStyle = TextStyle(
            fontFamily = fonts.fontRoboto,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            color = colors.white2
        )
    )

    class Dimensions(
        val toolbarMinCollapsedHeight: Dp = 56.dp,
        val toolbarHorizontalPadding: Dp = 16.dp,
        val toolbarExpandedTitleBottomPadding: Dp = 8.dp,
        val toolbarCollapsedTitleLineHeight: TextUnit = 28.sp,
        val progressBarStrokeWidth: Dp = 2.dp
    )

    companion object {
        var provider: () -> UsedeskKnowledgeBaseTheme =
            { UsedeskKnowledgeBaseTheme() }
    }
}