package insulator.viewmodel.configurations

import arrow.core.left
import helper.FxContext
import insulator.lib.configuration.ConfigurationRepo
import insulator.lib.configuration.ConfigurationRepoException
import insulator.lib.configuration.model.Cluster
import insulator.lib.configuration.model.Configuration
import io.kotest.assertions.timing.eventually
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class ListClusterViewModelTest : StringSpec({

    val errorMessage = "Example error"

    "Show an error if unable to retrieve the configuration" {
        FxContext().use {
            // arrange
            it.addToDI(
                ConfigurationRepo::class to mockk<ConfigurationRepo> {
                    every { addNewClusterCallback(any()) } just runs
                    coEvery { getConfiguration() } returns ConfigurationRepoException(errorMessage, Throwable()).left()
                }
            )
            val sut = ListClusterViewModel()
            // act
            val clusters = sut.clustersProperty
            // assert
            eventually(1.seconds) {
                clusters.size shouldBe 0
                sut.error.value!!.message shouldBe errorMessage
            }
        }
    }

    "Update the list of cluster when a new one is added" {
        FxContext().use {
            // arrange
            val newMockConfiguration = mockk<Configuration> { every { clusters } returns listOf(Cluster.empty(), Cluster.empty(), Cluster.empty()) }
            lateinit var callback: (Configuration) -> Unit
            it.addToDI(
                ConfigurationRepo::class to mockk<ConfigurationRepo> {
                    every { addNewClusterCallback(any()) } answers { callback = firstArg() }
                    coEvery { getConfiguration() } returns ConfigurationRepoException(errorMessage, Throwable()).left()
                }
            )
            val sut = ListClusterViewModel()
            val cluster = sut.clustersProperty
            cluster.size shouldBe 0
            // act
            callback(newMockConfiguration)
            // assert
            cluster.size shouldBe 3
        }
    }
})
