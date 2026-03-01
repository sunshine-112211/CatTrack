package com.cattrack.app.util

import com.cattrack.app.data.model.*
import kotlin.math.abs
import kotlin.math.roundToInt

object HealthAnalyzer {

    // ---- Score Weights ----
    private const val WEIGHT_ACTIVE = 0.35f
    private const val WEIGHT_SLEEP = 0.30f
    private const val WEIGHT_REGULARITY = 0.20f
    private const val WEIGHT_ANOMALY = 0.15f

    // ---- Reference Values ----
    private const val IDEAL_ACTIVE_MINUTES = 180 // 理想活动时长（分钟）
    private const val IDEAL_SLEEP_MINUTES = 840  // 理想睡眠时长（14小时）
    private const val ANOMALY_ACTIVITY_DROP_THRESHOLD = 0.5f // 活动量骤降阈值50%
    private const val ANOMALY_SLEEP_THRESHOLD = 0.4f // 睡眠异常阈值40%

    /**
     * 计算单日健康评分（0-100）
     */
    fun calculateDailyHealthScore(
        activeMinutes: Int,
        sleepMinutes: Int,
        steps: Int,
        hasAnomaly: Boolean,
        regularityScore: Float = 80f
    ): Int {
        val activeScore = calcActiveScore(activeMinutes)
        val sleepScore = calcSleepScore(sleepMinutes)
        val anomalyPenalty = if (hasAnomaly) 15f else 0f

        val score = (activeScore * WEIGHT_ACTIVE +
                sleepScore * WEIGHT_SLEEP +
                regularityScore * WEIGHT_REGULARITY -
                anomalyPenalty * WEIGHT_ANOMALY)

        return score.coerceIn(0f, 100f).roundToInt()
    }

    private fun calcActiveScore(activeMinutes: Int): Float {
        return when {
            activeMinutes >= IDEAL_ACTIVE_MINUTES -> 100f
            activeMinutes <= 30 -> 10f
            else -> (activeMinutes.toFloat() / IDEAL_ACTIVE_MINUTES) * 100f
        }
    }

    private fun calcSleepScore(sleepMinutes: Int): Float {
        return when {
            sleepMinutes in (IDEAL_SLEEP_MINUTES - 60)..(IDEAL_SLEEP_MINUTES + 60) -> 100f
            sleepMinutes < 360 -> 20f
            sleepMinutes > 1200 -> 60f
            else -> {
                val diff = abs(sleepMinutes - IDEAL_SLEEP_MINUTES).toFloat()
                (100f - diff / 10f).coerceAtLeast(20f)
            }
        }
    }

    /**
     * 识别异常行为
     */
    fun detectAnomalies(
        currentData: HealthData,
        previousData: HealthData?
    ): List<AnomalyEvent> {
        val anomalies = mutableListOf<AnomalyEvent>()

        // 活动量骤降
        if (previousData != null && previousData.totalActiveMinutes > 60) {
            val dropRatio = currentData.totalActiveMinutes.toFloat() / previousData.totalActiveMinutes
            if (dropRatio < ANOMALY_ACTIVITY_DROP_THRESHOLD) {
                anomalies.add(
                    AnomalyEvent(
                        date = currentData.date,
                        type = AnomalyType.ACTIVITY_DROP,
                        description = "今日活动量比昨天减少了${((1 - dropRatio) * 100).roundToInt()}%，请注意猫咪健康状态",
                        severity = AnomalySeverity.HIGH
                    )
                )
            }
        }

        // 睡眠异常 - 过长
        if (currentData.totalSleepMinutes > 1200) {
            anomalies.add(
                AnomalyEvent(
                    date = currentData.date,
                    type = AnomalyType.SLEEP_ABNORMAL,
                    description = "今日睡眠时间超过20小时，可能存在健康问题",
                    severity = AnomalySeverity.MEDIUM
                )
            )
        }

        // 睡眠异常 - 过短
        if (currentData.totalSleepMinutes < 300 && currentData.totalActiveMinutes > 60) {
            anomalies.add(
                AnomalyEvent(
                    date = currentData.date,
                    type = AnomalyType.SLEEP_ABNORMAL,
                    description = "今日睡眠时间不足5小时，休息不足",
                    severity = AnomalySeverity.MEDIUM
                )
            )
        }

        // 长时间不动
        if (currentData.totalActiveMinutes < 30 && currentData.totalSleepMinutes < 600) {
            anomalies.add(
                AnomalyEvent(
                    date = currentData.date,
                    type = AnomalyType.LONG_INACTIVITY,
                    description = "今日几乎没有活动，请检查猫咪状态",
                    severity = AnomalySeverity.HIGH
                )
            )
        }

        return anomalies
    }

