package components

/**
 * Alert component
 */
class ErrorNotification : AlertComponent() {

    override fun bind() {
        super.bind()
        errorNotification = this
    }
}