package com.lunacattus.conflux.ui.sections.llm.homepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.SpeakerNotes
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SpeakerNotes
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lunacattus.conflux.R
import com.lunacattus.conflux.ui.LocalInnerPadding
import com.lunacattus.conflux.ui.base.CompactCard
import com.lunacattus.conflux.ui.base.CompactCardData
import com.lunacattus.conflux.ui.base.FeatureCard
import com.lunacattus.conflux.ui.base.FeatureCardData
import com.lunacattus.conflux.ui.base.GradientHeader
import com.lunacattus.conflux.ui.base.SectionHeader
import com.lunacattus.ui_design.compose.overScrollVertical

@Composable
fun LlmRoute(
    viewModel: LlmViewModel,
    navToTts: () -> Unit,
    navToPolish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val capabilities = listOf(
        FeatureCardData(
            icon = Icons.Rounded.RecordVoiceOver,
            accentColor = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.llm_tts_title),
            description = stringResource(R.string.llm_tts_desc),
            statusText = stringResource(R.string.llm_tts_available),
            onClick = navToTts,
        ),
        FeatureCardData(
            icon = Icons.Rounded.AutoAwesome,
            accentColor = MaterialTheme.colorScheme.secondary,
            title = stringResource(R.string.llm_polish_title),
            description = stringResource(R.string.llm_polish_desc),
            statusText = stringResource(R.string.llm_tts_available),
            onClick = navToPolish,
        ),
    )

    LlmScreen(
        capabilities = capabilities,
        modifier = modifier,
    )
}

@Composable
private fun LlmScreen(
    capabilities: List<FeatureCardData>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .overScrollVertical(),
        contentPadding = LocalInnerPadding.current,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item { Spacer(Modifier.height(12.dp)) }

        item {
            GradientHeader(
                title = stringResource(R.string.llm_ai_center),
                subtitle = stringResource(R.string.llm_ai_subtitle),
                icon = Icons.Rounded.Psychology,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        item { Spacer(Modifier.height(20.dp)) }

        item {
            SectionHeader(
                title = stringResource(R.string.llm_available_caps),
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        items(capabilities, key = { it.title }) { capability ->
            FeatureCard(
                data = capability,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            if (capability != capabilities.last()) {
                Spacer(Modifier.height(12.dp))
            }
        }

        item {
            Spacer(Modifier.height(28.dp))
            SectionHeader(
                title = stringResource(R.string.llm_coming_soon),
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(12.dp))
        }

        item {
            ComingSoonRow(
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun ComingSoonRow(modifier: Modifier = Modifier) {
    val items = listOf(
        CompactCardData(
            icon = Icons.Rounded.Translate,
            accentColor = MaterialTheme.colorScheme.secondary,
            title = stringResource(R.string.llm_translate_title),
            description = stringResource(R.string.llm_translate_desc),
            badgeText = stringResource(R.string.feature_coming_soon),
        ),
        CompactCardData(
            icon = Icons.Rounded.SmartToy,
            accentColor = MaterialTheme.colorScheme.tertiary,
            title = stringResource(R.string.llm_chat_title),
            description = stringResource(R.string.llm_chat_desc),
            badgeText = stringResource(R.string.feature_coming_soon),
        ),
        CompactCardData(
            icon = Icons.Rounded.Psychology,
            accentColor = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.llm_nlu_title),
            description = stringResource(R.string.llm_nlu_desc),
            badgeText = stringResource(R.string.feature_coming_soon),
        ),
        CompactCardData(
            icon = Icons.AutoMirrored.Default.SpeakerNotes,
            accentColor = MaterialTheme.colorScheme.error,
            title = stringResource(R.string.llm_asr_title),
            description = stringResource(R.string.llm_asr_desc),
            badgeText = stringResource(R.string.feature_coming_soon),
        ),
    )

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 0.dp),
    ) {
        items(items, key = { it.title }) { item ->
            CompactCard(
                data = item,
                modifier = Modifier.width(160.dp),
            )
        }
    }
}
