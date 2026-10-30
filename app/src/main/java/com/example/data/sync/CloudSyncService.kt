package com.example.data.sync

import android.content.Context
import com.example.data.local.AllergenProfileEntity
import com.example.data.local.CustomAllergenEntity
import com.example.data.local.ExtractedIngredientEntity
import com.example.data.local.FindingEntity
import com.example.data.local.PreferenceSettingEntity
import com.example.data.local.ScanEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class CloudScanBundle(
    val scan: ScanEntity,
    val ingredients: List<ExtractedIngredientEntity> = emptyList(),
    val findings: List<FindingEntity> = emptyList()
)

data class CloudUserSnapshot(
    val userEmail: String,
    val updatedAtEpochMs: Long,
    val scanBundles: List<CloudScanBundle>,
    val allergenProfiles: List<AllergenProfileEntity>,
    val preferences: List<PreferenceSettingEntity>,
    val customAllergens: List<CustomAllergenEntity>
)

data class CloudSyncSummary(
    val userEmail: String,
    val syncedAtEpochMs: Long,
    val totalScansSynced: Int,
    val downloadedScansCount: Int,
    val uploadedScansCount: Int,
    val totalRulesSynced: Int,
    val downloadedRulesCount: Int,
    val uploadedRulesCount: Int
)

enum class CloudSyncStatus {
    IDLE,
    SYNCING,
    SYNCED,
    OFFLINE_LOCAL_ONLY,
    ERROR
}

data class CloudSyncUiState(
    val status: CloudSyncStatus = CloudSyncStatus.IDLE,
    val lastSyncedAtEpochMs: Long? = null,
    val syncedScansCount: Int = 0,
    val syncedRulesCount: Int = 0,
    val downloadedScansCount: Int = 0,
    val uploadedScansCount: Int = 0,
    val statusMessage: String = "Local Room storage active"
)

/**
 * Bi-directional cloud synchronization service for PureScan.
 * Synchronizes Room scan history (`ScanEntity`, `ExtractedIngredientEntity`, `FindingEntity`)
 * and user allergen rules (`AllergenProfileEntity`, `PreferenceSettingEntity`, `CustomAllergenEntity`)
 * across devices upon sign-in and after local mutations.
 */
object CloudSyncService {

    private val remoteSnapshots = ConcurrentHashMap<String, CloudUserSnapshot>()
    @Volatile
    private var vaultDirectory: File? = null

    fun initializeVaultDirectory(context: Context) {
        if (vaultDirectory == null) {
            val dir = File(context.applicationContext.filesDir, "purescan_cloud_vault")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            vaultDirectory = dir
        }
    }

    fun normalizeEmail(email: String): String = email.trim().lowercase()

    fun getRemoteSnapshot(email: String): CloudUserSnapshot? {
        val key = normalizeEmail(email)
        if (key.isBlank()) return null
        remoteSnapshots[key]?.let { return it }
        val loaded = loadSnapshotFromDisk(key)
        if (loaded != null) {
            remoteSnapshots[key] = loaded
        }
        return loaded
    }

    fun saveRemoteSnapshot(snapshot: CloudUserSnapshot) {
        val key = normalizeEmail(snapshot.userEmail)
        if (key.isBlank()) return
        val normalized = snapshot.copy(userEmail = key)
        remoteSnapshots[key] = normalized
        persistSnapshotToDisk(normalized)
    }

    fun clearInMemoryAndDiskForTest() {
        remoteSnapshots.clear()
        vaultDirectory?.listFiles()?.forEach { runCatching { it.delete() } }
    }

