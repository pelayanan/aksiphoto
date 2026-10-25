package id.aksisoft.aksiphoto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import id.aksisoft.aksiphoto.screens.AboutScreen
import id.aksisoft.aksiphoto.screens.BatchScreen
import id.aksisoft.aksiphoto.screens.EditorScreen
import id.aksisoft.aksiphoto.screens.ExportScreen
import id.aksisoft.aksiphoto.screens.FilesScreen
import id.aksisoft.aksiphoto.screens.GalleryScreen
import id.aksisoft.aksiphoto.screens.HomeScreen
import id.aksisoft.aksiphoto.screens.SettingsScreen
import id.aksisoft.aksiphoto.screens.SplashScreen
import id.aksisoft.aksiphoto.screens.OnboardingScreen
import id.aksisoft.aksiphoto.screens.ViewerScreen
import id.aksisoft.aksiphoto.ui.AksiPhotoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AksiPhotoTheme {
                val nav = rememberNavController()
                AksiPhotoNavHost(nav)
            }
        }
    }
}

@Composable
fun AksiPhotoNavHost(nav: NavHostController) {
    NavHost(navController = nav, startDestination = "splash") {
        composable("splash") { SplashScreen(nav) }
        composable("onboarding") { OnboardingScreen(nav) }
        composable("home") { HomeScreen(nav) }
        composable("gallery") { GalleryScreen(nav) }
        composable("viewer") { ViewerScreen(nav) }
        composable("editor") { EditorScreen(nav) }
        composable("export") { ExportScreen(nav) }
        composable("batch") { BatchScreen(nav) }
        composable("files") { FilesScreen(nav) }
        composable("settings") { SettingsScreen(nav) }
        composable("about") { AboutScreen(nav) }
    }
}
