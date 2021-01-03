package com.github.takuji31.compose.navigation

import android.os.Parcelable

public interface Screen<ID : ScreenId> : Parcelable {
    public val id: ID
}
