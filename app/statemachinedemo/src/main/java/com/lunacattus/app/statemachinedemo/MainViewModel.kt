package com.lunacattus.app.statemachinedemo

import androidx.lifecycle.ViewModel
import com.lunacattus.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val pageState: PageState
) : ViewModel() {

    init {
        Logger.d(TAG, "init.")
        pageState.setDbg(true)
    }

    fun machineStart() {
        pageState.start()
    }

    fun machineQuit() {
        pageState.quit()
    }

    fun sendMessage(what: Int) {
        pageState.sendMessage(what)
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}