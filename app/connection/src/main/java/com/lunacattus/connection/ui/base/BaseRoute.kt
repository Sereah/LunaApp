package com.lunacattus.connection.ui.base

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface BaseRoute : NavKey {
    val name: String
}

interface RootRoute : BaseRoute //根栈的路由分类

interface MainRoute : BaseRoute //嵌套NavDisplay的路由分类

@Serializable
data object MainGraph: NavKey //代表嵌套NavDisplay的路由放在根栈