package com.example.cachingstrategieslib

import com.example.cachingstrategieslib.SealedResponse.OnSuccess
import kotlin.reflect.KCallable
import kotlin.reflect.KParameter

class Strategies {

	// Pull data from Cache, then pull data from Network, if success then store to Cache

	fun <T> CNCstrategy(
        cacheFun1: StorageFunction<OnSealedResponse<T>>? = null,
        networkFun: StorageFunction<OnSealedResponse<T>>? = null,
        cacheFun2: StorageFunction<OnSealedResponse<Boolean>>? = null,
        responseOn: OnSealedResponse<T>
	) {

		cacheFun1?.execute { cacheData ->
			responseOn.invoke(cacheData)
			networkFun?.execute { networkData ->
				responseOn.invoke(networkData)
				cacheFun2?.let {
					if (networkData is OnSuccess) cacheFun2.executeWithParam(networkData.data) {
						//						Log.d((it as OnSuccess).data.toString(),"Logged responseOn")
					}
				}
			}

		}
	}

	fun <T> CNstrategy(
        cacheFun: StorageFunction<OnSealedResponse<T>>? = null,
        networkFun: StorageFunction<OnSealedResponse<T>>? = null,
        responseOn: OnSealedResponse<T>
	) {
		CNCstrategy(cacheFun, networkFun, null, responseOn)
	}

	fun <T> NCstrategy(
        networkFun: StorageFunction<OnSealedResponse<T>>? = null,
        cacheFun: StorageFunction<OnSealedResponse<Boolean>>? = null,
        responseOn: OnSealedResponse<T>
	) {
		networkFun?.execute { networkData ->
			responseOn.invoke(networkData)
			cacheFun?.let {
				if (networkData is OnSuccess) cacheFun.executeWithParam(networkData.data) {
					//						Log.d((it as OnSuccess).data.toString(),"Logged responseOn")
				}

			}
		}
	}

	fun <T> CorNstrategy(
        cacheFun: StorageFunction<OnSealedResponse<T>>? = null,
        networkFun: StorageFunction<OnSealedResponse<T>>? = null,
        responseOn: OnSealedResponse<T>
	) {
		cacheFun?.execute { cacheData ->
			if (cacheData is OnSuccess)
				responseOn.invoke(cacheData)
			else networkFun?.execute { networkData ->
				if (networkData is OnSuccess)
					responseOn.invoke(networkData)
			}

		}
	}

	fun <T> NorCstrategy(
        networkFun: StorageFunction<OnSealedResponse<T>>? = null,
        cacheFun: StorageFunction<OnSealedResponse<T>>? = null,
        responseOn: OnSealedResponse<T>
	) {
		networkFun?.execute { networkData ->
			if (networkData is OnSuccess)
				responseOn.invoke(networkData)
			else cacheFun?.execute { cacheData ->
				if (cacheData is OnSuccess)
					responseOn.invoke(cacheData)
			}
		}
	}

	open class StorageFunction<R>(private val function: KCallable<Unit>, vararg arguments: Any?) {

		val map by lazy { hashMapOf<KParameter, Any?>() }

		init {
			for ((counter, arg) in arguments.withIndex()) {
				map[function.parameters[counter]] = arg
			}
		}

		fun executeWithParam(returnValue: Any? = null, callback: R? = null) {
			returnValue?.let { map[function.parameters.first()] = returnValue }
			callback?.let { map[function.parameters.last()] = callback }
			function.callBy(map)
		}

		open fun execute(returnType: R? = null) {
			returnType?.let { map[function.parameters.last()] = returnType }
			function.callBy(map)
		}

	}

}