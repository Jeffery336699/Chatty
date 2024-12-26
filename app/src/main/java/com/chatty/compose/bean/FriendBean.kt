package com.chatty.compose.bean

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Stable
import java.util.*

/**
 * 重组的基准原则：from《Compose从入门到实战.143页》
 *  1. composable基于参数的比较结果来决定是否重组
 *  2. 更准确的说，只有当参与比较的参数对象是稳定的且equals返回true,才认为是相等的
 *  3. Ps:个人觉得默认情况是非常保守的，var就被当做不稳定的了，保守也是有原因的，因为UI的刷新依赖于重组,另外内部也对重组进行了大量的优化
 */
// Optimize: 优化重组检测：默认情况下，data class 已具备 equals 方法，Compose 能根据该方法判断对象是否变化。
//  但使用 @Stable 可进一步优化：如果属性值未改变(就算是var被认定为不稳定的)，Compose仍可以跳过当前作用域的重组。
@Stable
data class MessageItemData(
    val userProfile: UserProfileData,
    val lastMsg: String,
    val unreadCount: Int = 0,
    val mid: String = UUID.randomUUID().toString(),
)

@Stable
data class UserProfileData(
    val avatarRes: Int,
    val nickname: String,
    val motto: String,
    val gender: String? = null,
    val age: Int? = null,
    val phone: String? = null,
    val email: String? = null,
    val uid: String = UUID.randomUUID().toString(),
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(avatarRes)
        parcel.writeString(nickname)
        parcel.writeString(motto)
        parcel.writeString(gender)
        parcel.writeValue(age)
        parcel.writeString(phone)
        parcel.writeString(email)
        parcel.writeString(uid)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserProfileData> {
        override fun createFromParcel(parcel: Parcel): UserProfileData {
            return UserProfileData(parcel)
        }

        override fun newArray(size: Int): Array<UserProfileData?> {
            return arrayOfNulls(size)
        }
    }
}