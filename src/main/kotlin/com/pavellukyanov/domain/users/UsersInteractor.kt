package com.pavellukyanov.domain.users

import com.pavellukyanov.data.chatrooms.ChatRoomsDataSource
import com.pavellukyanov.data.users.UserDataSource
import com.pavellukyanov.domain.auth.entity.State
import com.pavellukyanov.domain.users.entity.response.UserResponse
import com.pavellukyanov.utils.Errors
import com.pavellukyanov.utils.map
import io.ktor.http.content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.types.ObjectId
import java.io.File

class UsersInteractor(
    private val chatRoomsDataSource: ChatRoomsDataSource,
    private val userDataSource: UserDataSource
) {
    suspend fun changeAvatar(
        userId: ObjectId?,
        multipartData: MultiPartData,
    ): State<UserResponse> =
        withContext(Dispatchers.IO) {
            try {
                var fileName: String?
                var avatarPath: String? = null
                var avatar: String? = null

                if (userId == null) {
                    State.Error(Errors.BAD_USER_ID)
                } else {
                    val user = userDataSource.getCurrentUser(userId)

                    user?.avatarPath?.let { path ->
                        File("/var/www/html/uploads/avatars/$path").delete()
                    }

                    multipartData.forEachPart { part ->
                        when (part) {
                            is PartData.FileItem -> {
                                fileName = part.originalFileName as String
                                var fileBytes = part.streamProvider().readBytes()
                                avatarPath = "$userId-$fileName"
                                File("/var/www/html/uploads/avatars/$avatarPath").writeBytes(fileBytes)
                                avatar = "http://188.225.9.194/uploads/avatars/$avatarPath"
                            }
                            else -> {}
                        }
                    }

                    val changedUser = user?.copy(
                        avatar = avatar,
                        avatarPath = avatarPath
                    )!!

                    val isAvatarChanged = userDataSource.changeUserAvatar(changedUser)

                    if (isAvatarChanged) chatRoomsDataSource.updateUserAvatar(user.id.toString(), avatar!!)

                    val response = UserResponse(
                        uuid = changedUser.id.toString(),
                        username = changedUser.username,
                        email = changedUser.email,
                        avatar = changedUser.avatar
                    )
                    State.Success(response)
                }
            } catch (e: Exception) {
                State.Exception(e)
            }
        }

    suspend fun getCurrentUser(userId: ObjectId?): State<UserResponse> = withContext(Dispatchers.IO) {
        try {
            if (userId == null) {
                State.Error(Errors.BAD_USER_ID)
            } else {
                val user = userDataSource.getCurrentUser(userId)
                val response = UserResponse(
                    uuid = user?.id.toString(),
                    username = user?.username,
                    email = user?.email,
                    avatar = user?.avatar
                )
                State.Success(response)
            }
        } catch (e: Exception) {
            State.Exception(e)
        }
    }

    suspend fun getAllUsers(): State<List<UserResponse>> = withContext(Dispatchers.IO) {
        try {
            val users = userDataSource.getAllUsers().map { user -> user.map() }
            State.Success(users)
        } catch (e: Exception) {
            State.Exception(e)
        }
    }
}