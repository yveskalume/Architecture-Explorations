package dev.yveskalume.newsapp.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.yveskalume.newsapp.R
import dev.yveskalume.newsapp.ui.screens.home.HomeRoute
import dev.yveskalume.newsapp.ui.screens.home.HomeScreenRoute
import dev.yveskalume.newsapp.ui.screens.home.navigateToHome
import dev.yveskalume.newsapp.ui.screens.search.SearchRoute
import dev.yveskalume.newsapp.ui.screens.search.SearchScreenRoute
import dev.yveskalume.newsapp.ui.screens.search.navigateToSearch
import org.koin.core.annotation.KoinExperimentalAPI
import dev.yveskalume.newsapp.ui.R as SharedUiR

@OptIn(KoinExperimentalAPI::class)
@Composable
fun NewsNavHost(
    modifier: Modifier = Modifier,
) {
    val backStack = rememberNavBackStack(HomeRoute)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = backStack.lastOrNull() is HomeRoute,
                    onClick = backStack::navigateToHome,
                    icon = {
                        Icon(
                            painter = painterResource(SharedUiR.drawable.ic_home),
                            contentDescription = stringResource(R.string.feed),
                        )
                    },
                    label = { Text(stringResource(R.string.feed)) },
                )
                NavigationBarItem(
                    selected = backStack.lastOrNull() is SearchRoute,
                    onClick = backStack::navigateToSearch,
                    icon = {
                        Icon(
                            painter = painterResource(SharedUiR.drawable.ic_search),
                            contentDescription = stringResource(R.string.search),
                        )
                    },
                    label = { Text(stringResource(R.string.search)) },
                )
            }
        },
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            entryProvider = entryProvider {
                entry<HomeRoute> {
                    HomeScreenRoute()
                }

                entry<SearchRoute> {
                    SearchScreenRoute()
                }
            },
        )
    }
}
