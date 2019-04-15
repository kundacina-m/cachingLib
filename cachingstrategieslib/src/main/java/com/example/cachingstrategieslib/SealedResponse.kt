package com.example.cachingstrategieslib

typealias OnSealedResponse<T> = (SealedResponse<T>) -> Unit

sealed class SealedResponse<out T> {

	data class OnSuccess<T>(val data: T) : SealedResponse<T>()
	data class OnError(val error: RequestError) : SealedResponse<Nothing>()

}

sealed class RequestError {
	object UnknownError : RequestError()
	object NoInternetError : RequestError()
	object ServerError : RequestError()
	data class HttpError(val code: Int, val message: String) : RequestError()
}
