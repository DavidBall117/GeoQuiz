package com.davidrobertball.geoquiz

import androidx.annotation.StringRes

// data class just holds data, no methods
// @StringRes is used to reference a string resource
// @DrawableRes is used to reference a drawable resource
// @LayoutRes is used to reference a layout resource and so on
data class Question(@StringRes val textResId: Int, val answer: Boolean)
