package com.github.takuji31.compose.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import java.util.*

/**
 * Inspire From [androidx.navigation.NavControllerViewModel]
 */
class NavControllerViewModel : ViewModel() {
    private val mViewModelStores = HashMap<UUID, ViewModelStore>()
    fun clear(backStackEntryUUID: UUID) {
        // Clear and remove the NavGraph's ViewModelStore
        val viewModelStore = mViewModelStores.remove(backStackEntryUUID)
        viewModelStore?.clear()
    }

    override fun onCleared() {
        for (store in mViewModelStores.values) {
            store.clear()
        }
        mViewModelStores.clear()
    }

    fun getViewModelStore(backStackEntryUUID: UUID): ViewModelStore {
        var viewModelStore = mViewModelStores[backStackEntryUUID]
        if (viewModelStore == null) {
            viewModelStore = ViewModelStore()
            mViewModelStores[backStackEntryUUID] = viewModelStore
        }
        return viewModelStore
    }

    override fun toString(): String {
        val sb = StringBuilder("NavControllerViewModel{")
        sb.append(Integer.toHexString(System.identityHashCode(this)))
        sb.append("} ViewModelStores (")
        val viewModelStoreIterator: Iterator<UUID> = mViewModelStores.keys.iterator()
        while (viewModelStoreIterator.hasNext()) {
            sb.append(viewModelStoreIterator.next())
            if (viewModelStoreIterator.hasNext()) {
                sb.append(", ")
            }
        }
        sb.append(')')
        return sb.toString()
    }

    companion object {
        private val FACTORY: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val viewModel = NavControllerViewModel()
                return viewModel as T
            }
        }

        fun getInstance(viewModelStore: ViewModelStore): NavControllerViewModel {
            val viewModelProvider = ViewModelProvider(viewModelStore, FACTORY)
            return viewModelProvider[NavControllerViewModel::class.java]
        }
    }
}
