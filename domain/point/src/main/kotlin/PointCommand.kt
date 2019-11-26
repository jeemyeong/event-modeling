import java.time.Instant

sealed class PointCommand {
    abstract val diff: Long
    abstract val userId: Long
    abstract val revision: Int
}

data class AddPoint(
    override val diff: Long,
    override val userId: Long,
    override val revision: Int,
    val addedFrom: AddedFrom,
    val addTime: Instant
) : PointCommand()


data class ConsumePoint(
    override val diff: Long,
    override val userId: Long,
    override val revision: Int,
    val consumedIn: ConsumedIn,
    val consumeTime: Instant
) : PointCommand()
