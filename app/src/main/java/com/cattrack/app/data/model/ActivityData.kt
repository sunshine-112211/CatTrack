package com.cattrack.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_data")
data class ActivityData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val catId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String, // yyyy-MM-dd
    val hour: Int, // 0-23
    val activityState: String = ActivityState.UNKNOWN.name,
    val steps: Int = 0,
    val activeMinutes: Int = 0,
    val sleepMinutes: Int = 0,
    val restMinutes: Int = 0,
    val walkMinutes: Int = 0,
    val runMinutes: Int = 0,
    val playMinutes: Int = 0,
    val accelerometerX: Float = 0f,
    val accelerometerY: Float = 0f,
    val accelerometerZ: Float = 0f,
    val temperature: Float = 0f // 环境温度
)

data class DailyActivitySummary(
    val date: String,
    val totalSteps: Int,
    val totalActiveMinutes: Int,
    val totalSleepMinutes: Int,
    val totalRestMinutes: Int,
    val activityDistribution: Map<ActivityState, Int>, // state -> minutes
    val hourlySteps: List<Int> // 24 hours
)

data class WeeklyActivitySummary(
    val weekStart: String,
    val weekEnd: String,
    val dailySummaries: List<DailyActivitySummary>,
    val avgDailySteps: Float,
    val avgActiveMintes: Float,
    val avgSleepMinutes: Float,
    val mostActiveDay: String,
    val leastActiveDay: String
)

data class VaccineRecord(
    val id: Long = 0,
    val catId: Long,
    val vaccineName: String,
    val vaccineDate: Long,
    val nextDueDate: Long,
    val notes: String = ""
)

data class DeworminRecord(
    val id: Long = 0,
    val catId: Long,
    val medicationName: String,
    val dewormDate: Long,
    val nextDueDate: Long,
    val notes: String = ""
)

data class WeightRecord(
    val id: Long = 0,
    val catId: Long,
    val weight: Float,
    val recordDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)
