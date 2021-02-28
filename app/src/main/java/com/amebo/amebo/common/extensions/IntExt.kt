package com.amebo.amebo.common.extensions


fun Int.wrap100() = if (this >= 99) "99+" else this.toString()