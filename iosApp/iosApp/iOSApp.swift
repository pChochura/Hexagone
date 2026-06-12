import SwiftUI
import Shared
import BackgroundTasks

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        IosBackgroundSync.shared.initializeKoin()
        IosBillingInitializer.shared.configure()
        registerBackgroundTasks()
        return true
    }

    func registerBackgroundTasks() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.pointlessgames.hexagone.leaderboard.sync", using: nil) { task in
            self.handleLeaderboardSync(task: task as! BGProcessingTask)
        }
    }

    func handleLeaderboardSync(task: BGProcessingTask) {
        scheduleLeaderboardSync()

        task.expirationHandler = {
            // Cancel any ongoing work if needed
        }

        IosBackgroundSync.shared.sync { success in
            task.setTaskCompleted(success: success.boolValue)
        }
    }

    func scheduleLeaderboardSync() {
        let request = BGProcessingTaskRequest(identifier: "com.pointlessgames.hexagone.leaderboard.sync")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 60 * 60) // 1 hour
        request.requiresNetworkConnectivity = true

        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule leaderboard sync: \(error)")
        }
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @Environment(\.scenePhase) private var scenePhase

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
        .onChange(of: scenePhase) { phase in
            if phase == .background {
                appDelegate.scheduleLeaderboardSync()
            }
        }
    }
}