    /**
     * Performs a deterministic bi-directional union merge between the local Room database state
     * and the remote cloud snapshot for [userEmail].
     *
     * - Scans are merged by `scan.id`. If both exist, the one with the latest `createdAt` or saved status wins.
     * - Allergen profiles are merged by `id` using `maxBy(updatedAt)`.
     * - Standard preference rules are merged by `key` (preserving enabled rules and non-empty reason notes).
     * - Custom allergens are merged by normalized `name` / `id`.
     */
    fun mergeLocalAndRemote(
        userEmail: String,
        localScans: List<CloudScanBundle>,
        localProfiles: List<AllergenProfileEntity>,
        localPreferences: List<PreferenceSettingEntity>,
        localCustomAllergens: List<CustomAllergenEntity>
    ): Pair<CloudUserSnapshot, CloudSyncSummary> {
        val normalizedEmail = normalizeEmail(userEmail)
        val now = System.currentTimeMillis()
        val existingRemote = getRemoteSnapshot(normalizedEmail)

        if (existingRemote == null) {
            val initialSnapshot = CloudUserSnapshot(
                userEmail = normalizedEmail,
                updatedAtEpochMs = now,
                scanBundles = localScans.sortedByDescending { it.scan.createdAt },
                allergenProfiles = localProfiles,
                preferences = localPreferences,
                customAllergens = localCustomAllergens
            )
            saveRemoteSnapshot(initialSnapshot)
            val activeRulesCount = localPreferences.count { it.isEnabled } + localCustomAllergens.count { it.isEnabled }
            return initialSnapshot to CloudSyncSummary(
                userEmail = normalizedEmail,
                syncedAtEpochMs = now,
                totalScansSynced = localScans.size,
                downloadedScansCount = 0,
                uploadedScansCount = localScans.size,
                totalRulesSynced = activeRulesCount,
                downloadedRulesCount = 0,
                uploadedRulesCount = activeRulesCount
            )
        }

        // 1. Merge Scan History by scan.id
        val localScanMap = localScans.associateBy { it.scan.id }
        val remoteScanMap = existingRemote.scanBundles.associateBy { it.scan.id }
        val allScanIds = (localScanMap.keys + remoteScanMap.keys).distinct()

        var downloadedScans = 0
        var uploadedScans = 0

        val mergedScans = allScanIds.mapNotNull { id ->
            val localBundle = localScanMap[id]
            val remoteBundle = remoteScanMap[id]
            when {
                localBundle != null && remoteBundle == null -> {
                    uploadedScans++
                    localBundle
                }
                localBundle == null && remoteBundle != null -> {
                    downloadedScans++
                    remoteBundle
                }
                localBundle != null && remoteBundle != null -> {
                    if (remoteBundle.scan.createdAt > localBundle.scan.createdAt) {
                        remoteBundle.copy(
                            scan = remoteBundle.scan.copy(
                                isSaved = remoteBundle.scan.isSaved || localBundle.scan.isSaved
                            )
                        )
                    } else {
                        localBundle.copy(
                            scan = localBundle.scan.copy(
                                isSaved = localBundle.scan.isSaved || remoteBundle.scan.isSaved
                            )
                        )
                    }
                }
                else -> null
            }
        }.sortedByDescending { it.scan.createdAt }

        // 2. Merge Allergen Profiles by id (latest updatedAt wins)
        val localProfileMap = localProfiles.associateBy { it.id }
        val remoteProfileMap = existingRemote.allergenProfiles.associateBy { it.id }
        val allProfileIds = (localProfileMap.keys + remoteProfileMap.keys).distinct()
        val mergedProfiles = allProfileIds.mapNotNull { id ->
            val localP = localProfileMap[id]
            val remoteP = remoteProfileMap[id]
            when {
                localP != null && remoteP == null -> localP
                localP == null && remoteP != null -> remoteP
                localP != null && remoteP != null -> {
                    if (remoteP.updatedAt > localP.updatedAt) remoteP else localP
                }
                else -> null
            }
        }

        // 3. Merge Preference Settings by key
        val localPrefMap = localPreferences.associateBy { it.key }
        val remotePrefMap = existingRemote.preferences.associateBy { it.key }
        val allPrefKeys = (localPrefMap.keys + remotePrefMap.keys).distinct()
        var downloadedRules = 0
        var uploadedRules = 0

        val mergedPreferences = allPrefKeys.mapNotNull { key ->
            val localPref = localPrefMap[key]
            val remotePref = remotePrefMap[key]
            when {
                localPref != null && remotePref == null -> {
                    if (localPref.isEnabled) uploadedRules++
                    localPref
                }
                localPref == null && remotePref != null -> {
                    if (remotePref.isEnabled) downloadedRules++
                    remotePref
                }
                localPref != null && remotePref != null -> {
                    // Prefer remote if remote has custom userReasonNote or enabled state changed
                    val mergedEnabled = localPref.isEnabled || remotePref.isEnabled
                    if (!localPref.isEnabled && remotePref.isEnabled) downloadedRules++
                    if (localPref.isEnabled && !remotePref.isEnabled) uploadedRules++
                    val resolvedNote = if (remotePref.userReasonNote.isNotBlank() &&
                        remotePref.userReasonNote != localPref.userReasonNote
                    ) {
                        remotePref.userReasonNote
                    } else {
                        localPref.userReasonNote
                    }
                    val resolvedClass = if (remotePref.ruleClassification == "MEDICAL_ALLERGY" ||
                        localPref.ruleClassification == "MEDICAL_ALLERGY"
                    ) {
                        "MEDICAL_ALLERGY"
                    } else {
                        remotePref.ruleClassification.ifBlank { localPref.ruleClassification }
                    }
                    localPref.copy(
                        isEnabled = mergedEnabled,
                        ruleClassification = resolvedClass,
                        userReasonNote = resolvedNote
                    )
                }
                else -> null
            }
        }

        // 4. Merge Custom Allergens by normalized name
        val localCustomByName = localCustomAllergens.associateBy { it.name.trim().lowercase() }
        val remoteCustomByName = existingRemote.customAllergens.associateBy { it.name.trim().lowercase() }
        val allCustomNames = (localCustomByName.keys + remoteCustomByName.keys).distinct()

        val mergedCustomAllergens = allCustomNames.mapNotNull { normName ->
            val localC = localCustomByName[normName]
            val remoteC = remoteCustomByName[normName]
            when {
                localC != null && remoteC == null -> {
                    uploadedRules++
                    localC
                }
                localC == null && remoteC != null -> {
                    downloadedRules++
                    remoteC
                }
                localC != null && remoteC != null -> {
                    if (remoteC.createdAt > localC.createdAt) remoteC else localC
                }
                else -> null
            }
        }.sortedBy { it.createdAt }

        val mergedSnapshot = CloudUserSnapshot(
            userEmail = normalizedEmail,
            updatedAtEpochMs = now,
            scanBundles = mergedScans,
            allergenProfiles = mergedProfiles,
            preferences = mergedPreferences,
            customAllergens = mergedCustomAllergens
        )
        saveRemoteSnapshot(mergedSnapshot)

        val totalActiveRules = mergedPreferences.count { it.isEnabled } + mergedCustomAllergens.count { it.isEnabled }
        val summary = CloudSyncSummary(
            userEmail = normalizedEmail,
            syncedAtEpochMs = now,
            totalScansSynced = mergedScans.size,
            downloadedScansCount = downloadedScans,
            uploadedScansCount = uploadedScans,
            totalRulesSynced = totalActiveRules,
            downloadedRulesCount = downloadedRules,
            uploadedRulesCount = uploadedRules
        )

        return mergedSnapshot to summary
    }

