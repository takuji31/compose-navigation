object Libs {
    const val composeVersion = "1.0.0-alpha09"

    object Hilt {
        private const val hilt_version = "2.30.1-alpha"
        const val core = "com.google.dagger:hilt-android:$hilt_version"
        const val compiler = "com.google.dagger:hilt-android-compiler:$hilt_version"
        const val testing = "com.google.dagger:hilt-android-testing:$hilt_version"

        private const val hilt_android_version = "1.0.0-alpha01"
        const val viewModel = "androidx.hilt:hilt-lifecycle-viewmodel:$hilt_android_version"
        const val androidXCompiler = "androidx.hilt:hilt-compiler:$hilt_android_version"
    }
}
