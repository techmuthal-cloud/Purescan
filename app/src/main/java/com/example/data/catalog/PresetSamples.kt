package com.example.data.catalog

data class SampleLabel(
    val id: String,
    val name: String,
    val category: String, // "skincare", "cosmetics", "food"
    val subtitle: String,
    val ingredientsText: String,
    val expectedOutcomeNote: String
)

object PresetSamples {
    val samples: List<SampleLabel> = listOf(
        SampleLabel(
            id = "sample_cleanser",
            name = "Hydrating Botanical Cleanser",
            category = "skincare",
            subtitle = "Gentle barrier face wash",
            ingredientsText = "Water, Glycerin, Cocamidopropyl Betaine, Sodium Hyaluronate, Camellia Sinensis Leaf Extract, Phenoxyethanol, Ethylhexylglycerin",
            expectedOutcomeNote = "Benign evidence (CIR Safe as formulated). No hazardous allergens detected."
        ),
        SampleLabel(
            id = "sample_retinol",
            name = "Age-Defying Retinol 0.3% Serum",
            category = "skincare",
            subtitle = "Active cellular renewal treatment",
            ingredientsText = "Water, Glycerin, Caprylic/Capric Triglyceride, Retinol, Niacinamide, Dimethicone, Phenoxyethanol, Disodium EDTA",
            expectedOutcomeNote = "Flags EU SCCS 0.3% limit caution & Pregnancy/Lactation avoidance alert."
        ),
        SampleLabel(
            id = "sample_sunscreen",
            name = "Daily Mineral Shield SPF 50",
            category = "skincare",
            subtitle = "Broad-spectrum physical protection",
            ingredientsText = "Zinc Oxide, Titanium Dioxide, Water, Butyloctyl Salicylate, Silica, Tocopherol, Phenoxyethanol",
            expectedOutcomeNote = "FDA GRASE mineral UV filters; EU aerosol caveat noted."
        ),
        SampleLabel(
            id = "sample_bodywash",
            name = "Aromatic Foaming Body Wash",
            category = "cosmetics",
            subtitle = "Conventional scented cleanser",
            ingredientsText = "Water, Sodium Laureth Sulfate, Cocamide DEA, Fragrance / Parfum, Methylparaben, Propylparaben, DMDM Hydantoin, Tartrazine CI 19140",
            expectedOutcomeNote = "Multiple findings: SLS/SLES sulfate, Fragrance allergen, Parabens, and Formaldehyde releaser."
        ),
        SampleLabel(
            id = "sample_oatbar",
            name = "Artisan Almond & Oat Snack Bar",
            category = "food",
            subtitle = "Nutrient whole-grain energy snack",
            ingredientsText = "Whole Grain Rolled Oats, Sweet Almond Oil, Honey, Pea Protein, Flaxseed, Sea Salt",
            expectedOutcomeNote = "Flags FALCPA Tree Nut allergen (Almond) and evaluates dietary criteria."
        )
    )
}
