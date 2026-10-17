package android.net

import android.os.Parcel

class TestUri(private val testPath: String) : Uri() {
    override fun getPath(): String = testPath
    override fun toString(): String = "file://$testPath"
    override fun isHierarchical(): Boolean = true
    override fun isRelative(): Boolean = false
    override fun getScheme(): String = "file"
    override fun getSchemeSpecificPart(): String = "//$testPath"
    override fun getAuthority(): String? = null
    override fun getUserInfo(): String? = null
    override fun getHost(): String? = null
    override fun getPort(): Int = -1
    override fun getQuery(): String? = null
    override fun getFragment(): String? = null
    override fun getPathSegments(): List<String> = testPath.split("/").filter { it.isNotEmpty() }
    override fun getLastPathSegment(): String? = getPathSegments().lastOrNull()
    override fun buildUpon(): Builder? = null
    override fun getEncodedAuthority(): String? = null
    override fun getEncodedFragment(): String? = null
    override fun getEncodedPath(): String? = testPath
    override fun getEncodedQuery(): String? = null
    override fun getEncodedSchemeSpecificPart(): String = "//$testPath"
    override fun getEncodedUserInfo(): String? = null
    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: Parcel, flags: Int) {}
}
