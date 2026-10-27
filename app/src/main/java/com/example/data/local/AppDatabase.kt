package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ScanEntity::class,
        UserAccountEntity::class,
        CustomAllergenEntity::class,
        AllergenProfileEntity::class,
        ExtractedIngredientEntity::class,
        FindingEntity::class,
        PreferenceSettingEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun customAllergenDao(): CustomAllergenDao
    abstract fun allergenProfileDao(): AllergenProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Explicit non-destructive migration from legacy schema versions 1..3 to 4.
         */
        val MIGRATION_1_4 = object : Migration(1, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE scans ADD COLUMN batchId TEXT DEFAULT NULL")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE scans ADD COLUMN toxicologyProfileJson TEXT NOT NULL DEFAULT '{}'")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE scans ADD COLUMN groundingCitationsJson TEXT NOT NULL DEFAULT '[]'")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE custom_allergens ADD COLUMN severityLevel TEXT NOT NULL DEFAULT 'HIGH_RISK'")
                } catch (_: Exception) {
                }
            }
        }

        val MIGRATION_2_4 = object : Migration(2, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_4.migrate(db)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_4.migrate(db)
            }
        }

        /**
         * Explicit non-destructive migration from version 4 to 5:
         * - Adds scanStatus, safetyVerdict, imagePath, and isSyntheticDemo to `scans`
         * - Adds profileCategory, aliasesCsv, and notes to `custom_allergens`
         * - Creates `allergen_profiles` table for named user-defined allergen profiles
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE scans ADD COLUMN scanStatus TEXT NOT NULL DEFAULT 'COMPLETED'")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE scans ADD COLUMN safetyVerdict TEXT NOT NULL DEFAULT 'UNKNOWN'")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE scans ADD COLUMN imagePath TEXT DEFAULT NULL")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE scans ADD COLUMN isSyntheticDemo INTEGER NOT NULL DEFAULT 0")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE custom_allergens ADD COLUMN profileCategory TEXT NOT NULL DEFAULT 'custom'")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE custom_allergens ADD COLUMN aliasesCsv TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE custom_allergens ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {
                }
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `allergen_profiles` (
                        `id` TEXT NOT NULL,
                        `profileName` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `enabledPreferenceKeysJson` TEXT NOT NULL,
                        `customTriggersJson` TEXT NOT NULL,
                        `defaultSeverity` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        /**
         * Explicit non-destructive migration from version 5 to 6:
         * - Adds ruleClassification and userReasonNote to `user_preferences`
         * - Adds ruleClassification to `custom_allergens`
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE user_preferences ADD COLUMN ruleClassification TEXT NOT NULL DEFAULT 'PERSONAL_SENSITIVITY'")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE user_preferences ADD COLUMN userReasonNote TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("ALTER TABLE custom_allergens ADD COLUMN ruleClassification TEXT NOT NULL DEFAULT 'MEDICAL_ALLERGY'")
                } catch (_: Exception) {
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scansafe_database"
                )
                    .addMigrations(MIGRATION_1_4, MIGRATION_2_4, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun createInMemory(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java
            )
                .allowMainThreadQueries()
                .build()
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialPreferences(database.preferenceDao())
                    populateInitialAllergenProfiles(database.allergenProfileDao())
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val existingPrefs = database.preferenceDao().getPreferencesList()
                    if (existingPrefs.isEmpty()) {
                        populateInitialPreferences(database.preferenceDao())
                    }
                    val existingProfiles = database.allergenProfileDao().getAllProfilesList()
                    if (existingProfiles.isEmpty()) {
                        populateInitialAllergenProfiles(database.allergenProfileDao())
                    }
                }
            }
        }

        private suspend fun populateInitialPreferences(dao: PreferenceDao) {
            val defaults = listOf(
                PreferenceSettingEntity(
                    key = "pref_fragrance_free",
                    label = "Fragrance / Parfum Free",
                    description = "Flag synthetic perfumes, fragrance blends, and high-allergen fragrance components",
                    category = "cosmetic",
                    isEnabled = true,
                    ruleClassification = "PERSONAL_SENSITIVITY",
                    userReasonNote = "Contact sensitivity to synthetic parfum blends"
                ),
                PreferenceSettingEntity(
                    key = "pref_sulfate_free",
                    label = "Sulfate-Free (SLS / SLES)",
                    description = "Identify harsh anionic surfactants that may strip skin barrier",
                    category = "cosmetic",
                    isEnabled = true,
                    ruleClassification = "PERSONAL_SENSITIVITY",
                    userReasonNote = "Avoid barrier-stripping anionic surfactants"
                ),
                PreferenceSettingEntity(
                    key = "pref_paraben_free",
                    label = "Paraben-Free",
                    description = "Flag methylparaben, ethylparaben, propylparaben, and butylparaben",
                    category = "cosmetic",
                    isEnabled = false,
                    ruleClassification = "PREFERENCE_AVOIDANCE",
                    userReasonNote = "Personal formulation preference"
                ),
                PreferenceSettingEntity(
                    key = "pref_silicone_free",
                    label = "Silicone-Free",
                    description = "Identify dimethicone, cyclopentasiloxane, and other silicones",
                    category = "cosmetic",
                    isEnabled = false,
                    ruleClassification = "PREFERENCE_AVOIDANCE",
                    userReasonNote = "Avoid heavy occlusive silicones"
                ),
                PreferenceSettingEntity(
                    key = "pref_pregnancy_caution",
                    label = "Pregnancy & Nursing Caution",
                    description = "Flag topical retinoids, high-strength salicylic acid (>2%), and chemical filters",
                    category = "special_care",
                    isEnabled = true,
                    ruleClassification = "MEDICAL_ALLERGY",
                    userReasonNote = "Clinical precaution for topical retinoids and high-dose BHA"
                ),
                PreferenceSettingEntity(
                    key = "pref_vegan",
                    label = "Vegan / Cruelty-Free Check",
                    description = "Flag animal-derived ingredients such as lanolin, carmine, and beeswax",
                    category = "dietary",
                    isEnabled = false,
                    ruleClassification = "PREFERENCE_AVOIDANCE",
                    userReasonNote = "Plant-based formulation preference"
                ),
                PreferenceSettingEntity(
                    key = "pref_tree_nuts",
                    label = "Tree Nut & Peanut Allergens",
                    description = "Flag almond oil, argan oil, macadamia, walnut, and peanut derivatives",
                    category = "allergen",
                    isEnabled = true,
                    ruleClassification = "MEDICAL_ALLERGY",
                    userReasonNote = "Diagnosed nut allergy — avoid botanical nut oils"
                ),
                PreferenceSettingEntity(
                    key = "pref_gluten",
                    label = "Gluten / Wheat Derivatives",
                    description = "Flag hydrolyzed wheat protein, barley, and oat cross-contact warnings",
                    category = "allergen",
                    isEnabled = false,
                    ruleClassification = "MEDICAL_ALLERGY",
                    userReasonNote = "Celiac / wheat protein allergy"
                ),
                PreferenceSettingEntity(
                    key = "pref_essential_oils",
                    label = "Essential Oils & Terpenes",
                    description = "Flag tea tree, lavender, eucalyptus, citrus peel oils, and aromatic terpenes",
                    category = "allergen",
                    isEnabled = false,
                    ruleClassification = "PERSONAL_SENSITIVITY",
                    userReasonNote = "Volatile terpene skin sensitivity"
                ),
                PreferenceSettingEntity(
                    key = "pref_soy",
                    label = "Soy & Legume Derivatives",
                    description = "Flag glycine soja, hydrolyzed soy protein, and soy lecithin",
                    category = "allergen",
                    isEnabled = false,
                    ruleClassification = "PERSONAL_SENSITIVITY",
                    userReasonNote = "Dietary & topical soy sensitivity"
                )
            )
            dao.insertPreferences(defaults)
        }

        private suspend fun populateInitialAllergenProfiles(profileDao: AllergenProfileDao) {
            val now = System.currentTimeMillis()
            val defaultProfiles = listOf(
                AllergenProfileEntity(
                    id = "profile_default_barrier",
                    profileName = "Sensitive Barrier & Nut-Free",
                    description = "Flags Fragrance, Sulfates, Tree Nuts, and Pregnancy Cautions",
                    enabledPreferenceKeysJson = "[\"pref_fragrance_free\",\"pref_sulfate_free\",\"pref_tree_nuts\",\"pref_pregnancy_caution\"]",
                    customTriggersJson = "[]",
                    defaultSeverity = "HIGH_RISK",
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                ),
                AllergenProfileEntity(
                    id = "profile_gluten_paraben_free",
                    profileName = "Gluten, Nut & Paraben Free",
                    description = "Strict dietary + preservative filter for Gluten/Wheat, Tree Nuts, Soy, and Parabens",
                    enabledPreferenceKeysJson = "[\"pref_gluten\",\"pref_tree_nuts\",\"pref_paraben_free\",\"pref_soy\"]",
                    customTriggersJson = "[\"Methylparaben\",\"Propylparaben\",\"Hydrolyzed Wheat Protein\"]",
                    defaultSeverity = "HIGH_RISK",
                    isActive = false,
                    createdAt = now,
                    updatedAt = now
                ),
                AllergenProfileEntity(
                    id = "profile_clean_botanical",
                    profileName = "Essential Oil & Fragrance Free",
                    description = "Dermatological profile avoiding Parfum, Essential Oils, Parabens, and Silicones",
                    enabledPreferenceKeysJson = "[\"pref_fragrance_free\",\"pref_essential_oils\",\"pref_paraben_free\",\"pref_silicone_free\"]",
                    customTriggersJson = "[\"Linalool\",\"Limonene\"]",
                    defaultSeverity = "WARNING",
                    isActive = false,
                    createdAt = now,
                    updatedAt = now
                )
            )
            profileDao.insertProfiles(defaultProfiles)
        }
    }
}
