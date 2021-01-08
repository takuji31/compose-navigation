package com.github.takuji31.compose.navigation

import android.os.Parcelable

/**
 * Interface representing the screen.
 *
 * In many cases its implementation would be sealed class.
 */
public interface Screen<ID : ScreenId> : Parcelable {
    public val id: ID
}
