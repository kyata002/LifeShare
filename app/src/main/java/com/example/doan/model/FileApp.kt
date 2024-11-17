import java.io.Serializable

data class FileApp(
    val name: String,
    val size: Long,
    val type: String,
    val path: String,
    val lastModified: Long
) : Serializable
