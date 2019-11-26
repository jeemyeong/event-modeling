enum class AddedFrom {
    iOS, Android, Admin
}

enum class ConsumedIn {
    iOS, Android
}

data class Point(
    val commonAmount: CommonAmount,
    val iOSAmount: IOSAmount,
    val androidAmount: AndroidAmount,
    val userId: UserId,
    val revision: Int
) {
    data class UserId(val value: Long)
    data class CommonAmount(val value: Long) {
        operator fun plus(diff: Long) = copy(
            value = value + diff
        )
    }
    data class IOSAmount(val value: Long) {
        operator fun plus(diff: Long) = copy(
            value = value + diff
        )
    }
    data class AndroidAmount(val value: Long) {
        operator fun plus(diff: Long) = copy(
            value = value + diff
        )
    }

    companion object {
        fun new(userId: UserId): Point {
            return Point(
                commonAmount = CommonAmount(0),
                iOSAmount = IOSAmount(0),
                androidAmount = AndroidAmount(0),
                userId = userId,
                revision = 0
            )
        }
    }

    fun apply(e: PointEvent): Point {
        val next = copy(
            revision = e.revision
        )
        return when (e) {
            is PointEvent.PointAdded -> {
                when (e.addedFrom) {
                    AddedFrom.iOS -> {
                        return next.copy(
                            iOSAmount = iOSAmount + e.diff
                        )
                    }
                    AddedFrom.Android -> {
                        return next.copy(
                            androidAmount = androidAmount + e.diff
                        )
                    }
                    AddedFrom.Admin -> {
                        return next.copy(
                            commonAmount = commonAmount + e.diff
                        )
                    }
                }
            }
            is PointEvent.PointConsumed -> {
                when (e.consumedIn) {
                    ConsumedIn.iOS -> {
                        if ((iOSAmount + e.diff).value > 0) {
                            return next.copy(
                                iOSAmount = iOSAmount + e.diff
                            )
                        }
                        return next.copy(
                            commonAmount = commonAmount + (iOSAmount + e.diff).value,
                            iOSAmount = IOSAmount(0)
                        )
                    }
                    ConsumedIn.Android -> {
                        if ((androidAmount + e.diff).value > 0) {
                            return next.copy(
                                androidAmount = androidAmount + e.diff
                            )
                        }
                        return next.copy(
                            commonAmount = commonAmount + (androidAmount + e.diff).value,
                            androidAmount = AndroidAmount(0)
                        )
                    }
                }

            }
        }
    }
}
