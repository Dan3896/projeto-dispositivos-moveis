package com.example.upcampusplus.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.upcampusplus.domain.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Único valor fora do Room: se as boas-vindas já foram vistas. */
class DataStoreOnboardingRepository(private val store: DataStore<Preferences>) : OnboardingRepository {
    override val onboardingDone: Flow<Boolean> = store.data.map { it[ONBOARDING_DONE] ?: false }

    override suspend fun markDone() {
        store.edit { it[ONBOARDING_DONE] = true }
    }

    private companion object {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }
}
