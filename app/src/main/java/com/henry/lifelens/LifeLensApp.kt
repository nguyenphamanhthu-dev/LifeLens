package com.henry.lifelens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.henry.lifelens.data.local.LifeLensDatabase
import com.henry.lifelens.data.repository.ReminderRepository
import com.henry.lifelens.data.repository.ScanRepository
import com.henry.lifelens.notification.ReminderScheduler
import com.henry.lifelens.presentation.camera.CameraScreen
import com.henry.lifelens.presentation.detail.ScanDetailScreen
import com.henry.lifelens.presentation.detail.ScanDetailViewModel
import com.henry.lifelens.presentation.detail.ScanDetailViewModelFactory
import com.henry.lifelens.presentation.history.HistoryScreen
import com.henry.lifelens.presentation.history.HistoryViewModel
import com.henry.lifelens.presentation.history.HistoryViewModelFactory
import com.henry.lifelens.presentation.home.HomeScreen
import com.henry.lifelens.presentation.home.HomeViewModel
import com.henry.lifelens.presentation.home.HomeViewModelFactory
import com.henry.lifelens.presentation.result.ResultScreen
import com.henry.lifelens.presentation.result.ResultViewModel
import com.henry.lifelens.presentation.result.ResultViewModelFactory
import com.henry.lifelens.presentation.reminder.ReminderHubScreen
import com.henry.lifelens.presentation.utilities.LifeLensUtilitiesScreen

@Composable
fun LifeLensApp() {



    val context = LocalContext.current

    val navController =
        rememberNavController()

    val database = remember {

        LifeLensDatabase.getDatabase(
            context
        )
    }


    val scanRepository = remember {

        ScanRepository(
            database.scanDao()
        )
    }

    val reminderRepository = remember {

        ReminderRepository(
            database.reminderDao()
        )
    }


    val reminderScheduler = remember {

        ReminderScheduler(
            context.applicationContext
        )
    }


    var scannedImageUri by remember {
        mutableStateOf<Uri?>(null)
    }


    var scannedText by remember {
        mutableStateOf("")
    }
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {

        composable("reminders") {
            ReminderHubScreen(
                repository = reminderRepository,
                scheduler = reminderScheduler,
                onBackClick = { navController.popBackStack() }
            )
        }
        composable("map") {
            LifeLensUtilitiesScreen("map") { navController.popBackStack() }
        }
        composable("link") {
            LifeLensUtilitiesScreen("link") { navController.popBackStack() }
        }
        composable("phone") {
            LifeLensUtilitiesScreen("phone") { navController.popBackStack() }
        }
        composable(
            route = "home"
        ) {

            val homeViewModel: HomeViewModel =
                viewModel(
                    factory = HomeViewModelFactory(
                        scanRepository = scanRepository,
                        reminderRepository = reminderRepository
                    )
                )


            HomeScreen(

                // CAMERA
                onCameraClick = {

                    navController.navigate(
                        "camera"
                    )
                },


                // GALLERY
                onGalleryScanCompleted = {
                        imageUri,
                        recognizedText ->

                    scannedImageUri =
                        imageUri

                    scannedText =
                        recognizedText


                    navController.navigate(
                        "result"
                    )
                },


                // VIEW MODEL
                viewModel =
                    homeViewModel,


                // HISTORY
                onHistoryClick = { navController.navigate("history") },
                onReminderClick = { navController.navigate("reminders") },
                onMapClick = { navController.navigate("map") },
                onLinkClick = { navController.navigate("link") },
                onPhoneClick = { navController.navigate("phone") }
            )
        }


        composable(
            route = "camera"
        ) {

            CameraScreen(

                onScanCompleted = {
                        imageUri,
                        recognizedText ->

                    scannedImageUri =
                        imageUri
                    scannedText =
                    recognizedText


                    navController.navigate(
                        "result"
                    ) {

                        popUpTo(
                            "camera"
                        ) {
                            inclusive = true
                        }
                    }
                },


                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }

        composable(
            route = "result"
        ) {

            val resultViewModel: ResultViewModel =
                viewModel(

                    factory =
                        ResultViewModelFactory(

                            scanRepository =
                                scanRepository,

                            reminderRepository =
                                reminderRepository,

                            reminderScheduler =
                                reminderScheduler
                        )
                )


            ResultScreen(

                imageUri =
                    scannedImageUri,


                recognizedText =
                    scannedText,


                viewModel =
                    resultViewModel,


                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }

        composable(
            route = "history"
        ) {

            val historyViewModel: HistoryViewModel =
                viewModel(

                    factory =
                        HistoryViewModelFactory(
                            scanRepository
                        )
                )
            HistoryScreen(

                viewModel =
                    historyViewModel,


                onScanClick = {
                        scanId ->

                    navController.navigate(
                        "scan_detail/$scanId"
                    )
                },


                onBackClick = {

                    navController
                        .popBackStack()
                }
            )
        }

        composable(

            route =
                "scan_detail/{scanId}",

            arguments =
                listOf(

                    navArgument(
                        "scanId"
                    ) {

                        type =
                            NavType.LongType
                    }
                )

        ) { backStackEntry ->

            val scanId =
                backStackEntry
                    .arguments
                    ?.getLong(
                        "scanId"
                    )
                    ?: return@composable


            val detailViewModel: ScanDetailViewModel =
                viewModel(

                    factory =
                        ScanDetailViewModelFactory(

                            repository =
                                scanRepository
                        )
                )

            ScanDetailScreen(

                scanId =
                    scanId,


                viewModel =
                    detailViewModel,


                onBackClick = {

                    navController
                }
            )
        }
    }
}