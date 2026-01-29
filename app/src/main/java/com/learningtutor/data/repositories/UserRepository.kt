package com.learningtutor.data.repositories

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.learningtutor.core.models.User
import com.learningtutor.data.database.UserDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao
) {

    private val gson = Gson()

    suspend fun getCurrentUserSync(): User? {
        return userDao.getUser().firstOrNull()
    }

    fun getCurrentUser(): Flow<User?> {
        return userDao.getUser()
    }

    suspend fun createUserIfNotExists(name: String = "Ученик"): User {
        val existingUser = getCurrentUserSync()

        return if (existingUser != null) {
            existingUser
        } else {
            val newUser = User(name = name)
            userDao.insert(newUser)
            newUser
        }
    }

    suspend fun updateUserName(name: String) {
        val user = getCurrentUserSync() ?: return
        val updatedUser = user.copy(name = name)
        userDao.update(updatedUser)
    }

    suspend fun updateUserTheta(userId: String, newTheta: Double) {
        userDao.updateTheta(userId, newTheta)

        // Обновляем историю тета
        val user = getCurrentUserSync() ?: return
        val history = parseThetaHistory(user.thetaHistoryJson)
        val updatedHistory = history + Pair(System.currentTimeMillis(), newTheta)
        val historyJson = serializeThetaHistory(updatedHistory)

        userDao.updateThetaHistory(userId, historyJson)
    }

    suspend fun getUserThetaHistory(): List<Pair<Long, Double>> {
        val user = getCurrentUserSync() ?: return emptyList()
        return parseThetaHistory(user.thetaHistoryJson)
    }

    private fun parseThetaHistory(json: String): List<Pair<Long, Double>> {
        return try {
            val type = object : TypeToken<List<List<Double>>>() {}.type
            val list = gson.fromJson<List<List<Double>>>(json, type)
            list?.map { Pair(it[0].toLong(), it[1]) } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun serializeThetaHistory(history: List<Pair<Long, Double>>): String {
        val list = history.map { listOf(it.first.toDouble(), it.second) }
        return gson.toJson(list)
    }
}