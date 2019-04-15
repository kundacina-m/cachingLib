package com.example.cachingstrategieslib


import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

// region constants
const val LOCAL = "Local"
const val REMOTE = "Remote"
const val ID: Long = 1
// endregion constants

@ExtendWith(MockKExtension::class)
class StrategiesTest {

	// region helper fields
	val testStorageList: TestStorage<List<TestObj>> = spyk(TestStorage())
	// endregion helper fields

	lateinit var SUT: Strategies

	@BeforeEach
	fun setup() {
		SUT = Strategies()
	}

	@Nested
	@DisplayName("'Network or Cache' strategy")
	inner class StrategyNorC {

		@Test
		fun `network invalid callback, cache has valid and it is triggered`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = false, localResponse = true)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 0) { testStorageList.networkCall {} }
			verify(exactly = 1) { testStorageList.localCall(any()) }
			assertEquals(listOf(TestObj(
				ID,
				LOCAL
			)), response)

		}

		@Test
		fun `network valid callback, local invalid, network triggered only`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = true, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.networkCall(any()) }
			verify(exactly = 0) { testStorageList.localCall {} }
			assertEquals(listOf(TestObj(
				ID,
				REMOTE
			)), response)
		}

		@Test
		fun `both have valid callbacks, but network triggered only`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = true, localResponse = true)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.networkCall(any()) }
			verify(exactly = 0) { testStorageList.localCall {} }
			assertEquals(listOf(TestObj(1, REMOTE)), response)
		}

		@Test
		fun `network and local have invalid callbacks, state not changed, none triggered`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = false, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 0) { testStorageList.networkCall {} }
			verify(exactly = 0) { testStorageList.localCall {} }
			assertEquals(emptyList<TestObj>(), response)

		}

		// region nested class helper methods

		private fun callFunUnderTest(): List<TestObj> {
			var response = emptyList<TestObj>()
			SUT.NorCstrategy<List<TestObj>>(
                Strategies.StorageFunction(testStorageList::networkCall),
                Strategies.StorageFunction(testStorageList::localCall)
			) {
				if (it is SealedResponse.OnSuccess) {
					response = it.data
				}
			}
			return response
		}

		// endregion nested class helper methods

	}

	@Nested
	@DisplayName("'Cache or Network' strategy")
	inner class StrategyCorN {

		@Test
		fun `network invalid callback, cache has valid and it is triggered`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = false, localResponse = true)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.localCall(any()) }
			verify(exactly = 0) { testStorageList.networkCall {} }
			assertEquals(listOf(TestObj(
				ID,
				LOCAL
			)), response)

		}

		@Test
		fun `network valid callback, local invalid, network triggered only`() {
			// Arrangeg
			setNetworkAndLocalResponse(networkResponse = true, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 0) { testStorageList.localCall {} }
			verify(exactly = 1) { testStorageList.networkCall(any()) }
			assertEquals(listOf(TestObj(
				ID,
				REMOTE
			)), response)
		}

		@Test
		fun `both have valid callbacks, but local triggered only`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = true, localResponse = true)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.localCall(any()) }
			verify(exactly = 0) { testStorageList.networkCall(any()) }
			assertEquals(listOf(TestObj(
				ID,
				LOCAL
			)), response)
		}

		@Test
		fun `network and local have invalid callbacks, state not changed, none triggered`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = false, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 0) { testStorageList.localCall {} }
			verify(exactly = 0) { testStorageList.networkCall {} }
			assertEquals(emptyList<TestObj>(), response)

		}

		// region nested class helper methods

		private fun callFunUnderTest(): List<TestObj> {
			var response = emptyList<TestObj>()
			SUT.CorNstrategy<List<TestObj>>(
                Strategies.StorageFunction(testStorageList::localCall),
                Strategies.StorageFunction(testStorageList::networkCall)
			) {
				if (it is SealedResponse.OnSuccess) {
					response = it.data
				}
			}
			return response
		}

		// endregion nested class helper methods

	}

	@Nested
	@DisplayName("'Cache, Network' strategy")
	inner class StrategyCN {

		@Test
		fun `cache has valid callback ,network has valid callback `() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = true, localResponse = true)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.localCall(any()) }
			verify(exactly = 1) { testStorageList.networkCall(any()) }
		}

		@Test
		fun `cache has valid callback, network invalid, cache triggered only`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = false, localResponse = true)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.localCall(any()) }
			verify(exactly = 0) { testStorageList.networkCall{} }
			assertEquals(listOf(TestObj(
				ID,
				LOCAL
			)), response)

		}

		@Test
		fun `both have invalid callbacks`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = false, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 0) { testStorageList.localCall{} }
			verify(exactly = 0) { testStorageList.networkCall{} }
			assertEquals(emptyList<TestObj>(), response)

		}

		@Test
		fun `cache invalid callback, network valid callback`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = true, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 0) { testStorageList.localCall {} }
			verify(exactly = 1) { testStorageList.networkCall (any()) }
			assertEquals(listOf(TestObj(
				ID,
				REMOTE
			)), response)

		}

		// region nested class helper methods

		private fun callFunUnderTest(): List<TestObj> {
			var response = emptyList<TestObj>()
			SUT.CNstrategy<List<TestObj>>(
                Strategies.StorageFunction(testStorageList::localCall),
                Strategies.StorageFunction(testStorageList::networkCall)
			) {
				if (it is SealedResponse.OnSuccess) {
					response = it.data
				}
			}
			return response
		}

		// endregion nested class helper methods

	}

	@Nested
	@DisplayName("'Network, Cache' strategy")
	inner class StrategyNC {

		@Test
		fun `network valid callback, cache valid callback`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = true, localResponse = true)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.localCall(any()) }
			verify(exactly = 1) { testStorageList.networkCall(any()) }
		}

		@Test
		fun `network valid callback, local invalid callback`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = true, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.networkCall(any()) }
			verify(exactly = 0) { testStorageList.localCall {} }
			assertEquals(listOf(TestObj(
				ID,
				REMOTE
			)), response)
		}

		@Test
		fun `network invalid callback, cache invalid callback`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = false, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 0) { testStorageList.localCall{} }
			verify(exactly = 0) { testStorageList.networkCall{} }
			assertEquals(emptyList<TestObj>(), response)
		}

		// region nested class helper methods

		private fun callFunUnderTest(): List<TestObj> {
			var response = emptyList<TestObj>()
			SUT.NCstrategy<List<TestObj>>(
                Strategies.StorageFunction(testStorageList::networkCall),
                Strategies.StorageFunction(testStorageList::localCall)
			) {
				if (it is SealedResponse.OnSuccess) {
					response = it.data
				}
			}
			return response
		}

		// endregion nested class helper methods

	}



	@Nested
	@DisplayName("'Cache, Network, Cache' strategy")
	inner class StrategyCNC {


		@Test
		fun `cache valid callback, network valid callback, network response added to cache`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = true, localResponse = true)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.localCall(any()) }
			verify(exactly = 1) { testStorageList.networkCall(any()) }
			verify(exactly = 1) { testStorageList.localCallAddAll(any()) }

		}

		@Test
		fun `cache valid callback, network invalid callback, network response not added to cache`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = false, localResponse = true)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 1) { testStorageList.localCall(any()) }
			verify(exactly = 0) { testStorageList.networkCall{} }
			verify(exactly = 0) { testStorageList.localCallAddAll{} }
			assertEquals(listOf(TestObj(
				ID,
				LOCAL
			)), response)


		}

		@Test
		fun `cache invalid callback, network valid callback, network response added to cache`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = true, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 0) { testStorageList.localCall{} }
			verify(exactly = 1) { testStorageList.networkCall(any()) }
			verify(exactly = 1) { testStorageList.localCallAddAll(any()) }
			assertEquals(listOf(TestObj(
				ID,
				REMOTE
			)), response)

		}

		@Test
		fun `cache invalid callback, network invalid callback, network response not added to cache`() {
			// Arrange
			setNetworkAndLocalResponse(networkResponse = false, localResponse = false)

			// Act
			val response = callFunUnderTest()

			// Assert
			verify(exactly = 0) { testStorageList.localCall{} }
			verify(exactly = 0) { testStorageList.networkCall{} }
			verify(exactly = 0) { testStorageList.localCallAddAll{} }
			assertEquals(emptyList<TestObj>(), response)


		}

		private fun callFunUnderTest(): List<TestObj> {
			var response = emptyList<TestObj>()
			SUT.CNCstrategy<List<TestObj>>(
                Strategies.StorageFunction(testStorageList::localCall),
                Strategies.StorageFunction(testStorageList::networkCall),
                Strategies.StorageFunction(testStorageList::localCallAddAll)

			) {
				if (it is SealedResponse.OnSuccess) {
					response = it.data
				}
			}
			return response
		}
	}

	// region helper methods

	fun setNetworkAndLocalResponse(networkResponse: Boolean, localResponse: Boolean) {
		if (networkResponse) testStorageList.networkResponseOn = SealedResponse.OnSuccess(
            listOf(
                TestObj(
                    ID,
                    REMOTE
                )
            )
        )
		else testStorageList.networkResponseOn = SealedResponse.OnError(RequestError.UnknownError)
		if (localResponse) testStorageList.localResponseOn = SealedResponse.OnSuccess(
            listOf(
                TestObj(
                    ID,
                    LOCAL
                )
            )
        )
		else testStorageList.localResponseOn = SealedResponse.OnError(RequestError.UnknownError)
	}

	// endregion helper methods

	// region helper classes

	class TestStorage<T> {

		lateinit var networkResponseOn: SealedResponse<T>
		lateinit var localResponseOn: SealedResponse<T>

		fun networkCall(onSealedResponse: OnSealedResponse<T>) {
			onSealedResponse(networkResponseOn)
		}

		fun localCall(onSealedResponse: OnSealedResponse<T>) {
			onSealedResponse(localResponseOn)
		}

		fun localCallAddAll(onSealedResponse: OnSealedResponse<T>) {
			onSealedResponse(networkResponseOn)
		}
	}

	// endregion helper classes
}