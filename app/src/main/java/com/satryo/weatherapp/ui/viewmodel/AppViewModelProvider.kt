package com.satryo.weatherapp.ui.viewmodel

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.satryo.weatherapp.WeatherApplication

/**
 * Factory untuk seluruh ViewModel. Dependency diambil dari AppContainer di [WeatherApplication].
 */
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val container = weatherApplication().container
            DashboardViewModel(
                weatherRepository = container.weatherRepository,
                locationRepository = container.locationRepository,
                savedLocationRepository = container.savedLocationRepository,
                countryRepository = container.countryRepository,
            )
        }
        initializer {
            val container = weatherApplication().container
            DetailViewModel(
                savedStateHandle = createSavedStateHandle(),
                weatherRepository = container.weatherRepository,
                savedLocationRepository = container.savedLocationRepository,
                countryRepository = container.countryRepository,
            )
        }
        initializer {
            val container = weatherApplication().container
            SearchViewModel(
                locationRepository = container.locationRepository,
                countryRepository = container.countryRepository,
            )
        }
    }
}

/** Mengambil instance [WeatherApplication] dari [CreationExtras]. */
fun CreationExtras.weatherApplication(): WeatherApplication =
    this[AndroidViewModelFactory.APPLICATION_KEY] as WeatherApplication
