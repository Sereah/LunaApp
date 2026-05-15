package com.lunacattus.conflux.ui.base

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunacattus.conflux.R
import com.lunacattus.ui_design.compose.clickableWithDebounce
import com.lunacattus.ui_design.compose.onClickWithDebounced

sealed class IconSource {
    data class Vector(val imageVector: ImageVector) : IconSource()
    data class Resource(@param:DrawableRes val resId: Int) : IconSource()
}

sealed interface Item {
    val title: String
    val icon: IconSource
    val iconTint: Color
    val summary: String?
    val accentColor: Color
}

data class SwitchItem(
    override val title: String,
    override val icon: IconSource,
    override val iconTint: Color,
    override val summary: String? = null,
    override val accentColor: Color = iconTint,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
) : Item

data class NavigationItem(
    override val title: String,
    override val icon: IconSource,
    override val iconTint: Color,
    override val summary: String? = null,
    override val accentColor: Color = iconTint,
    val onClick: () -> Unit,
) : Item

data class ValueNavigationItem(
    override val title: String,
    override val icon: IconSource,
    override val iconTint: Color,
    override val summary: String? = null,
    override val accentColor: Color = iconTint,
    val valueText: String,
    val onClick: () -> Unit,
) : Item

@Composable
fun ItemRow(
    item: Item,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isLast: Boolean = false,
) {
    val shape = when {
        isFirst && isLast -> RoundedCornerShape(16.dp)
        isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
        else -> RoundedCornerShape(0.dp)
    }

    val clickableModifier = when (item) {
        is SwitchItem -> Modifier.clickableWithDebounce {}
        is NavigationItem -> Modifier.clickableWithDebounce { item.onClick() }
        is ValueNavigationItem -> Modifier.clickableWithDebounce { item.onClick() }
    }

    val rowAccent = item.accentColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = shape,
            )
            .then(clickableModifier)
            .padding(start = 0.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(rowAccent.copy(alpha = 0.7f)),
        )

        Spacer(Modifier.width(14.dp))

        val iconBg = when (item) {
            is SwitchItem -> if (item.checked) rowAccent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerHighest
            else -> rowAccent.copy(alpha = 0.1f)
        }
        val resolvedTint = when (item) {
            is SwitchItem -> if (item.checked) item.iconTint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            else -> item.iconTint
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            when (item.icon) {
                is IconSource.Resource -> {
                    Icon(
                        painter = painterResource((item.icon as IconSource.Resource).resId),
                        tint = resolvedTint,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp),
                    )
                }

                is IconSource.Vector -> {
                    Icon(
                        imageVector = (item.icon as IconSource.Vector).imageVector,
                        tint = resolvedTint,
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            item.summary?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }

        when (item) {
            is SwitchItem -> {
                Switch(
                    checked = item.checked,
                    onCheckedChange = item.onCheckedChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = rowAccent,
                        uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    ),
                )
            }

            is NavigationItem -> {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(rowAccent.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = rowAccent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            is ValueNavigationItem -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.valueText,
                        fontSize = 14.sp,
                        color = rowAccent,
                    )
                    Spacer(Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = rowAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }

    if (!isLast) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 74.dp, end = 16.dp)
                .height(0.5.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
        )
    }
}

@Composable
fun ItemCard(
    items: List<Item>,
    categoryText: String = "",
    lockState: CardLockState = CardLockState.UnLock,
    onLockButtonClick: (CardLockState) -> Unit = {}
) {
    val cardAccent = items.firstOrNull()?.accentColor ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(
                    0.5.dp,
                    cardAccent.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp),
                )
        ) {
            if (categoryText.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(cardAccent),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = categoryText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp,
                        color = cardAccent,
                    )
                }
            }

            items.forEachIndexed { index, item ->
                ItemRow(
                    item = item,
                    isFirst = index == 0 && categoryText.isEmpty(),
                    isLast = index == items.lastIndex,
                )
            }
        }

        AnimatedVisibility(
            visible = lockState != CardLockState.UnLock,
            modifier = Modifier.matchParentSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickableWithDebounce { },
                contentAlignment = Alignment.Center
            ) {
                Button(enabled = lockState == CardLockState.Lock, onClick = onClickWithDebounced {
                    onLockButtonClick(lockState)
                }) {
                    Icon(Icons.Default.LockOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when (lockState) {
                            CardLockState.Lock -> stringResource(R.string.unlock)
                            else -> stringResource(R.string.unlocking)
                        }
                    )
                }
            }
        }
    }
}

enum class CardLockState {
    Lock, UnLock, UnLocking
}
