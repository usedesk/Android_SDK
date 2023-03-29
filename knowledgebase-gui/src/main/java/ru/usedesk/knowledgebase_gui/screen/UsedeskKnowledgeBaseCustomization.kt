package ru.usedesk.knowledgebase_gui.screen

import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R

class UsedeskKnowledgeBaseCustomization(
    @StringRes val textIdSectionsTitle: Int = R.string.usedesk_sections_title,
    @StringRes val textIdSearchPlaceholder: Int = R.string.usedesk_enter_your_query,
    @StringRes val textIdSearchCancel: Int = R.string.usedesk_cancel,
    @StringRes val textIdSearchLoadError: Int = R.string.usedesk_sections_load_error,
    @StringRes val textIdSearchTryAgain: Int = R.string.usedesk_try_again,
    @StringRes val textIdSearchIsEmpty: Int = R.string.usedesk_search_fail,
    @StringRes val textIdArticleRating: Int = R.string.usedesk_rating_question,
    @StringRes val textIdArticleReviewTitle: Int = R.string.usedesk_article_review_title,
    @StringRes val textIdArticleReviewSend: Int = R.string.usedesk_send,
    @StringRes val textIdArticleReviewYes: Int = R.string.usedesk_rating_yes,
    @StringRes val textIdArticleReviewNo: Int = R.string.usedesk_rating_no,
    @StringRes val textIdArticleRatingThanks: Int = R.string.usedesk_rating_thanks,
    @StringRes val textIdArticleReviewPlaceholder: Int = R.string.usedesk_article_review_placeholder,
    @ArrayRes val arrayIdReviewTags: Int = R.array.usedesk_article_review_tags,
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
    ),
    @ColorRes val colorIdWhite1: Int = R.color.usedesk_white_1,
    @ColorRes val colorIdWhite2: Int = R.color.usedesk_white_2,
    @ColorRes val colorIdBlack2: Int = R.color.usedesk_black_2,
    @ColorRes val colorIdGray1: Int = R.color.usedesk_gray_1,
    @ColorRes val colorIdGray12: Int = R.color.usedesk_gray_12,
    @ColorRes val colorIdGray2: Int = R.color.usedesk_gray_2,
    @ColorRes val colorIdGray3: Int = R.color.usedesk_gray_3,
    @ColorRes val colorIdGrayCold1: Int = R.color.usedesk_gray_cold_1,
    @ColorRes val colorIdGrayCold2: Int = R.color.usedesk_gray_cold_2,
    @ColorRes val colorIdRed: Int = R.color.usedesk_red,
    @ColorRes val colorIdGreen: Int = R.color.usedesk_green,
    @ColorRes val colorIdBlue: Int = R.color.usedesk_blue,
) {
    @Composable
    fun textStyleSectionTitleItem() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = colorResource(colorIdGrayCold2)
    )

    @Composable
    fun textStyleSectionTextItem() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        color = colorResource(colorIdBlack2)
    )

    @Composable
    fun textStyleSearch() = TextStyle(
        fontFamily = fontSfProDisplay,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp
    )

    companion object {
        var provider: () -> UsedeskKnowledgeBaseCustomization =
            { UsedeskKnowledgeBaseCustomization() }
    }
}