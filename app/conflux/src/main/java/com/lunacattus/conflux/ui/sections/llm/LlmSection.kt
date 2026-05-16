package com.lunacattus.conflux.ui.sections.llm

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.base.MainRoute
import com.lunacattus.conflux.ui.base.entryWithNavAndVm
import com.lunacattus.conflux.ui.sections.llm.homepage.LlmRoute
import com.lunacattus.conflux.ui.sections.llm.homepage.LlmViewModel
import com.lunacattus.conflux.ui.sections.llm.polish.PolishRoute
import com.lunacattus.conflux.ui.sections.llm.polish.PolishViewModel
import com.lunacattus.conflux.ui.sections.llm.tts.TtsRoute
import com.lunacattus.conflux.ui.sections.llm.tts.TtsViewModel
import kotlinx.serialization.Serializable

@Serializable
data object LlmRoute : MainRoute {
    override val titleResId: Int
        get() = R.string.llm_title
}

@Serializable
data object TtsRoute : MainRoute {
    override val titleResId: Int
        get() = R.string.tts_title
}

@Serializable
data object PolishRoute : MainRoute {
    override val titleResId: Int
        get() = R.string.polish_title
}

fun EntryProviderScope<NavKey>.homeSection() {
    entryWithNavAndVm<LlmRoute, LlmViewModel> { _, navigator, viewModel ->
        LlmRoute(
            viewModel = viewModel,
            navToTts = { navigator.navigate(TtsRoute) },
            navToPolish = { navigator.navigate(PolishRoute) })
    }

    entryWithNavAndVm<TtsRoute, TtsViewModel> { _, _, viewModel ->
        TtsRoute(viewModel = viewModel)
    }

    entryWithNavAndVm<PolishRoute, PolishViewModel> { _, _, viewModel ->
        PolishRoute(viewModel = viewModel)
    }
}

