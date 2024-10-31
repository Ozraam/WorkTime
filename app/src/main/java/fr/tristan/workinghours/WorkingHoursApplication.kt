package fr.tristan.workinghours

import android.app.Application
import fr.tristan.workinghours.data.AppContainer
import fr.tristan.workinghours.data.DefaultAppContainer

class WorkingHoursApplication: Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}