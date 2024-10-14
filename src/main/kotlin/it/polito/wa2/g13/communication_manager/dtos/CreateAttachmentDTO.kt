package it.polito.wa2.g13.communication_manager.dtos

data class CreateAttachmentDTO(
    val bytes: ByteArray,
    val contentType: String,
    val filename: String,
    val attachmentId: String,
    val mailId: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreateAttachmentDTO

        if (!bytes.contentEquals(other.bytes)) return false
        if (contentType != other.contentType) return false
        if (filename != other.filename) return false
        if (attachmentId != other.attachmentId) return false
        if (mailId != other.mailId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + filename.hashCode()
        result = 31 * result + attachmentId.hashCode()
        result = 31 * result + mailId.hashCode()
        return result
    }
}