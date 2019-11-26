// ----- exception -----
sealed class PointException(msg: String?) : RuntimeException(msg) {
    data class NotFound(val userId: Long) :
        PointException("itemCode not found: $userId")
    data class InvalidValue(val invalidValue: Long) :
        PointException("Invalid value: $invalidValue")
    data class InvalidRevision(val invalidRevision: Int) :
        PointException("Invalid version: $invalidRevision")
}
