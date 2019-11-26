
// ----- command handler -----
interface PointCommandHandler {
    fun handle(command: PointCommand): List<PointEvent>
}

open class PointCommandHandlerImpl(
    private val eventStore: PointEventStore
): PointCommandHandler {
    override fun handle(command: PointCommand): List<PointEvent> {
        val events = when (command) {
            is AddPoint -> addPoint(command)
            is ConsumePoint -> consumePoint(command)
        }

        eventStore.save(events)
        return events
    }

    private fun load(userId: Point.UserId): Point? {
        return eventStore.find(userId.value).fold(null) { p: Point?, e ->
            (p ?: Point.new(userId)).apply(e)
        }
    }

    private fun addPoint(command: AddPoint): List<PointEvent> {
        val userId = Point.UserId(command.userId)
        val old = load(userId)
        var revision = old?.revision ?: 0
        if (command.revision < revision) {
            throw PointException.InvalidRevision(command.revision)
        }
        if (command.diff < 0) {
            throw PointException.InvalidValue(command.diff)
        }

        val events = mutableListOf<PointEvent>()

        events += PointEvent.PointAdded(
            diff = command.diff,
            userId = command.userId,
            revision = ++revision,
            eventTime = command.addTime.toEpochMilli(),
            addedFrom = command.addedFrom
        )
        return events
    }

    private fun consumePoint(command: ConsumePoint): List<PointEvent> {
        val userId = Point.UserId(command.userId)
        val old = load(userId) ?: throw PointException.NotFound(userId.value)
        var revision = old.revision
        if (command.revision < revision) {
            throw PointException.InvalidRevision(command.revision)
        }
        when (command.consumedIn) {
            ConsumedIn.iOS -> {
                if (old.commonAmount.value + old.iOSAmount.value + command.diff < 0) {
                    throw PointException.InvalidValue(command.diff)
                }
            }
            ConsumedIn.Android -> {
                if (old.commonAmount.value + old.androidAmount.value + command.diff < 0) {
                    throw PointException.InvalidValue(command.diff)
                }
            }
        }
        val events = mutableListOf<PointEvent>()
        events += PointEvent.PointConsumed(
            diff = command.diff,
            userId = command.userId,
            revision = ++revision,
            eventTime = command.consumeTime.toEpochMilli(),
            consumedIn = command.consumedIn
        )
        return events
    }
}