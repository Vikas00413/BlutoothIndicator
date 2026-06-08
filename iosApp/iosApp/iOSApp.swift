import SharedLogic
import SwiftUI

@main
struct iOSApp: App {
    init() {
        KoinInit_iosKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}