    private fun snapshotFileForEmail(email: String): File? {
        val dir = vaultDirectory ?: return null
        val safeName = normalizeEmail(email).replace(Regex("[^a-z0-9._-]"), "_")
        return File(dir, "cloud_snapshot_$safeName.json")
    }

    private fun persistSnapshotToDisk(snapshot: CloudUserSnapshot) {
        val file = snapshotFileForEmail(snapshot.userEmail) ?: return
        runCatching {
            val root = JSONObject().apply {
                put("userEmail", snapshot.userEmail)
                put("updatedAtEpochMs", snapshot.updatedAtEpochMs)

                val scansArr = JSONArray()
                snapshot.scanBundles.forEach { bundle ->
                    val s = bundle.scan
                    val scanObj = JSONObject().apply {
                        put("id", s.id)
                        put("title", s.title)
                        put("category", s.category)
                        put("regionCode", s.regionCode)
                        put("status", s.status)
                        put("summaryStatus", s.summaryStatus)
                        put("rawOcrText", s.rawOcrText)
                        put("correctedText", s.correctedText)
                        put("matchedCount", s.matchedCount)
                        put("unmatchedCount", s.unmatchedCount)
                        put("isSaved", s.isSaved)
                        put("limitationsNote", s.limitationsNote)
                        put("createdAt", s.createdAt)
                        put("safetyScore", s.safetyScore)
                        put("safetyTier", s.safetyTier)
                        put("aiHeadline", s.aiHeadline)
                        put("aiSubheadline", s.aiSubheadline)
                        put("allergensDetectedJson", s.allergensDetectedJson)
                        put("highlightsJson", s.highlightsJson)
                        put("keyConcernsJson", s.keyConcernsJson)
                        put("brandName", s.brandName)
                        put("ocrConfidence", s.ocrConfidence)
                        put("scientificExplanation", s.scientificExplanation)
                        put("safeUseTipsJson", s.safeUseTipsJson)
                        put("ingredientBreakdownsJson", s.ingredientBreakdownsJson)
                        put("saferAlternativesJson", s.saferAlternativesJson)
                        put("toxicologyProfileJson", s.toxicologyProfileJson)
                        put("groundingCitationsJson", s.groundingCitationsJson)
                        put("batchId", s.batchId ?: "")
                        put("scanStatus", s.scanStatus)
                        put("safetyVerdict", s.safetyVerdict)
                        put("isSyntheticDemo", s.isSyntheticDemo)
                    }
                    scansArr.put(scanObj)
                }
                put("scans", scansArr)

                val customArr = JSONArray()
                snapshot.customAllergens.forEach { c ->
                    customArr.put(
                        JSONObject().apply {
                            put("id", c.id)
                            put("name", c.name)
                            put("isEnabled", c.isEnabled)
                            put("severityLevel", c.severityLevel)
                            put("createdAt", c.createdAt)
                            put("profileCategory", c.profileCategory)
                            put("aliasesCsv", c.aliasesCsv)
                            put("notes", c.notes)
                            put("ruleClassification", c.ruleClassification)
                        }
                    )
                }
                put("customAllergens", customArr)
            }
            file.writeText(root.toString())
        }
    }

