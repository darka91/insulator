package insulator.configuration

import insulator.configuration.model.Configuration
import insulator.configuration.model.InsulatorTheme
import insulator.kafka.model.Cluster
import insulator.kafka.model.SaslConfiguration
import insulator.kafka.model.SchemaRegistryConfiguration
import insulator.kafka.model.SslConfiguration
import insulator.test.helper.getTestSandboxFolder
import io.kotest.assertions.arrow.either.shouldBeLeft
import io.kotest.assertions.arrow.either.shouldBeRight
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.File
import java.nio.file.Paths
import java.util.UUID

class ConfigurationRepoTest : FreeSpec({
    fun mockConfigPath() = Paths.get(getTestSandboxFolder().toString(), ".insulator.test").toString()

    "getConfiguration invokes the callback on change" - {
        // arrange
        val testConfig = mockConfigPath()
        val sut = ConfigurationRepo(testConfig)
        val testCluster = Cluster.empty()
        var callbackCalled: Configuration? = null

        "call the callback on store" {
            // act
            sut.addNewClusterCallback { callbackCalled = it }
            sut.store(testCluster)
            // assert
            callbackCalled!!.clusters shouldContain testCluster
        }

        "call the callback on deltete" {
            // act
            sut.addNewClusterCallback { callbackCalled = it }
            sut.delete(testCluster)
            // assert
            callbackCalled!!.clusters.isEmpty() shouldBe true
        }
    }

    "getConfiguration return left with invalid files" - {
        // arrange
        val testConfig = "http://something"
        val sut = ConfigurationRepo(testConfig)

        "left on retrieve configurations" {
            // act
            val res = sut.getConfiguration()
            // assert
            res shouldBeLeft { it.shouldBeInstanceOf<ConfigurationRepoException>() }
        }

        "left on store configurations" {
            // act
            val res = sut.store(Cluster.empty())
            // assert
            res shouldBeLeft { it.shouldBeInstanceOf<ConfigurationRepoException>() }
        }
    }

    "getConfiguration the first time create the config file" {
        // arrange
        val testConfig = mockConfigPath()
        val sut = ConfigurationRepo(testConfig)
        // act
        val res = sut.getConfiguration()
        // assert
        res shouldBeRight Configuration(clusters = emptyList())
        File(testConfig).exists() shouldBe true
    }

    "getConfiguration of a corrupted file return left" {
        // arrange
        val testConfig = mockConfigPath()
        File(testConfig).writeText("Wrong content")
        val sut = ConfigurationRepo(testConfig)
        // act
        val res = sut.getConfiguration()
        // assert
        res shouldBeLeft {}
    }

    "delete a cluster" - {
        // arrange
        val testConfig = mockConfigPath()
        val sut = ConfigurationRepo(testConfig)

        "delete a cluster from the configuration" {
            val testCluster = UUID.randomUUID()
            sut.store(Cluster(testCluster, "Test", ""))
            // act
            val res = sut.delete(Cluster(testCluster, "", ""))
            // assert
            res shouldBeRight Unit
            File(testConfig).readText().replace("\n", "").replace(" ", "") shouldBe "{\"clusters\":[],\"theme\":\"Light\"}"
        }

        "delete a cluster never added" {
            // arrange
            sut.store(Cluster(UUID.randomUUID(), "Test", ""))
            val expectedConfig = File(testConfig).readText()
            // act
            val res = sut.delete(Cluster(UUID.randomUUID(), "", ""))
            // assert
            res shouldBeRight Unit
            File(testConfig).readText() shouldBe expectedConfig
        }
    }

    "store a new cluster" - {
        // arrange
        val testConfig = mockConfigPath()
        val sut = ConfigurationRepo(testConfig)
        val uuid = UUID.randomUUID()

        "minimal cluster" {
            // act
            val res = sut.store(Cluster(uuid, "", ""))
            // assert
            res shouldBeRight Unit
            ConfigurationRepo(testConfig).getConfiguration() shouldBeRight
                Configuration(clusters = listOf(Cluster(uuid, "", "")))
        }
        "store a cluster with all configs" {
            // act
            val res = sut.store(
                Cluster(
                    uuid,
                    "",
                    "",
                    true,
                    SslConfiguration("", "", "", ""),
                    true,
                    SaslConfiguration("", ""),
                    SchemaRegistryConfiguration("", "", "")
                )
            )
            // assert
            res shouldBeRight Unit
            ConfigurationRepo(testConfig).getConfiguration() shouldBeRight {}
        }
    }

    "store theme" {
        // arrange
        val testConfig = mockConfigPath()
        val sut = ConfigurationRepo(testConfig)
        // act
        val res = sut.store(InsulatorTheme.Dark)
        // assert
        res shouldBeRight Unit
        File(testConfig).readText().replace("\n", "").replace(" ", "") shouldBe "{\"clusters\":[],\"theme\":\"Dark\"}"
    }

    "store new record doesn't reset the team" {
        // arrange
        val testConfig = mockConfigPath()
        val sut = ConfigurationRepo(testConfig)
        val testCluster = Cluster.empty()
        sut.store(InsulatorTheme.Dark)
        // act
        val res = sut.store(testCluster)
        // assert
        res shouldBeRight Unit
        sut.getConfiguration() shouldBeRight { it.theme shouldBe InsulatorTheme.Dark }
    }
})
