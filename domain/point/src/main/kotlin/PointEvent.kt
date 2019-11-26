sealed class PointEvent {
    abstract val diff: Long
    abstract val userId: Long
    abstract val revision: Int
    abstract val eventTime: Long

    data class PointAdded(
        override val diff: Long,
        override val userId: Long,
        override val revision: Int,
        override val eventTime: Long,
        val addedFrom: AddedFrom
    ) : PointEvent()

    data class PointConsumed(
        override val diff: Long,
        override val userId: Long,
        override val revision: Int,
        override val eventTime: Long,
        val consumedIn: ConsumedIn
    ) : PointEvent()
}

interface PointEventStore {
    fun save(events: List<PointEvent>)
    fun find(userId: Long): Sequence<PointEvent>
}

open class PointEventStoreInMemoryImpl : PointEventStore {
    private val list = mutableListOf<PointEvent>()

    fun clear() {
        list.clear()
    }

    override fun save(events: List<PointEvent>) {
        list += events
    }

    override fun find(userId: Long): Sequence<PointEvent> {
        return list.asSequence().filter { it.userId == userId }
    }
}
