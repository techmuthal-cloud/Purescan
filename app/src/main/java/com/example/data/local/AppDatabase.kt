package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ScanEntity::class,
        UserAccountEntity::class,
        CustomAllergenEntity::class,
        ExtractedIngredientEntity::class,
        FindingEntity::class,
        PreferenceSettingEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun customAllergenDao(): CustomAllergenDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scansafe_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration(true)
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
                    isEnabled = true
                ),
                PreferenceSettingEntity(
                    key = "pref_sulfate_free",
                    label = "Sulfate-Free (SLS / SLES)",
                    description = "Identify harsh anionic surfactants that may strip skin barrier",
                    category = "cosmetic",
                    isEnabled = true
                ),
                PreferenceSettingEntity(
                    key = "pref_paraben_free",
                    label = "Paraben-Free",
                    description = "Flag methylparaben, ethylparaben, propylparaben, and butylparaben",
                    category = "cosmetic",
                    isEnabled = false
                ),
                PreferenceSettingEntity(
                    key = "pref_silicone_free",
                    label = "Silicone-Free",
                    description = "Identify dimethicone, cyclopentasiloxane, and other silicones",
                    category = "cosmetic",
                    isEnabled = false
                ),
                PreferenceSettingEntity(
                    key = "pref_pregnancy_caution",
                    label = "Pregnancy & Nursing Caution",
                    description = "Flag topical retinoids, high-strength salicylic acid (>2%), and chemical filters",
                    category = "special_care",
                    isEnabled = true
                ),
                PreferenceSettingEntity(
                    key = "pref_vegan",
                    label = "Vegan / Cruelty-Free Check",
                    description = "Flag animal-derived ingredients such as lanolin, carmine, and beeswax",
                    category = "dietary",
                    isEnabled = false
                ),
                PreferenceSettingEntity(
                    key = "pref_tree_nuts",
                    label = "Tree Nut & Peanut Allergens",
                    description = "Flag almond oil, argan oil, macadamia, walnut, and peanut derivatives",
                    category = "allergen",
                    isEnabled = true
                ),
                PreferenceSettingEntity(
                    key = "pref_gluten",
                    label = "Gluten / Wheat Derivatives",
                    description = "Flag hydrolyzed wheat protein, barley, and oat cross-contact warnings",
                    category = "allergen",
                    isEnabled = false
                ),
                PreferenceSettingEntity(
                    key = "pref_essential_oils",
                    label = "Essential Oils & Terpenes",
                    description = "Flag tea tree, lavender, eucalyptus, citrus peel oils, and aromatic terpenes",
                    category = "allergen",
                    isEnabled = false
                ),
                PreferenceSettingEntity(
                    key = "pref_soy",
                    label = "Soy & Legume Derivatives",
                    description = "Flag glycine soja, hydrolyzed soy protein, and soy lecithin",
                    category = "allergen",
                    isEnabled = false
                )
            )
            dao.insertPreferences(defaults)
        }
    }
}
