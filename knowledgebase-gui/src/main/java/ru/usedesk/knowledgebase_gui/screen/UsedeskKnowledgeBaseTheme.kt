
package ru.usedesk.knowledgebase_gui.screen

import androidx.annotation.ArrayRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.usedesk.knowledgebase_gui.R

class UsedeskKnowledgeBaseTheme(
    palette: Palette = Palette(),
    fonts: Fonts = Fonts(),
    val strings: Strings = Strings(),
    val drawables: Drawables = Drawables(),
    val textStyles: TextStyles = TextStyles(fonts, palette),
    val dimensions: Dimensions = Dimensions(),
    val colors: Colors = Colors(palette),
    val animationStiffness: Float = StiffnessMediumLow
) {
    fun <T> animationSpec() = spring<T>(stiffness = animationStiffness)

    class Strings(
        @StringRes val sectionsTitle: Int = R.string.usedesk_sections_title,
        @StringRes val searchPlaceholder: Int = R.string.usedesk_enter_your_query,
        @StringRes val searchCancel: Int = R.string.usedesk_cancel,
        @StringRes val loadError: Int = R.string.usedesk_sections_load_error,
        @StringRes val tryAgain: Int = R.string.usedesk_try_again,
        @StringRes val searchIsEmpty: Int = R.string.usedesk_search_fail,
        @StringRes val articleRating: Int = R.string.usedesk_rating_question,
        @StringRes val articleReviewTitle: Int = R.string.usedesk_article_review_title,
        @StringRes val articleReviewSend: Int = R.string.usedesk_send,
        @StringRes val articleReviewYes: Int = R.string.usedesk_rating_yes,
        @StringRes val articleReviewNo: Int = R.string.usedesk_rating_no,
        @StringRes val articleRatingThanks: Int = R.string.usedesk_rating_thanks,
        @StringRes val articleReviewPlaceholder: Int = R.string.usedesk_article_review_placeholder,
        @ArrayRes val reviewTags: Int = R.array.usedesk_article_review_tags
    )

    class Palette(
        val white1: Color = Color(0xFFFFFFFF),
        val white2: Color = Color(0xFFF7F7F7),
        val black2: Color = Color(0xFF333333),
        val black3: Color = Color(0xFF666666),
        val gray1: Color = Color(0xFFEEEEEE),
        val gray2: Color = Color(0xFFD5D5D5),
        val gray3: Color = Color(0xFFBCBCBC),
        val gray4: Color = Color(0xFF828282),
        val grayCold1: Color = Color(0xFFF2F4FA),
        val grayCold2: Color = Color(0xFF989FB3),
        val red: Color = Color(0xFFEB5757),
        val green: Color = Color(0xFF26BC00),
        val blue: Color = Color(0xFF2F80ED)
    )

    class Colors(
        palette: Palette,
        val rootBackground: Color = palette.white2,
        val progressBarBackground: Color = palette.white1,
        val progressBarIndicator: Color = palette.red,
        val searchBarBackground: Color = palette.gray1,
        val supportBackground: Color = palette.black2,
        val listItemBackground: Color = palette.white1,
        val sectionsIconBackground: Color = palette.grayCold1,
        val articleRatingGoodBackground: Color = palette.green.copy(alpha = 0.2f),
        val articleRatingBadBackground: Color = palette.red.copy(alpha = 0.2f),
        val articleRatingDivider: Color = palette.gray2,
        val articleReviewTagUnselectedBackground: Color = palette.gray2,
        val articleReviewTagSelectedBackground: Color = palette.black2,
        val articleReviewSendBackground: Color = palette.black2
    )

    class Drawables(
        @DrawableRes val iconBack: Int = R.drawable.usedesk_ic_back,
        @DrawableRes val iconSupport: Int = R.drawable.usedesk_ic_support,
        @DrawableRes val iconSearch: Int = R.drawable.usedesk_ic_search,
        @DrawableRes val iconSearchCancel: Int = R.drawable.usedesk_ic_cancel_round,
        @DrawableRes val iconSearchPaginationError: Int = R.drawable.usedesk_ic_error_round,
        @DrawableRes val iconListItemArrowForward: Int = R.drawable.usedesk_ic_arrow_forward,
        @DrawableRes val iconRatingGood: Int = R.drawable.usedesk_ic_rating_good,
        @DrawableRes val iconRatingBad: Int = R.drawable.usedesk_ic_rating_bad,
        @DrawableRes val iconRatingError: Int = R.drawable.usedesk_ic_error_round,
        @DrawableRes val iconReviewError: Int = R.drawable.usedesk_ic_error_round,
        @DrawableRes val imageCantLoad: Int = R.drawable.usedesk_image_cant_load
    )

    class Fonts(
        val sfProDisplay: FontFamily = FontFamily(
            Font(R.font.sf_pro_display_regular),
            Font(R.font.sf_pro_display_medium, FontWeight.Medium),
            Font(R.font.sf_pro_display_semibold, FontWeight.SemiBold),
            Font(R.font.sf_pro_display_semibold, FontWeight.Bold)
        ),
        val sfUiDisplay: FontFamily = FontFamily(
            Font(R.font.sf_ui_display_regular),
            Font(R.font.sf_ui_display_medium, FontWeight.Medium),
            Font(R.font.sf_ui_display_semibold, FontWeight.SemiBold),
            Font(R.font.sf_ui_display_semibold, FontWeight.Bold)
        ),
        val roboto: FontFamily = FontFamily(
            Font(R.font.roboto_regular),
            Font(R.font.roboto_medium, FontWeight.Medium)
        )
    )

    class TextStyles(
        fonts: Fonts,
        palette: Palette,
        val toolbarExpandedTitle: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = palette.black2
        ),
        val toolbarCollapsedTitle: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = palette.black2
        ),
        val sectionTitleItem: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = palette.grayCold2
        ),
        val sectionTextItem: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = palette.black2
        ),
        val searchText: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = palette.black2
        ),
        val searchPlaceholder: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = palette.gray4
        ),
        val searchCancel: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontWeight = FontWeight.Normal,
            fontSize = 17.sp,
            color = palette.blue
        ),
        val searchIsEmpty: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            color = palette.grayCold2
        ),
        val loadError: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = palette.black2
        ),
        val tryAgain: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            color = palette.blue
        ),
        val searchItemTitle: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = palette.black2
        ),
        val searchItemDescription: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = palette.black3
        ),
        val searchItemPath: TextStyle = TextStyle(
            fontFamily = fonts.sfUiDisplay,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = palette.grayCold2
        ),
        val categoriesTitle: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontSize = 17.sp,
            textAlign = TextAlign.Start,
            color = palette.black2
        ),
        val categoriesDescription: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontSize = 14.sp,
            textAlign = TextAlign.Start,
            color = palette.grayCold2
        ),
        val articlesItemTitle: TextStyle = TextStyle(
            fontFamily = fonts.sfProDisplay,
            fontSize = 17.sp,
            textAlign = TextAlign.Start,
            color = palette.black2
        ),
        val articleRatingTitle: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = palette.grayCold2
        ),
        val articleRatingGood: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = palette.green
        ),
        val articleRatingBad: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = palette.red
        ),
        val articleRatingThanks: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            color = palette.black3
        ),
        val articleReviewTag: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = palette.white2
        ),
        val articleReviewTagSelected: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = palette.black2
        ),
        val articleReviewCommentText: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = palette.black2
        ),
        val articleReviewCommentPlaceholder: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = palette.grayCold2
        ),
        val articleReviewSend: TextStyle = TextStyle(
            fontFamily = fonts.roboto,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            color = palette.white2
        )
    )

    class Dimensions(
        dp4: Dp = 4.dp,
        dp8: Dp = 8.dp,
        dp10: Dp = 10.dp,
        dp12: Dp = 12.dp,
        dp16: Dp = 16.dp,
        dp20: Dp = 20.dp,
        dp24: Dp = 24.dp,
        dp32: Dp = 32.dp,
        dp48: Dp = 48.dp,
        val rootPadding: Padding = Padding(dp16),
        val contentCornerRadius: Dp = dp10,
        val clickableRadius: Dp = dp24,
        val shadowElevation: Dp = 4.dp,
        val progressBarStrokeWidth: Dp = 2.dp,
        val loadingSize: Dp = dp32,
        val loadingPadding: Padding = Padding(
            start = dp16,
            end = dp16,
            top = dp16
        ),
        val paginationLoadingPadding: Padding = Padding(
            start = dp16,
            end = dp16,
            bottom = dp16
        ),
        val toolbarIconSize: Dp = dp24,
        val toolbarIntervalX: Dp = dp16,
        val toolbarIntervalY: Dp = dp16,
        val toolbarBottomPadding: Dp = dp16,
        val supportIconSize: Dp = dp24,
        val supportIconPadding: Padding = Padding(dp16),
        val notLoadedImagePadding: Padding = Padding(bottom = dp12),
        val notLoadedTitlePadding: Padding = Padding(bottom = dp8),
        val searchBarCornerRadius: Dp = dp10,
        val searchBarBottomPadding: Dp = dp16,
        val searchBarInnerPadding: Padding = Padding(dp8),
        val searchBarIconSize: Dp = dp20,
        val searchBarCancelInterval: Dp = dp12,
        val searchBarQueryPadding: Padding = Padding(horizontal = dp8),
        val sectionsItemInnerPadding: Padding = Padding(
            horizontal = dp10,
            vertical = dp8
        ),
        val sectionsItemIconSize: Dp = dp48,
        val sectionsItemTitlePadding: Padding = Padding(horizontal = dp10),
        val sectionsItemArrowSize: Dp = dp24,
        val categoriesItemInnerPadding: Padding = Padding(
            start = dp20,
            end = dp10,
            top = dp12,
            bottom = dp12
        ),
        val categoriesItemTitlePadding: Padding = Padding(end = dp10),
        val categoriesItemArrowSize: Dp = dp24,
        val articlesItemInnerPadding: Padding = Padding(
            start = dp20,
            end = dp10,
            top = dp24,
            bottom = dp24
        ),
        val articlesItemTitlePadding: Padding = Padding(
            end = dp10
        ),
        val articlesItemArrowSize: Dp = dp24,
        val searchItemInnerPadding: Padding = Padding(
            start = dp20,
            end = dp10,
            top = dp8,
            bottom = dp8
        ),
        val searchItemTitlePadding: Padding = Padding(bottom = dp4, end = dp10),
        val searchItemDescriptionPadding: Padding = Padding(bottom = dp4, end = dp10),
        val searchItemPathPadding: Padding = Padding(bottom = dp4, end = dp10),
        val searchItemArrowSize: Dp = dp24,
        val searchEmptyTopPadding: Dp = dp16,
        val articleContentInnerPadding: Padding = Padding(
            start = dp8,
            end = dp16,
            top = dp8,
            bottom = dp8
        ),
        val articleDividerHeight: Dp = 0.5.dp,
        val articleRatingPadding: Padding = Padding(dp8),
        val articleRatingTitlePadding: Padding = Padding(
            top = dp16,
            bottom = dp8
        ),
        val articleRatingButtonCornerRadius: Dp = dp4,
        val articleRatingButtonInnerPadding: Padding = Padding(
            horizontal = dp10,
            vertical = dp8
        ),
        val articleRatingButtonIconSize: Dp = dp16,
        val articleRatingButtonInnerInterval: Dp = dp12,
        val articleRatingButtonInterval: Dp = dp12,
        val articleReviewTagsBottomPadding: Dp = dp32,
        val articleReviewTagsVerticalInterval: Dp = dp10,
        val articleReviewTagsHorizontalInterval: Dp = dp10,
        val articleReviewTagCornerRadius: Dp = dp4,
        val articleReviewTagInnerPadding: Padding = Padding(
            horizontal = dp10,
            vertical = dp8
        ),
        val articleReviewCommentInnerPadding: Padding = Padding(
            horizontal = dp10,
            vertical = dp16
        ),
        val articleReviewSendCornerRadius: Dp = dp8,
        val articleReviewSendPadding: Padding = Padding(dp12),
        val articleReviewSendHeight: Dp = dp48,
        val articleReviewSendIconSize: Dp = dp24,
    ) {
        class Padding(
            val start: Dp = 0.dp,
            val end: Dp = 0.dp,
            val top: Dp = 0.dp,
            val bottom: Dp = 0.dp
        ) {
            constructor(all: Dp) : this(start = all, end = all, top = all, bottom = all)
            constructor(
                horizontal: Dp = 0.dp,
                vertical: Dp = 0.dp
            ) : this(start = horizontal, end = horizontal, top = vertical, bottom = vertical)
        }
    }

    companion object {
        var provider: () -> UsedeskKnowledgeBaseTheme = { UsedeskKnowledgeBaseTheme() }
    }
}