import components.AlertNotification
import components.ErrorNotification
import components.InfoNotification
import network.RestClient
import components.container.Container
import views.login.LoginView
import components.navigation.Navigation
import views.alerts.activity.ActivityAlertsView
import views.alerts.activity.HostsView
import views.settings.PasswordChangeView
import views.log.NavigationItem
import views.log.LogView
import views.taggers.TaggersView
import views.users.UsersView
import kotlin.browser.window

val api = RestClient("https://${window.location.hostname}:${window.location.port}/api")

fun main(args: Array<String>) {
    registerComponent(LogView::class, { LogView() })
    registerComponent(Container::class, { Container() })
    registerComponent(Navigation::class, { Navigation() })
    registerComponent(NavigationItem::class, { NavigationItem() })
    registerComponent(LoginView::class, { LoginView() })
    registerComponent(PasswordChangeView::class, { PasswordChangeView() })
    registerComponent(UsersView::class, { UsersView() })
    registerComponent(TaggersView::class, { TaggersView() })
    registerComponent(ActivityAlertsView::class, { ActivityAlertsView() })
    registerComponent(HostsView::class, { HostsView() })
    registerComponent(AlertNotification::class, { AlertNotification() })
    registerComponent(InfoNotification::class, { InfoNotification() })
    registerComponent(ErrorNotification::class, { ErrorNotification() })

    initializeUI()
}


