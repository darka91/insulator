package insulator.viewmodel.main.topic

import arrow.core.Tuple3
import arrow.core.right
import helper.cleanupFXFramework
import helper.configureDi
import helper.configureFXFramework
import insulator.lib.kafka.AdminApi
import insulator.lib.kafka.Consumer
import insulator.lib.kafka.model.Topic
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import javafx.collections.FXCollections
import javafx.scene.input.Clipboard
import kotlinx.coroutines.delay
import tornadofx.* // ktlint-disable no-wildcard-imports
import java.util.concurrent.CompletableFuture

class TopicViewModelTest : FunSpec({

    val topicName = "topic-name"

    test("clear remove all records from the list") {
        // arrange
        val sut = TopicViewModel(topicName)
        sut.records.add(mockk())
        // act
        sut.clear()
        // assert
        sut.records.size shouldBe 0
    }

    test("stop an already stopped consumer is ignored") {
        // arrange
        val sut = TopicViewModel(topicName)
        // act
        sut.stop()
    }

    test("delete call the deleteTopic function from lib with the topic name") {
        // arrange
        val sut = TopicViewModel(topicName)
        // act
        sut.delete()
        // assert
    }

    test("consume") {
        // arrange
        val sut = TopicViewModel(topicName)
        // act
        sut.consume()
        delay(1000)
        // assert
        sut.records.count() shouldBe 3
    }

    test("copy single element happy path") {
        // arrange
        val mockClipboard = mockk<Clipboard>(relaxed = true)
        unmockkAll()
        mockkStatic(Clipboard::class)
        every { Clipboard.getSystemClipboard() } returns mockClipboard
        val sut = TopicViewModel(topicName)
        sut.selectedItem.set(RecordViewModel("key", "value", 1599913230000L))
        // act
        sut.copySelectedRecordToClipboard()
        // assert
        verify(exactly = 1) { mockClipboard.putString("2020-09-12 12:20:30\tkey\tvalue") }
    }

    test("copy all happy path") {
        // arrange
        val mockClipboard = mockk<Clipboard>(relaxed = true)
        unmockkAll()
        mockkStatic(Clipboard::class)
        every { Clipboard.getSystemClipboard() } returns mockClipboard
        val sut = TopicViewModel(topicName)
        sut.filteredRecords.set(
            FXCollections.observableList(
                listOf(
                    RecordViewModel("key1", "value1", 1599913230000L),
                    RecordViewModel("key2", "value2", 1599913230000L)
                )
            )
        )
        // act
        sut.copyAllRecordsToClipboard()
        // assert
        verify(exactly = 1) { mockClipboard.putString("2020-09-12 12:20:30\tkey1\tvalue1\n2020-09-12 12:20:30\tkey2\tvalue2") }
    }

    beforeSpec {
        configureFXFramework()
        configureDi(
            AdminApi::class to mockk<AdminApi> {
                every { describeTopic(any()) } returns CompletableFuture.completedFuture(Topic("Topic name").right())
                every { deleteTopic(any()) } returns CompletableFuture.completedFuture(null.right())
            },
            Consumer::class to mockk<Consumer> {
                every { start(any(), any(), any(), any()) } answers {
                    lastArg<(List<Tuple3<String?, String, Long>>) -> Unit>()(listOf(Tuple3("1", "2", 3L)))
                    lastArg<(List<Tuple3<String?, String, Long>>) -> Unit>()(listOf(Tuple3("1", "2", 3L)))
                    lastArg<(List<Tuple3<String?, String, Long>>) -> Unit>()(listOf(Tuple3("1", "2", 3L)))
                }
                every { stop() } just runs
                every { isRunning() } returns false
            }
        )
    }

    afterSpec {
        cleanupFXFramework()
    }
})