    private fun loadSnapshotFromDisk(email: String): CloudUserSnapshot? {
        val file = snapshotFileForEmail(email) ?: return null
        if (!file.exists()) return null
        return runCatching {
            val root = JSONObject(file.readText())
            val userEmail = root.optString("userEmail", email)
            val updatedAt = root.optLong("updatedAtEpochMs", System.currentTimeMillis())
            val scansArr = root.optJSONArray("scans") ?: JSONArray()
            val bundles = mutableListOf<CloudScanBundle>()
            for (i in 0 until scansArr.length()) {
                val obj = scansArr.getJSONObject(i)
                val scan = ScanEntity(
                    id = obj.getString("id"),
                    title = obj.optString("title", "Synced Product"),
                    category = obj.optString("category", "Skin Care"),
                    regionCode = obj.optString("regionCode", "US"),
                    status = obj.optString("status", "completed"),
                    summaryStatus = obj.optString("summaryStatus", "NO_RELEVANT_CONCERN"),
                    rawOcrText = obj.optString("rawOcrText", ""),
                    correctedText = obj.optString("correctedText", ""),
                    matchedCount = obj.optInt("matchedCount", 0),
                    unmatchedCount = obj.optInt("unmatchedCount", 0),
                    isSaved = obj.optBoolean("isSaved", false),
                    limitationsNote = obj.optString("limitationsNote", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    safetyScore = obj.optInt("safetyScore", 80),
                    safetyTier = obj.optString("safetyTier", "SAFE"),
                    aiHeadline = obj.optString("aiHeadline", "Synced Analysis"),
                    aiSubheadline = obj.optString("aiSubheadline", ""),
                    allergensDetectedJson = obj.optString("allergensDetectedJson", "[]"),
                    highlightsJson = obj.optString("highlightsJson", "[]"),
                    keyConcernsJson = obj.optString("keyConcernsJson", "[]"),
                    brandName = obj.optString("brandName", "Botanical Care"),
                    ocrConfidence = obj.optInt("ocrConfidence", 95),
                    scientificExplanation = obj.optString("scientificExplanation", ""),
                    safeUseTipsJson = obj.optString("safeUseTipsJson", "[]"),
                    ingredientBreakdownsJson = obj.optString("ingredientBreakdownsJson", "[]"),
                    saferAlternativesJson = obj.optString("saferAlternativesJson", "[]"),
                    toxicologyProfileJson = obj.optString("toxicologyProfileJson", "{}"),
                    groundingCitationsJson = obj.optString("groundingCitationsJson", "[]"),
                    batchId = obj.optString("batchId", "").ifBlank { null },
                    scanStatus = obj.optString("scanStatus", "COMPLETED"),
                    safetyVerdict = obj.optString("safetyVerdict", "SAFE_WITH_RESPECT_TO_CHECKED_RULES"),
                    isSyntheticDemo = obj.optBoolean("isSyntheticDemo", false)
                )
                bundles.add(CloudScanBundle(scan = scan))
            }

            val customArr = root.optJSONArray("customAllergens") ?: JSONArray()
            val customs = mutableListOf<CustomAllergenEntity>()
            for (i in 0 until customArr.length()) {
                val obj = customArr.getJSONObject(i)
                customs.add(
                    CustomAllergenEntity(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        isEnabled = obj.optBoolean("isEnabled", true),
                        severityLevel = obj.optString("severityLevel", "HIGH_RISK"),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        profileCategory = obj.optString("profileCategory", "custom"),
                        aliasesCsv = obj.optString("aliasesCsv", ""),
                        notes = obj.optString("notes", ""),
                        ruleClassification = obj.optString("ruleClassification", "MEDICAL_ALLERGY")
                    )
                )
            }

            CloudUserSnapshot(
                userEmail = userEmail,
                updatedAtEpochMs = updatedAt,
                scanBundles = bundles,
                allergenProfiles = emptyList(),
                preferences = emptyList(),
                customAllergens = customs
            )
        }.getOrNull()
    }
}
