package com.lunacattus.nav3test.ui.base

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

interface RootRoute: NavKey //根栈的路由分类

interface MainRoute: NavKey //嵌套NavDisplay的路由分类

@Serializable
data object MainGraph : NavKey //代表嵌套NavDisplay的路由放在根栈