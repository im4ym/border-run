package com.borderrun.app.data.repository

import android.util.Log
import com.borderrun.app.data.local.dao.CountryDao
import com.borderrun.app.data.local.entity.CountryEntity
import com.borderrun.app.data.remote.api.CountryApiService
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class CountryRepositoryImplTest {

    private val countryDao: CountryDao = mockk()
    private val apiService: CountryApiService = mockk()

    private val repository = CountryRepositoryImpl(countryDao, apiService)

    @Before
    fun setUp() {
        // CountryRepositoryImpl calls android.util.Log — stub all methods to
        // avoid RuntimeException("Stub!") from the Android SDK stubs.
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── getAllCountries ────────────────────────────────────────────────────────

    @Test
    fun `getAllCountries returns cached entities mapped to domain models`() = runTest {
        val entity = fakeCountryEntity("VNM", "Vietnam", "Hanoi", "Asia")
        every { countryDao.getAllCountries() } returns flowOf(listOf(entity))

        val countries = repository.getAllCountries().first()

        assertEquals(1, countries.size)
        assertEquals("VNM", countries[0].id)
        assertEquals("Vietnam", countries[0].name)
        assertEquals("Hanoi", countries[0].capital)
        assertEquals("Asia", countries[0].region)
    }

    @Test
    fun `getAllCountries returns empty list when cache is empty`() = runTest {
        every { countryDao.getAllCountries() } returns flowOf(emptyList())

        val countries = repository.getAllCountries().first()

        assertTrue(countries.isEmpty())
    }

    @Test
    fun `getCountriesByRegion returns only countries for the given region`() = runTest {
        val entity = fakeCountryEntity("DEU", "Germany", "Berlin", "Europe")
        every { countryDao.getCountriesByRegion("Europe") } returns flowOf(listOf(entity))

        val countries = repository.getCountriesByRegion("Europe").first()

        assertEquals(1, countries.size)
        assertEquals("Europe", countries[0].region)
    }

    @Test
    fun `getCountryById returns null when id not found`() = runTest {
        every { countryDao.getCountryById("XYZ") } returns flowOf(null)

        val country = repository.getCountryById("XYZ").first()

        assertEquals(null, country)
    }

    @Test
    fun `getCountryById returns mapped domain model when found`() = runTest {
        val entity = fakeCountryEntity("JPN", "Japan", "Tokyo", "Asia")
        every { countryDao.getCountryById("JPN") } returns flowOf(entity)

        val country = repository.getCountryById("JPN").first()

        assertEquals("JPN", country?.id)
        assertEquals("Japan", country?.name)
    }

    // ── syncCountries ─────────────────────────────────────────────────────────

    @Test
    fun `syncCountries calls upsertAll with merged data`() = runTest {
        coEvery { apiService.getAllCountriesBasic(any()) } returns emptyList()
        coEvery { apiService.getAllCountriesExtra(any()) } returns emptyList()
        coJustRun { countryDao.upsertAll(any()) }

        repository.syncCountries()

        coVerify(exactly = 1) { countryDao.upsertAll(emptyList()) }
    }

    @Test
    fun `syncCountries throws when API fails`() = runTest {
        coEvery { apiService.getAllCountriesBasic(any()) } throws RuntimeException("Network error")

        var threw = false
        try {
            repository.syncCountries()
        } catch (e: RuntimeException) {
            threw = true
            assertEquals("Network error", e.message)
        }
        assertTrue("Expected RuntimeException to propagate", threw)
    }

    // ── getCachedCountryCount ─────────────────────────────────────────────────

    @Test
    fun `getCachedCountryCount delegates to DAO`() = runTest {
        coEvery { countryDao.getCountryCount() } returns 195

        val count = repository.getCachedCountryCount()

        assertEquals(195, count)
    }

    @Test
    fun `getCachedCountryCount returns zero when cache is empty`() = runTest {
        coEvery { countryDao.getCountryCount() } returns 0

        assertEquals(0, repository.getCachedCountryCount())
    }

    // ── getLatestCacheTimestamp ───────────────────────────────────────────────

    @Test
    fun `getLatestCacheTimestamp returns null when cache is empty`() = runTest {
        coEvery { countryDao.getLatestCacheTimestamp() } returns null

        assertEquals(null, repository.getLatestCacheTimestamp())
    }

    @Test
    fun `getLatestCacheTimestamp returns timestamp from DAO`() = runTest {
        val ts = 1_700_000_000_000L
        coEvery { countryDao.getLatestCacheTimestamp() } returns ts

        assertEquals(ts, repository.getLatestCacheTimestamp())
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun fakeCountryEntity(
        id: String,
        name: String,
        capital: String,
        region: String,
    ) = CountryEntity(
        id = id,
        name = name,
        officialName = "Official $name",
        capital = capital,
        region = region,
        subregion = "Sub $region",
        flagUrl = "https://flag.example.com/$id.png",
        population = 1_000_000L,
        area = 100_000.0,
        languages = "[]",
        currencies = "[]",
        borders = "[]",
        isLandlocked = false,
        drivingSide = "right",
        cachedAt = System.currentTimeMillis(),
    )
}
