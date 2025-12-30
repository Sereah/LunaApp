package com.lunacattus.conflux.ui.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lunacattus.ui_design.compose.clickableWithDebounce

sealed interface Item {
    val title: String
    val icon: ImageVector
    val summary: String?
}

data class SwitchItem(
    override val title: String,
    override val icon: ImageVector,
    override val summary: String? = null,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
) : Item

data class NavigationItem(
    override val title: String,
    override val icon: ImageVector,
    override val summary: String? = null,
    val onClick: () -> Unit,
) : Item

data class ValueNavigationItem(
    override val title: String,
    override val icon: ImageVector,
    override val summary: String? = null,
    val valueText: String,
    val onClick: () -> Unit,
) : Item

@Composable
fun ItemRow(
    item: Item,
    modifier: Modifier = Modifier,
) {
    val clickableModifier = when (item) {
        is SwitchItem -> Modifier.clickableWithDebounce {}
        is NavigationItem -> Modifier.clickableWithDebounce { item.onClick() }
        is ValueNavigationItem -> Modifier.clickableWithDebounce { item.onClick() }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp)
            .then(clickableModifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            modifier = Modifier.size(36.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, fontSize = 18.sp)

            item.summary?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        when (item) {
            is SwitchItem -> {
                Switch(
                    checked = item.checked,
                    onCheckedChange = item.onCheckedChange
                )
            }

            is NavigationItem -> {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = "navigate",
                    modifier = Modifier.size(30.dp)
                )
            }

            is ValueNavigationItem -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.valueText,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "navigate",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ItemCard(items: List<Item>, categoryText: String = "") {
    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        if (categoryText.isNotEmpty()) {
            Text(text = categoryText, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
        }
        items.forEachIndexed { index, item ->
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                ItemRow(item)
            }
            if (index != items.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier
                        .padding(start = 58.dp, end = 12.dp)
                )
            }
        }
    }
}