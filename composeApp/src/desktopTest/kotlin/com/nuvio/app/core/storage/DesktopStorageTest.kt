package com.nuvio.app.core.storage

import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DesktopStorageTest {
    @Test
    fun boatflix_preferences_do_not_modify_a_nuvio_store() {
        if (System.getenv("BOATFLIX_STORAGE_ISOLATION_TEST") != "true") return
        val temporaryRoot = Path.of(checkNotNull(System.getenv("RUNNER_TEMP"))).toAbsolutePath().normalize()
        val appData = Path.of(checkNotNull(System.getenv("APPDATA"))).toAbsolutePath().normalize()
        check(appData.startsWith(temporaryRoot)) { "This test requires disposable application data" }
        assertEquals(appData.resolve("BOATFLIX"), DesktopStorage.rootDir)
        assertNotEquals(appData.resolve("Nuvio"), DesktopStorage.rootDir)

        val legacyDirectory = appData.resolve("Nuvio")
        Files.createDirectories(legacyDirectory)
        val legacyFile = legacyDirectory.resolve("independence_fixture.properties")
        val original = "profile=original-nuvio-profile\n"
        Files.writeString(legacyFile, original)
        val store = DesktopStorage.store("independence_fixture")
        try {
            store.putString("profile", "boatflix-profile")
            assertEquals("boatflix-profile", store.getString("profile"))
            assertEquals(original, Files.readString(legacyFile))
        } finally {
            store.remove("profile")
            Files.deleteIfExists(DesktopStorage.rootDir.resolve("independence_fixture.properties"))
            Files.deleteIfExists(legacyFile)
        }
    }

    @Test
    fun unchanged_operations_do_not_rewrite_the_store() {
        val directory = Files.createTempDirectory("desktop-storage-test")
        val file = directory.resolve("preferences.properties")
        try {
            val store = DesktopStorage.Store(file)
            store.putString("key", "value")
            val sentinel = FileTime.fromMillis(1_000L)
            Files.setLastModifiedTime(file, sentinel)

            store.putString("key", "value")
            store.remove("missing")
            store.removeAll(listOf("also-missing"))

            assertEquals(sentinel, Files.getLastModifiedTime(file))

            store.putString("key", "updated")

            assertNotEquals(sentinel, Files.getLastModifiedTime(file))
        } finally {
            Files.deleteIfExists(file)
            Files.deleteIfExists(directory)
        }
    }
}