    /**
     * 生成健康建议
     */
    fun generateSuggestions(
        healthScore: Int,
        activeMinutes: Int,
        sleepMinutes: Int,
        anomalies: List<AnomalyEvent>
    ): List<String> {
        val suggestions = mutableListOf<String>()

        when {
            healthScore >= 90 -> suggestions.add("🎉 状态极佳！继续保持良好的生活习惯")
            healthScore >= 75 -> suggestions.add("👍 整体状态良好，注意保持规律作息")
            healthScore >= 60 -> suggestions.add("⚠️ 健康状态一般，建议增加互动时间")
            else -> suggestions.add("🚨 健康状态不佳，请关注猫咪并考虑就医检查")
        }

        if (activeMinutes < 60) {
            suggestions.add("🐾 活动量偏低，建议增加玩耍时间，每天至少30分钟互动")
        } else if (activeMinutes > 300) {
            suggestions.add("😊 活动量充足！你的猫咪精力旺盛")
        }

        if (sleepMinutes < 600) {
            suggestions.add("😴 睡眠不足，确保猫咪有安静舒适的休息环境")
        } else if (sleepMinutes > 1100) {
            suggestions.add("🛏️ 睡眠时间较长，这对猫咪来说很正常，但要留意是否有精神不振")
        }

        if (anomalies.any { it.severity == AnomalySeverity.HIGH }) {
            suggestions.add("❗ 检测到异常行为，建议密切观察并联系兽医")
        }

        // 饮食建议
        suggestions.add(generateFeedingAdvice(activeMinutes))

        return suggestions
    }

    /**
     * 生成喂养建议
     */
    fun generateFeedingAdvice(activeMinutes: Int): String {
        return when {
            activeMinutes >= 200 -> "🍽️ 今日活动量大，可适当增加10-15%的食物摄入，补充能量"
            activeMinutes >= 100 -> "🍽️ 正常喂食即可，保持规律饮食时间"
            else -> "🍽️ 活动量偏低，注意控制饮食量，避免肥胖"
        }
    }

    /**
     * 趋势分析（环比对比）
     */
    fun analyzeTrend(
        currentList: List<HealthData>,
        previousList: List<HealthData>
    ): TrendAnalysis {
        if (currentList.isEmpty()) return TrendAnalysis(0f, 0f, 0f, "数据不足")

        val currentAvgScore = currentList.map { it.healthScore }.average().toFloat()
        val currentAvgActive = currentList.map { it.totalActiveMinutes }.average().toFloat()
        val currentAvgSleep = currentList.map { it.totalSleepMinutes }.average().toFloat()

        if (previousList.isEmpty()) {
            return TrendAnalysis(currentAvgScore, currentAvgActive, currentAvgSleep, "暂无环比数据")
        }

        val prevAvgScore = previousList.map { it.healthScore }.average().toFloat()
        val prevAvgActive = previousList.map { it.totalActiveMinutes }.average().toFloat()
        val changePercent = if (prevAvgScore > 0) {
            ((currentAvgScore - prevAvgScore) / prevAvgScore) * 100f
        } else 0f

        val trendDesc = buildString {
            append("本期平均健康分 ${currentAvgScore.roundToInt()}分")
            if (changePercent > 0) append("，较上期提升${changePercent.roundToInt()}%📈")
            else if (changePercent < -5) append("，较上期下降${abs(changePercent).roundToInt()}%📉")
            else append("，与上期持平➡️")

            if (currentAvgActive > prevAvgActive) append("，活动量有所增加")
            else if (currentAvgActive < prevAvgActive * 0.8f) append("，活动量明显减少，需要关注")
        }

        return TrendAnalysis(currentAvgScore, currentAvgActive, currentAvgSleep, trendDesc, changePercent)
    }

    /**
     * 生成完整报告
     */
    fun generateReport(
        catId: Long,
        period: ReportPeriod,
        startDate: String,
        endDate: String,
        healthDataList: List<HealthData>,
        prevHealthDataList: List<HealthData>
    ): HealthReport {
        val avgScore = if (healthDataList.isEmpty()) 0
        else healthDataList.map { it.healthScore }.average().roundToInt()

        val avgActive = if (healthDataList.isEmpty()) 0f
        else healthDataList.map { it.totalActiveMinutes }.average().toFloat()

        val avgSleep = if (healthDataList.isEmpty()) 0f
        else healthDataList.map { it.totalSleepMinutes }.average().toFloat()

        val avgSteps = if (healthDataList.isEmpty()) 0f
        else healthDataList.map { it.totalSteps }.average().toFloat()

        val allAnomalies = healthDataList.filter { it.hasAnomaly }.flatMap { data ->
            detectAnomalies(data, null)
        }

        val trend = analyzeTrend(healthDataList, prevHealthDataList)
        val suggestions = generateSuggestions(avgScore, avgActive.toInt(), avgSleep.toInt(), allAnomalies)
        val feedingAdvice = generateFeedingAdvice(avgActive.toInt())

        val changePercent = if (prevHealthDataList.isNotEmpty()) {
            val prevAvg = prevHealthDataList.map { it.healthScore }.average().toFloat()
            if (prevAvg > 0) ((avgScore - prevAvg) / prevAvg) * 100f else 0f
        } else 0f

        return HealthReport(
            catId = catId,
            period = period,
            startDate = startDate,
            endDate = endDate,
            healthScore = avgScore,
            avgActiveMinutes = avgActive,
            avgSleepMinutes = avgSleep,
            avgSteps = avgSteps,
            trendAnalysis = trend.description,
            anomalies = allAnomalies,
            suggestions = suggestions,
            feedingAdvice = feedingAdvice,
            comparedToPreviousPeriod = changePercent
        )
    }

    data class TrendAnalysis(
        val avgHealthScore: Float,
        val avgActiveMinutes: Float,
        val avgSleepMinutes: Float,
        val description: String,
        val changePercent: Float = 0f
    )
}
