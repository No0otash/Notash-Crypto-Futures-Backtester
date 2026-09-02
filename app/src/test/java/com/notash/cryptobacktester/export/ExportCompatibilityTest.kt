package com.notash.cryptobacktester.export

import kotlin.test.Test
import kotlin.test.assertEquals

class ExportCompatibilityTest {
    @Test
    fun choosesMediaStoreOnlyWhereDownloadsCollectionIsAvailable() {
        assertEquals(ExportStorageMode.APP_EXTERNAL_FILES, exportStorageModeForSdk(23))
        assertEquals(ExportStorageMode.APP_EXTERNAL_FILES, exportStorageModeForSdk(28))
        assertEquals(ExportStorageMode.MEDIA_STORE_DOWNLOADS, exportStorageModeForSdk(29))
        assertEquals(ExportStorageMode.MEDIA_STORE_DOWNLOADS, exportStorageModeForSdk(35))
    }
}
