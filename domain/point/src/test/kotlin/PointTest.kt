import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

const val WELCOME_POINT = 100L
const val POINTS_BY_PAYMENT_A = 500L
const val CONSUME_POINT_A = -50L

class PointTest {
    @Test
    fun `Point Handler Test`() {
        // given
        val store = PointEventStoreInMemoryImpl()
        val handler = PointCommandHandlerImpl(store)
        val userId = 1L
        val addedFrom = AddedFrom.iOS
        val consumedIn = ConsumedIn.iOS

        // when
        handler.handle(
            AddPoint(
                diff = WELCOME_POINT,
                userId = userId,
                revision = 1,
                addedFrom = AddedFrom.Admin,
                addTime = Instant.now()
            )
        )
        handler.handle(
            AddPoint(
                diff = POINTS_BY_PAYMENT_A,
                userId = userId,
                revision = 2,
                addedFrom = addedFrom,
                addTime = Instant.now()
            )
        )
        handler.handle(
            ConsumePoint(
                diff = CONSUME_POINT_A,
                userId = userId,
                revision = 3,
                consumedIn = consumedIn,
                consumeTime = Instant.now()
            )
        )

        // then
        store.find(userId).let { events ->
            assertTrue {
                events.any {
                    it is PointEvent.PointAdded
                }
            }
            assertTrue {
                events.any {
                    it is PointEvent.PointConsumed
                }
            }
            assertEquals(3, events.last().revision)

            val point = events.fold(null) { p: Point?, e ->
                (p ?: Point.new(Point.UserId(userId))).apply(e)
            }

            assertTrue {
                point !== null
            }

            point?.let {
                println(point)
                assertTrue {
                    point.revision == 3
                }
                assertTrue {
                    point.commonAmount.value == 100L
                }
                assertTrue {
                    point.androidAmount.value == 0L
                }
                assertTrue {
                    point.iOSAmount.value == 450L
                }

            }
        }

    }
}