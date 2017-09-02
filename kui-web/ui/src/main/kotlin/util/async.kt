package util

typealias SuccessResultCallback<T> = (response: T) -> Unit

typealias SuccessCallback = () -> Unit

typealias ErrorCallback = (message: String) -> Unit

class ResultAsync<T : Any>(val method: (successCallback: SuccessResultCallback<T>, errorCallback: ErrorCallback) -> Unit) {

    private var successCallback: (SuccessResultCallback<T>)? = null
    private var errorCallback: (ErrorCallback)? = null

    fun success(successCallback: SuccessResultCallback<T>) {
        this.successCallback = successCallback
        if (this.errorCallback == null) {
            throw RuntimeException("Set error callback first.")
        }
        method(successCallback, errorCallback!!)
    }

    fun error(errorCallback: ErrorCallback) : ResultAsync<T> {
        this.errorCallback = errorCallback
        return this
    }

}

class Async(val method: (successCallback: SuccessCallback, errorCallback: ErrorCallback) -> Unit) {

    private var successCallback: (SuccessCallback)? = null
    private var errorCallback: (ErrorCallback)? = null

    fun success(successCallback: SuccessCallback) {
        this.successCallback = successCallback
        if (this.errorCallback == null) {
            throw RuntimeException("Set error callback first.")
        }
        method(successCallback, errorCallback!!)
    }

    fun error(errorCallback: ErrorCallback) : Async {
        this.errorCallback = errorCallback
        return this
    }

}