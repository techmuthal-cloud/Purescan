package com.example.data.catalog

import com.example.data.model.CanonicalIngredient
import com.example.data.model.EvidenceRecord
import com.example.data.model.FindingType
import com.example.data.model.Severity

object IngredientCatalog {

    val allIngredients: List<CanonicalIngredient> = listOf(
        CanonicalIngredient(
            id = "ing_water",
            canonicalName = "Water / Aqua",
            category = "Solvent",
            aliases = listOf("water", "aqua", "eau", "purified water", "deionized water", "aqua/water/eau"),
            description = "Standard cosmetic and beverage solvent used as the liquid base for formulations.",
            casNumber = "7732-18-5",
            chemicalClass = "Inorganic Oxide / Solvent",
            maxSafeConcentration = "Up to 100% (No restriction)",
            dermalBioavailability = "Non-toxic physiological hydration across stratum corneum",
            toxicologyMechanism = "Biocompatible universal solvent; zero mutagenic, sensitizing, or systemic toxicity.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_water_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Safe as formulated. No toxicological concern.",
                    evidenceStrength = "Strong",
                    conditions = "General cosmetic use",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2020-05-12"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_glycerin",
            canonicalName = "Glycerin",
            category = "Humectant",
            aliases = listOf("glycerin", "glycerine", "glycerol"),
            description = "Naturally occurring humectant that attracts and locks moisture into the skin barrier.",
            casNumber = "56-81-5",
            chemicalClass = "Polyol / Trihydroxy Alcohol",
            maxSafeConcentration = "Up to 50% in leave-on, 78% in oral/rinse-off",
            dermalBioavailability = "High epidermal affinity via aquaporin-3 channels; minimal systemic risk",
            toxicologyMechanism = "Endogenous polyol metabolite; improves corneocyte cohesion and barrier lipid fluidity.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_glyc_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Evaluated safe as used in cosmetics without irritation potential at typical cosmetic levels.",
                    evidenceStrength = "Strong",
                    conditions = "Leave-on and rinse-off up to 50%",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org/ingredients/glycerin",
                    reviewDate = "2019-11-18"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_niacinamide",
            canonicalName = "Niacinamide (Vitamin B3)",
            category = "Active",
            aliases = listOf("niacinamide", "nicotinamide", "vitamin b3"),
            description = "Water-soluble vitamin that helps visibly improve enlarged pores, uneven skin tone, and weakened barrier.",
            casNumber = "98-92-0",
            chemicalClass = "Pyridinecarboxamide / Vitamin B3",
            maxSafeConcentration = "2% to 10% (Cosmetic leave-on)",
            dermalBioavailability = "Moderate-high dermal absorption (~10–15% over 24h)",
            toxicologyMechanism = "Precursor to NAD+/NADP+ coenzymes; inhibits melanosome transfer and upregulates ceramide synthesis. Free nicotinic acid impurity >0.1% may cause transient flushing.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_niac_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Safe as cosmetic ingredient. Well tolerated on sensitive skin barriers.",
                    evidenceStrength = "Strong",
                    conditions = "Concentrations up to 5%",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2021-03-04"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_retinol",
            canonicalName = "Retinol",
            category = "Active",
            aliases = listOf("retinol", "pure retinol", "vitamin a"),
            description = "Potent derivative of Vitamin A that accelerates cell turnover and stimulates collagen synthesis.",
            pregnancyCaution = true,
            casNumber = "68-26-8",
            chemicalClass = "Diterpenoid Retinoid / Vitamin A",
            maxSafeConcentration = "Max 0.3% facial leave-on; Max 0.05% body lotion (EU SCCS)",
            dermalBioavailability = "Approximately 5–8% percutaneous absorption",
            toxicologyMechanism = "Binds nuclear retinoic acid receptors (RAR/RXR) after enzymatic oxidation to all-trans retinoic acid. Cumulative systemic Vitamin A excess carries teratogenic risk during gestation.",
            saferAlternatives = listOf("Bakuchiol", "Palmitoyl Tripeptide-1", "Niacinamide"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_ret_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.REGULATORY_RESTRICTION,
                    severity = Severity.CAUTION,
                    summary = "SCCS scientific opinion (SCCS/1643/22) concluded safe in leave-on facial products up to 0.3% and body lotions up to 0.05% due to cumulative dietary Vitamin A exposure.",
                    evidenceStrength = "Strong",
                    conditions = "Maximum limit 0.3% in facial products; 0.05% in body lotion",
                    jurisdiction = "EU",
                    sourceUrl = "https://health.ec.europa.eu/publications/sccs-opinion-vitamin-retinol_en",
                    reviewDate = "2022-10-25"
                ),
                EvidenceRecord(
                    id = "ev_ret_preg",
                    organization = "US FDA / ACOG",
                    findingType = FindingType.HAZARD_EVIDENCE,
                    severity = Severity.CRITICAL,
                    summary = "Topical retinoids are advised to be avoided during pregnancy and nursing as a precautionary measure against teratogenic risks associated with excessive retinoid uptake.",
                    evidenceStrength = "Strong",
                    conditions = "Pregnancy & Lactation contraindicated",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.fda.gov/drugs/postmarket-drug-safety-information-patients-and-providers",
                    reviewDate = "2023-01-15"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_salicylic_acid",
            canonicalName = "Salicylic Acid",
            category = "Active / Exfoliant",
            aliases = listOf("salicylic acid", "beta hydroxy acid", "bha"),
            description = "Lipid-soluble beta-hydroxy acid that penetrates into pores to dissolve sebum and dead cellular buildup.",
            pregnancyCaution = true,
            casNumber = "69-72-7",
            chemicalClass = "Beta-Hydroxy Acid / Phenolic Acid",
            maxSafeConcentration = "Max 2.0% leave-on; Max 3.0% rinse-off hair",
            dermalBioavailability = "10–25% percutaneous absorption depending on vehicle pH",
            toxicologyMechanism = "Keratolytic desmosome dissolution; high-surface-area application (>2%) poses salicylate systemic toxicity risk in infants.",
            saferAlternatives = listOf("Gluconolactone (PHA)", "Lactobionic Acid", "Willow Bark Extract (Low %)") ,
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_sal_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.REGULATORY_RESTRICTION,
                    severity = Severity.CAUTION,
                    summary = "SCCS/1601/18 restricts salicylic acid to maximum 2.0% in leave-on cosmetic products, and 3.0% in rinse-off hair products.",
                    evidenceStrength = "Strong",
                    conditions = "Max 2.0% leave-on; not permitted in products for children under 3 years except shampoo",
                    jurisdiction = "EU",
                    sourceUrl = "https://ec.europa.eu/health/scientific_committees/consumer_safety_en",
                    reviewDate = "2021-06-30"
                ),
                EvidenceRecord(
                    id = "ev_sal_fda",
                    organization = "US FDA",
                    findingType = FindingType.REGULATORY_RESTRICTION,
                    severity = Severity.INFO,
                    summary = "Regulated as OTC acne active ingredient at 0.5% to 2.0% concentration.",
                    evidenceStrength = "Strong",
                    conditions = "0.5% - 2.0% OTC Monograph",
                    jurisdiction = "US",
                    sourceUrl = "https://www.fda.gov/cosmetics/cosmetic-ingredients/beta-hydroxy-acids",
                    reviewDate = "2020-09-10"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_fragrance",
            canonicalName = "Fragrance / Parfum",
            category = "Fragrance",
            aliases = listOf("fragrance", "parfum", "aroma", "perfume", "flavor / aroma"),
            description = "A proprietary blend of scent chemicals and aroma compounds commonly associated with contact sensitization.",
            isFragrance = true,
            isAllergen = true,
            casNumber = "Mixture (IFRA)",
            chemicalClass = "Volatile Terpenes, Aldehydes & Musk Esters",
            maxSafeConcentration = "IFRA Category-dependent; EU mandates disclosure >0.001% leave-on",
            dermalBioavailability = "Variable rapid epidermal penetration of low-MW volatile terpenes",
            toxicologyMechanism = "Haptenation of epidermal proteins after autoxidation (e.g., hydroperoxides of limonene/linalool), triggering T-cell-mediated Type IV allergic contact dermatitis.",
            saferAlternatives = listOf("Fragrance-Free Formulations", "Triethyl Citrate", "Bisabolol"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_frag_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.ALLERGEN_ALERT,
                    severity = Severity.CAUTION,
                    summary = "Fragrance mixes are among the leading triggers of allergic contact dermatitis in cosmetics. The EU requires specific declaration of 56 established fragrance contact allergens.",
                    evidenceStrength = "Strong",
                    conditions = "Must comply with IFRA safe usage standards and individual allergen declaration thresholds",
                    jurisdiction = "EU",
                    sourceUrl = "https://ec.europa.eu/growth/sectors/cosmetics/legislation_en",
                    reviewDate = "2023-07-26"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_sls",
            canonicalName = "Sodium Lauryl Sulfate (SLS)",
            category = "Surfactant",
            aliases = listOf("sodium lauryl sulfate", "sls", "sodium dodecyl sulfate"),
            description = "Strong anionic cleansing agent known to cause skin and eye irritation at higher concentrations or extended contact.",
            isSulfate = true,
            casNumber = "151-21-3",
            chemicalClass = "Anionic Alkyl Sulfate Surfactant",
            maxSafeConcentration = "Max 1.0% in leave-on; Rinse-off non-irritating formulation",
            dermalBioavailability = "Low systemic uptake, high stratum corneum protein binding",
            toxicologyMechanism = "Denatures keratin proteins and solubilizes intercellular ceramide bilayers, elevating transepidermal water loss (TEWL) and triggering pro-inflammatory cytokine release.",
            saferAlternatives = listOf("Coco-Glucoside", "Decyl Glucoside", "Sodium Cocoyl Isethionate"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_sls_cir",
                    organization = "US CIR",
                    findingType = FindingType.HAZARD_EVIDENCE,
                    severity = Severity.CAUTION,
                    summary = "CIR Expert Panel concluded SLS is safe in rinse-off products, but leave-on products should not exceed 1% concentration due to cutaneous barrier disruption and irritation.",
                    evidenceStrength = "Strong",
                    conditions = "Safe in rinse-off; max 1% in leave-on formulations",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2020-04-14"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_sles",
            canonicalName = "Sodium Laureth Sulfate (SLES)",
            category = "Surfactant",
            aliases = listOf("sodium laureth sulfate", "sles", "sodium lauryl ether sulfate"),
            description = "Ethoxylated surfactant milder than SLS, but ethoxylation may produce trace 1,4-dioxane byproducts if unpurified.",
            isSulfate = true,
            casNumber = "68585-34-2 / 9004-82-4",
            chemicalClass = "Ethoxylated Anionic Surfactant",
            maxSafeConcentration = "Rinse-off up to 20%; Vacuum-stripped <10 ppm 1,4-dioxane",
            dermalBioavailability = "Minimal dermal penetration due to larger micellar radius",
            toxicologyMechanism = "Milder protein interaction than SLS; primary toxicological vigilance centers on vacuum-stripping trace 1,4-dioxane ethoxylation byproducts.",
            saferAlternatives = listOf("Sodium Lauroyl Methyl Isethionate", "Sodium Coco-Sulfate", "Lauryl Glucoside"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_sles_cir",
                    organization = "US CIR",
                    findingType = FindingType.REGULATORY_RESTRICTION,
                    severity = Severity.INFO,
                    summary = "Safe as cosmetic ingredient when formulated to be non-irritating and purified to minimize trace 1,4-dioxane.",
                    evidenceStrength = "Strong",
                    conditions = "Rinse-off cleansing formulations",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2018-08-22"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_methylparaben",
            canonicalName = "Methylparaben",
            category = "Preservative",
            aliases = listOf("methylparaben", "methyl 4-hydroxybenzoate", "e218"),
            description = "Alkyl ester of p-hydroxybenzoic acid used as broad-spectrum antimicrobial preservative.",
            isParaben = true,
            casNumber = "99-76-3",
            chemicalClass = "Paraben / p-Hydroxybenzoate Ester",
            maxSafeConcentration = "Max 0.4% single ester; 0.8% combined esters (EU Annex V)",
            dermalBioavailability = "Hydrolyzed rapidly by cutaneous carboxylesterases to p-hydroxybenzoic acid",
            toxicologyMechanism = "Disrupts microbial membrane transport; exhibits ~10,000-fold weaker binding affinity to estrogen receptors than endogenous 17β-estradiol.",
            saferAlternatives = listOf("Sodium Benzoate + Potassium Sorbate", "Pentylene Glycol", "Ethylhexylglycerin"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_mpar_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.REGULATORY_RESTRICTION,
                    severity = Severity.CAUTION,
                    summary = "EU Regulation Annex V permits methylparaben up to 0.4% as single ester, or 0.8% in parabens mixture.",
                    evidenceStrength = "Strong",
                    conditions = "Maximum 0.4% concentration single; 0.8% combined",
                    jurisdiction = "EU",
                    sourceUrl = "https://ec.europa.eu/growth/tools-databases/cosing/",
                    reviewDate = "2021-02-17"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_propylparaben",
            canonicalName = "Propylparaben",
            category = "Preservative",
            aliases = listOf("propylparaben", "propyl 4-hydroxybenzoate", "e216"),
            description = "Longer chain paraben preservative regulated with stricter limits due to weak estrogenic receptor affinity.",
            isParaben = true,
            casNumber = "94-13-3",
            chemicalClass = "Paraben / Propyl Ester",
            maxSafeConcentration = "Max 0.14% (as acid); Prohibited in under-3 nappy area leave-on",
            dermalBioavailability = "Moderate dermal absorption; partially metabolized in epidermis",
            toxicologyMechanism = "Longer alkyl chain increases lipophilicity and weak estrogenic receptor (ERα/ERβ) affinity compared to methylparaben.",
            saferAlternatives = listOf("Caprylyl Glycol", "Phenoxyethanol", "Leuconostoc/Radish Root Ferment"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_ppar_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.REGULATORY_RESTRICTION,
                    severity = Severity.CAUTION,
                    summary = "EU restricts propylparaben to maximum 0.14% (calculated as acid). Prohibited in leave-on products designed for diaper area of children under 3.",
                    evidenceStrength = "Strong",
                    conditions = "Maximum 0.14%; prohibited in diaper area leave-on products",
                    jurisdiction = "EU",
                    sourceUrl = "https://ec.europa.eu/health/scientific_committees/consumer_safety_en",
                    reviewDate = "2020-11-05"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_phenoxyethanol",
            canonicalName = "Phenoxyethanol",
            category = "Preservative",
            aliases = listOf("phenoxyethanol", "2-phenoxyethanol", "ethylene glycol monophenyl ether"),
            description = "Widely used synthetic aromatic ether preservative providing antimicrobial protection.",
            casNumber = "122-99-6",
            chemicalClass = "Glycol Ether / Aromatic Alcohol",
            maxSafeConcentration = "Max 1.0% in all cosmetic formulations (EU Annex V)",
            dermalBioavailability = "Moderate absorption; rapidly oxidized to 2-phenoxyacetic acid and excreted renally",
            toxicologyMechanism = "Uncouples oxidative phosphorylation in bacteria; safe up to 1.0% with rare contact eczema in hyper-reactive barriers.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_phen_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.REGULATORY_RESTRICTION,
                    severity = Severity.INFO,
                    summary = "SCCS/1575/15 concluded phenoxyethanol is safe for all consumers, including children, at maximum concentration of 1.0%.",
                    evidenceStrength = "Strong",
                    conditions = "Maximum concentration 1.0% in all cosmetics",
                    jurisdiction = "EU",
                    sourceUrl = "https://health.ec.europa.eu/publications/sccs-opinion-phenoxyethanol_en",
                    reviewDate = "2016-10-06"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_dimethicone",
            canonicalName = "Dimethicone",
            category = "Emollient / Silicone",
            aliases = listOf("dimethicone", "polydimethylsiloxane", "pdms"),
            description = "Linear silicone polymer that creates a protective, breathable, velvety barrier on skin surface.",
            isSilicone = true,
            casNumber = "9006-65-9 / 63148-62-9",
            chemicalClass = "Siloxane Polymer",
            maxSafeConcentration = "Up to 30% OTC skin protectant; 100% cosmetic barrier",
            dermalBioavailability = "0% dermal penetration (High molecular weight >500 Da)",
            toxicologyMechanism = "Biologically inert macromolecule; remains on outer stratum corneum to reduce friction and prevent water loss without pore occlusion.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_dim_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Evaluated safe as used in cosmetics. Molecular weight is too large to penetrate healthy human dermis.",
                    evidenceStrength = "Strong",
                    conditions = "Safe up to 100% in barrier creams",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2021-09-12"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_dmdm_hydantoin",
            canonicalName = "DMDM Hydantoin",
            category = "Preservative",
            aliases = listOf("dmdm hydantoin", "dimethyloldimethyl hydantoin"),
            description = "Formaldehyde-releasing antimicrobial preservative.",
            isAllergen = true,
            casNumber = "6440-58-0",
            chemicalClass = "Hydantoin Formaldehyde-Donor",
            maxSafeConcentration = "Max 0.6% (Warning required if free formaldehyde >0.001% / 10 ppm)",
            dermalBioavailability = "Low parent penetration; released formaldehyde reacts locally with epidermal nucleophiles",
            toxicologyMechanism = "Hydrolyzes in aqueous vehicles to release equilibrium trace formaldehyde, cross-linking proteins and sensitizing Langerhans cells.",
            saferAlternatives = listOf("Sodium Benzoate", "Phenoxyethanol", "Sorbitan Caprylate"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_dmdm_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.HAZARD_EVIDENCE,
                    severity = Severity.CAUTION,
                    summary = "Releases trace formaldehyde to inhibit bacterial growth. The EU requires the warning 'Contains formaldehyde' if released formaldehyde exceeds 0.001% (10 ppm).",
                    evidenceStrength = "Strong",
                    conditions = "Must disclose formaldehyde if >10 ppm",
                    jurisdiction = "EU",
                    sourceUrl = "https://ec.europa.eu/growth/sectors/cosmetics/legislation_en",
                    reviewDate = "2022-07-11"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_oxybenzone",
            canonicalName = "Oxybenzone (Benzophenone-3)",
            category = "UV Filter",
            aliases = listOf("oxybenzone", "benzophenone-3", "bp-3"),
            description = "Chemical sunscreen agent that absorbs UVA and UVB radiation.",
            casNumber = "131-57-7",
            chemicalClass = "Benzophenone Derivative",
            maxSafeConcentration = "Max 2.2% body / 6.0% face (EU SCCS); Restricted in Hawaii/Key West",
            dermalBioavailability = "High systemic absorption (~8–10% detected in plasma after whole-body application)",
            toxicologyMechanism = "Photo-sensitizer and suspected endocrine modulator with documented coral planulae bleaching toxicity.",
            saferAlternatives = listOf("Non-Nano Zinc Oxide", "Bemotrizinol (Tinosorb S)", "Diethylamino Hydroxybenzoyl Hexyl Benzoate"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_oxy_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.REGULATORY_RESTRICTION,
                    severity = Severity.CAUTION,
                    summary = "SCCS/1625/20 restricted maximum safe concentration to 6% in face sunscreens, 2.2% in body sunscreens, and 0.5% in other cosmetics due to endocrine activity concerns.",
                    evidenceStrength = "Strong",
                    conditions = "Max 6% in face sunscreens; environmental reef restrictions in HI/PW",
                    jurisdiction = "EU",
                    sourceUrl = "https://health.ec.europa.eu/publications/sccs-opinion-benzophenone-3_en",
                    reviewDate = "2021-03-31"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_zinc_oxide",
            canonicalName = "Zinc Oxide",
            category = "UV Filter / Protectant",
            aliases = listOf("zinc oxide", "ci 77947", "non-nano zinc oxide"),
            description = "Broad-spectrum physical mineral sunscreen and skin protectant active ingredient.",
            casNumber = "1314-13-2",
            chemicalClass = "Inorganic Mineral Oxide",
            maxSafeConcentration = "Up to 25% (US FDA GRASE / EU Annex VI)",
            dermalBioavailability = "<0.01% dermal penetration across intact human skin",
            toxicologyMechanism = "Photostable semiconductor mineral that reflects, scatters, and absorbs UVA1/UVA2/UVB photons without generating reactive oxygen species when coated.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_zno_fda",
                    organization = "US FDA",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Designated by the US FDA as GRASE (Generally Recognized as Safe and Effective) for sunscreen formulations.",
                    evidenceStrength = "Strong",
                    conditions = "Safe up to 25% OTC sunscreen; avoid spray inhalation",
                    jurisdiction = "US",
                    sourceUrl = "https://www.fda.gov/drugs/understanding-over-counter-medicines/sunscreen-how-help-protect-your-skin-sun",
                    reviewDate = "2023-05-19"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_titanium_dioxide",
            canonicalName = "Titanium Dioxide",
            category = "UV Filter / Colorant",
            aliases = listOf("titanium dioxide", "ci 77891", "tio2", "e171"),
            description = "Inorganic mineral used as white pigment and physical UV blocker reflecting harmful radiation.",
            casNumber = "13463-67-7",
            chemicalClass = "Inorganic Transition Metal Oxide",
            maxSafeConcentration = "Up to 25% topical lotions; Banned as food additive (E171) in EU",
            dermalBioavailability = "Negligible dermal penetration; alveolar retention hazard if inhaled as aerosol",
            toxicologyMechanism = "Inert topically on skin barrier; nanoparticles in spray/powder or ingested E171 raise genotoxicity and pulmonary inflammation concerns.",
            saferAlternatives = listOf("Non-Nano Zinc Oxide", "Iron Oxides (Pigment)"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_tio2_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.REGULATORY_RESTRICTION,
                    severity = Severity.CAUTION,
                    summary = "Safe for topical skin application up to 25%. Inhalation risk in spray/aerosol applications; banned as food additive (E171) in EU by EFSA.",
                    evidenceStrength = "Strong",
                    conditions = "Permitted in lotions up to 25%; not permitted in aerosol sprays or EU food",
                    jurisdiction = "EU",
                    sourceUrl = "https://www.efsa.europa.eu/en/efsajournal/pub/6585",
                    reviewDate = "2021-05-06"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_hyaluronic_acid",
            canonicalName = "Sodium Hyaluronate (Hyaluronic Acid)",
            category = "Humectant",
            aliases = listOf("sodium hyaluronate", "hyaluronic acid", "hydrolyzed hyaluronic acid"),
            description = "Glycosaminoglycan with immense water-binding capacity capable of holding up to 1000x its weight in water.",
            casNumber = "9067-32-7",
            chemicalClass = "Anionic Nonsulfated Glycosaminoglycan",
            maxSafeConcentration = "Up to 2.0% in leave-on serums and creams",
            dermalBioavailability = "MW-dependent: >500 kDa forms surface hydration film; <50 kDa penetrates epidermis",
            toxicologyMechanism = "Endogenous extracellular matrix polymer; zero sensitization or systemic hazard.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_ha_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "CIR Expert Panel concluded sodium hyaluronate and hyaluronic acid are safe as cosmetic ingredients.",
                    evidenceStrength = "Strong",
                    conditions = "Cosmetic formulation up to 2%",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2021-07-20"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_ceramide",
            canonicalName = "Ceramide NP",
            category = "Lipid / Barrier Restorative",
            aliases = listOf("ceramide np", "ceramide 3", "ceramide", "ceramide ap", "ceramide eop"),
            description = "Skin-identical lipid that reinforces moisture retention and protects against external environmental irritants.",
            casNumber = "100403-19-8",
            chemicalClass = "Sphingolipid / N-Acylsphingosine",
            maxSafeConcentration = "Up to 1.0% in barrier formulations",
            dermalBioavailability = "Integrates directly into intercellular lamellar lipid matrix of stratum corneum",
            toxicologyMechanism = "Biomimetic epidermal lipid restoring tight junction and lamellar barrier integrity.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_cer_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Biocompatible skin lipid. Evaluated safe as formulated in skin barrier creams.",
                    evidenceStrength = "Strong",
                    conditions = "Concentrations up to 1%",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2020-09-08"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_tocopherol",
            canonicalName = "Tocopherol (Vitamin E)",
            category = "Antioxidant",
            aliases = listOf("tocopherol", "tocopheryl acetate", "vitamin e", "dl-alpha tocopherol"),
            description = "Lipid-soluble antioxidant that protects product oils from oxidation and neutralizes free radicals on skin.",
            casNumber = "59-02-9 / 10191-41-0",
            chemicalClass = "Methylated Phenol / Tocochromanol",
            maxSafeConcentration = "0.1% to 5.0% in leave-on cosmetics",
            dermalBioavailability = "Accumulates in sebaceous glands and lipophilic stratum corneum domains",
            toxicologyMechanism = "Chain-breaking peroxyl radical scavenger preventing squalene and lipid peroxidation.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_toc_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Safe as used in cosmetics and food. Rare mild contact dermatitis reported at 100% pure oil.",
                    evidenceStrength = "Strong",
                    conditions = "Cosmetic concentrations up to 5%",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2019-04-12"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_almond_oil",
            canonicalName = "Sweet Almond Oil (Prunus Amygdalus Dulcis)",
            category = "Botanical / Emollient",
            aliases = listOf("sweet almond oil", "prunus amygdalus dulcis oil", "almond oil", "almond butter"),
            description = "Nourishing plant oil rich in oleic and linoleic essential fatty acids.",
            isAllergen = true,
            casNumber = "8007-69-0",
            chemicalClass = "Triglyceride Botanical Fixed Oil",
            maxSafeConcentration = "Up to 100% (Allergen disclosure required for tree-nut sensitive users)",
            dermalBioavailability = "Remains primarily in upper epidermal lipid layers",
            toxicologyMechanism = "Residual amandin (Prunus dulcis 2S albumin / legumin) seed storage proteins can trigger IgE-mediated hypersensitivity in tree-nut allergic individuals.",
            saferAlternatives = listOf("Squalane (Olive/Sugarcane)", "Jojoba Seed Oil", "Sunflower Seed Oil"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_almond_allergy",
                    organization = "US FDA / EFSA",
                    findingType = FindingType.ALLERGEN_ALERT,
                    severity = Severity.CAUTION,
                    summary = "Classified as a major tree nut allergen under the Food Allergen Labeling and Consumer Protection Act (FALCPA). Contact sensitization possible in individuals with nut allergies.",
                    evidenceStrength = "Strong",
                    conditions = "Mandatory FALCPA allergen disclosure in foods; caution in cosmetics for nut allergy sufferers",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.fda.gov/food/food-labeling-nutrition/food-allergies",
                    reviewDate = "2022-03-14"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_oats",
            canonicalName = "Colloidal Oatmeal (Avena Sativa)",
            category = "Botanical / Protectant",
            aliases = listOf("colloidal oatmeal", "avena sativa kernel flour", "oat extract", "whole grain rolled oats", "oats"),
            description = "Anti-inflammatory botanical rich in beta-glucans, avenanthramides, and soothing starches.",
            casNumber = "134134-86-4",
            chemicalClass = "Botanical Polysaccharide & Polyphenol Complex",
            maxSafeConcentration = "Min 0.007% bath; 0.1%–5% OTC skin protectant",
            dermalBioavailability = "Surface film-forming beta-glucans; localized epidermal anti-inflammatory activity",
            toxicologyMechanism = "Avenanthramides inhibit NF-κB activation and histamine release in irritated keratinocytes.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_oat_fda",
                    organization = "US FDA",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Designated by US FDA as an approved OTC Skin Protectant active ingredient for relieving itching and minor skin irritations.",
                    evidenceStrength = "Strong",
                    conditions = "Approved 0.007% to 1.0% in bath products; min 0.1% skin protectant",
                    jurisdiction = "US",
                    sourceUrl = "https://www.fda.gov",
                    reviewDate = "2020-01-20"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_yellow_5",
            canonicalName = "Tartrazine (CI 19140 / Yellow 5)",
            category = "Colorant",
            aliases = listOf("ci 19140", "yellow 5", "tartrazine", "fd&c yellow no. 5", "e102"),
            description = "Synthetic azo dye imparting bright yellow coloration to cosmetic and food items.",
            isAllergen = true,
            casNumber = "1934-21-0",
            chemicalClass = "Sulfonated Azo Dye",
            maxSafeConcentration = "ADI 7.5 mg/kg bw/day (EFSA); Purity-certified batches only",
            dermalBioavailability = "Low (<1%) intact dermal absorption; oral absorption metabolized by gut azo-reductases",
            toxicologyMechanism = "Azo linkage cleavage produces sulfanilic acid metabolites; cross-reactivity documented in aspirin-intolerant urticaria/asthma cohorts.",
            saferAlternatives = listOf("Curcumin (CI 75300)", "Beta-Carotene", "Riboflavin"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_y5_eu",
                    organization = "EU EFSA",
                    findingType = FindingType.ALLERGEN_ALERT,
                    severity = Severity.CAUTION,
                    summary = "May cause mild allergic or intolerance reactions (urticaria, asthma) in sensitive individuals, especially those with aspirin sensitivity. In EU foods, requires warning label regarding children's activity/attention.",
                    evidenceStrength = "Moderate",
                    conditions = "EU food warning required; cosmetic purity criteria enforced",
                    jurisdiction = "EU",
                    sourceUrl = "https://www.efsa.europa.eu",
                    reviewDate = "2019-12-08"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_shea_butter",
            canonicalName = "Shea Butter (Butyrospermum Parkii)",
            category = "Botanical / Emollient",
            aliases = listOf("shea butter", "butyrospermum parkii butter", "butyrospermum parkii"),
            description = "Vegetable fat extracted from the African karite tree nut, rich in triterpenes and fatty acids.",
            casNumber = "194043-92-0 / 91080-23-8",
            chemicalClass = "Botanical Triglyceride & Triterpene Ester Complex",
            maxSafeConcentration = "Up to 100% (Non-sensitizing refined butter)",
            dermalBioavailability = "Occlusive and emollient integration within stratum corneum",
            toxicologyMechanism = "Contains cinnamic acid triterpene esters with soothing anti-protease properties; extremely low protein allergenicity even in tree-nut cohorts.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_shea_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Safe as formulated in cosmetic products. Excellent non-comedogenic tolerance.",
                    evidenceStrength = "Strong",
                    conditions = "Safe up to 100%",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2019-08-16"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_carmine",
            canonicalName = "Carmine (CI 75470)",
            category = "Colorant",
            aliases = listOf("carmine", "ci 75470", "cochineal extract", "natural red 4", "e120"),
            description = "Natural bright crimson pigment derived from crushed female cochineal insects.",
            isAnimalDerived = true,
            isAllergen = true,
            casNumber = "1390-65-4",
            chemicalClass = "Anthraquinone Aluminum Lake Pigment (Carminic Acid)",
            maxSafeConcentration = "Good Manufacturing Practice (Must be explicitly declared on US/EU labels)",
            dermalBioavailability = "Insoluble lake pigment; protein impurities trigger IgE response on mucosa/lips",
            toxicologyMechanism = "Residual insect protein allergens (CC38K) bound to carminic acid can provoke IgE-mediated cheilitis or systemic anaphylaxis.",
            saferAlternatives = listOf("Red Iron Oxide (CI 77491)", "Anthocyanins", "Beetroot Red"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_carmine_fda",
                    organization = "US FDA",
                    findingType = FindingType.PREFERENCE_MISMATCH,
                    severity = Severity.CAUTION,
                    summary = "Mandatory declaration in food and cosmetics due to animal insect origin (non-vegan/vegetarian) and documented rare anaphylaxis or contact allergy.",
                    evidenceStrength = "Strong",
                    conditions = "Must be declared on ingredient labels by name",
                    jurisdiction = "US",
                    sourceUrl = "https://www.fda.gov",
                    reviewDate = "2020-03-24"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_lanolin",
            canonicalName = "Lanolin",
            category = "Emollient",
            aliases = listOf("lanolin", "wool fat", "lanolin oil", "lanolin alcohol"),
            description = "Waxy substance secreted by sheep sebaceous glands for conditioning wool fibers.",
            isAnimalDerived = true,
            isAllergen = true,
            casNumber = "8006-54-0",
            chemicalClass = "Sterol & High-MW Wax Ester Complex",
            maxSafeConcentration = "Up to 30% in barrier ointments (Purified medical grade recommended)",
            dermalBioavailability = "Low systemic absorption; forms semi-occlusive barrier film",
            toxicologyMechanism = "Free aliphatic wool alcohols (Amerchol L-101 fraction) can act as weak haptens in compromised or venous-stasis skin barriers.",
            saferAlternatives = listOf("Bis-Diglyceryl Polyacyladipate-2", "Shea Butter", "Phytosteryl Macadamiate"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_lano_cir",
                    organization = "US CIR",
                    findingType = FindingType.ALLERGEN_ALERT,
                    severity = Severity.INFO,
                    summary = "Safe for general population, but known allergen for individuals with eczema or broken skin barrier. Not suitable for vegan consumers.",
                    evidenceStrength = "Strong",
                    conditions = "Safe in general use; caution for sensitive/atopic dermatitis",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2018-05-10"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_palm_oil",
            canonicalName = "Palm Oil / Elaeis Guineensis",
            category = "Plant Oil",
            aliases = listOf("palm oil", "elaeis guineensis oil", "sodium palmate", "palm kernel oil"),
            description = "Edible vegetable oil derived from the mesocarp of the fruit of the oil palm.",
            casNumber = "8002-75-3",
            chemicalClass = "Palmitic & Oleic Triglyceride Complex",
            maxSafeConcentration = "No toxicological limit; RSPO sustainability certification recommended",
            dermalBioavailability = "Topical emollient; dietary absorption metabolized as fatty acids",
            toxicologyMechanism = "Biologically benign topical lipid; high palmitic acid content may be mildly comedogenic on acne-prone skin.",
            saferAlternatives = listOf("RSPO Certified Shea Butter", "Babassu Oil", "Sunflower Seed Oil"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_palm_pref",
                    organization = "RSPO / EFSA",
                    findingType = FindingType.PREFERENCE_MISMATCH,
                    severity = Severity.INFO,
                    summary = "High saturated fat content. Frequently flagged by consumers due to tropical deforestation and environmental sustainability preferences.",
                    evidenceStrength = "Moderate",
                    conditions = "Safe for consumption; sustainability flag",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://rspo.org",
                    reviewDate = "2021-11-12"
                )
            )
        ),
        // High-Risk / Prohibited & Severely Restricted Ingredients for Complete Hazard Filtering
        CanonicalIngredient(
            id = "ing_formaldehyde",
            canonicalName = "Formaldehyde (Methylene Glycol)",
            category = "Preservative / Keratin Cross-Linker",
            aliases = listOf("formaldehyde", "methylene glycol", "formalin", "methanal"),
            description = "Reactive aldehyde gas in solution historically used in nail hardeners and hair-smoothing treatments; classified as a known human carcinogen.",
            isAllergen = true,
            casNumber = "50-00-0 / 463-57-0",
            chemicalClass = "Simple Aldehyde",
            maxSafeConcentration = "Prohibited in EU Cosmetics (Annex II); US FDA proposed ban in hair straighteners",
            dermalBioavailability = "Rapid mucosal inhalation and cutaneous covalent binding",
            toxicologyMechanism = "Forms DNA-protein cross-links and potent epidermal hapten adducts; IARC Group 1 human carcinogen (nasopharyngeal & myeloid pathways) and potent contact allergen.",
            saferAlternatives = listOf("Glyoxylic Acid Carbocysteine Systems", "Phenoxyethanol", "Sodium Benzoate"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_form_eu",
                    organization = "EU SCCS / IARC",
                    findingType = FindingType.HAZARD_EVIDENCE,
                    severity = Severity.CRITICAL,
                    summary = "Classified as a Group 1 Carcinogen by WHO/IARC and CMR 1B substance. Strictly prohibited in cosmetic products under EU Regulation (EC) No 1223/2009 Annex II.",
                    evidenceStrength = "Strong",
                    conditions = "Prohibited in EU cosmetics; severe inhalation & sensitization hazard",
                    jurisdiction = "EU",
                    sourceUrl = "https://ec.europa.eu/growth/tools-databases/cosing/",
                    reviewDate = "2023-06-15"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_mit",
            canonicalName = "Methylisothiazolinone (MIT)",
            category = "Preservative",
            aliases = listOf("methylisothiazolinone", "mit", "2-methyl-4-isothiazolin-3-one", "kathon cg"),
            description = "Potent heterocyclic biocide preservative strongly linked to severe epidemic allergic contact dermatitis.",
            isAllergen = true,
            casNumber = "2682-20-4",
            chemicalClass = "Isothiazolinone Heterocycle",
            maxSafeConcentration = "Banned in leave-on cosmetics in EU; Max 0.0015% (15 ppm) in rinse-off",
            dermalBioavailability = "High epidermal reactivity with cysteine thiol residues",
            toxicologyMechanism = "Electrophilic sulfur-nitrogen ring attacks nucleophilic thiol groups on cutaneous proteins, inducing severe dendritic cell activation and delayed hypersensitivity.",
            saferAlternatives = listOf("Ethylhexylglycerin", "Caprylyl Glycol", "Potassium Sorbate"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_mit_sccs",
                    organization = "EU SCCS",
                    findingType = FindingType.HAZARD_EVIDENCE,
                    severity = Severity.CRITICAL,
                    summary = "Banned in all leave-on cosmetic products (including wet wipes) in the EU due to high rates of severe allergic contact dermatitis. Restricted to 15 ppm (0.0015%) in rinse-off products.",
                    evidenceStrength = "Strong",
                    conditions = "Prohibited in leave-on products; max 0.0015% rinse-off",
                    jurisdiction = "EU",
                    sourceUrl = "https://health.ec.europa.eu/scientific-committees/scientific-committee-consumer-safety-sccs_en",
                    reviewDate = "2022-04-19"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_lilial",
            canonicalName = "Butylphenyl Methylpropional (Lilial)",
            category = "Fragrance",
            aliases = listOf("butylphenyl methylpropional", "lilial", "p-bmhca", "lily aldehyde"),
            description = "Synthetic floral aldehyde fragrance compound prohibited in EU/UK cosmetics due to reproductive toxicity.",
            isFragrance = true,
            isAllergen = true,
            pregnancyCaution = true,
            casNumber = "80-54-6",
            chemicalClass = "Aromatic Aldehyde Fragrance",
            maxSafeConcentration = "Prohibited in EU & UK Cosmetics (Annex II Entry 1666)",
            dermalBioavailability = " ~14% percutaneous absorption under occlusion",
            toxicologyMechanism = "Classified as Toxic for Reproduction Category 1B (CMR 1B) based on adverse developmental and spermatogenic effects in animal toxicology studies.",
            saferAlternatives = listOf("Florosa (Pyranol)", "Hydroxycitronellal (IFRA limited)", "Fragrance-Free"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_lilial_eu",
                    organization = "EU SCCS",
                    findingType = FindingType.HAZARD_EVIDENCE,
                    severity = Severity.CRITICAL,
                    summary = "Banned across all cosmetic categories in the European Union and UK effective March 2022 after classification as a CMR 1B reproductive toxicant.",
                    evidenceStrength = "Strong",
                    conditions = "Prohibited in EU/UK cosmetics (0% tolerance)",
                    jurisdiction = "EU",
                    sourceUrl = "https://ec.europa.eu/growth/sectors/cosmetics/legislation_en",
                    reviewDate = "2022-03-01"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_hydroquinone",
            canonicalName = "Hydroquinone",
            category = "Active / Depigmenting Agent",
            aliases = listOf("hydroquinone", "1,4-dihydroxybenzene", "quinol"),
            description = "Potent tyrosinase inhibitor skin-lightening chemical restricted to prescription use due to exogenous ochronosis and mutagenicity risks.",
            pregnancyCaution = true,
            casNumber = "123-31-9",
            chemicalClass = "Aromatic Diol / Phenol Derivative",
            maxSafeConcentration = "Prohibited in OTC cosmetics in US, EU, UK & Japan (Prescription drug only)",
            dermalBioavailability = "High rapid percutaneous absorption (~35–45% across human forehead skin)",
            toxicologyMechanism = "Cytotoxic to melanocytes via semiquinone radical formation; chronic use causes irreversible blue-black dermal pigmentation (exogenous ochronosis).",
            saferAlternatives = listOf("Alpha-Arbutin (up to 2%)", "Azelaic Acid", "Tranexamic Acid", "Vitamin C"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_hq_fda_eu",
                    organization = "US FDA / EU SCCS",
                    findingType = FindingType.HAZARD_EVIDENCE,
                    severity = Severity.CRITICAL,
                    summary = "Prohibited in over-the-counter cosmetics in both the US (CARES Act) and EU Annex II due to exogenous ochronosis, leukoderma, and renal/hepatic carcinogenic findings in rodent bioassays.",
                    evidenceStrength = "Strong",
                    conditions = "Prohibited in OTC skin cosmetics; prescription dermatological supervision only",
                    jurisdiction = "EU",
                    sourceUrl = "https://www.fda.gov/drugs/drug-safety-and-availability/fda-works-protect-consumers-potentially-harmful-otc-skin-lightening-products",
                    reviewDate = "2023-02-10"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_triclosan",
            canonicalName = "Triclosan",
            category = "Antimicrobial / Biocide",
            aliases = listOf("triclosan", "5-chloro-2-(2,4-dichlorophenoxy)phenol", "irgasan dp300"),
            description = "Chlorinated aromatic antibacterial compound restricted due to thyroid hormone disruption and antimicrobial cross-resistance.",
            casNumber = "3380-34-5",
            chemicalClass = "Polychloro Phenoxy Phenol",
            maxSafeConcentration = "Banned in US OTC consumer antiseptic washes; Max 0.3% in EU toothpastes/deodorants",
            dermalBioavailability = "10–15% dermal and oral mucosal absorption",
            toxicologyMechanism = "Inhibits bacterial enoyl-acyl carrier protein reductase (FabI); interferes with mammalian thyroid hormone homeostasis (T4 clearance) and forms dioxin photoproducts under UV.",
            saferAlternatives = listOf("Capryloyl Glycine", "Piroctone Olamine", "Zinc Ricinoleate"),
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_tric_fda",
                    organization = "US FDA / EU SCCS",
                    findingType = FindingType.HAZARD_EVIDENCE,
                    severity = Severity.CRITICAL,
                    summary = "Banned by US FDA in consumer antibacterial hand and body soaps; EU SCCS restricts to 0.3% in limited rinse-off/oral products due to endocrine disruption and antibiotic resistance concerns.",
                    evidenceStrength = "Strong",
                    conditions = "Prohibited in US OTC antiseptic washes; max 0.3% EU restricted categories",
                    jurisdiction = "EU",
                    sourceUrl = "https://www.fda.gov/consumers/consumer-updates/5-things-know-about-triclosan",
                    reviewDate = "2022-06-28"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_squalane",
            canonicalName = "Squalane",
            category = "Emollient",
            aliases = listOf("squalane", "phytosqualane", "hydrogenated squalene"),
            description = "Fully saturated, oxidation-stable hydrocarbon emollient derived from sugarcane or olives that mimics natural skin sebum.",
            casNumber = "111-01-3",
            chemicalClass = "Branched Aliphatic Hydrocarbon (C30H62)",
            maxSafeConcentration = "Up to 100% in facial oils and moisturizers",
            dermalBioavailability = "Biomimetic integration into stratum corneum lipid bilayers without systemic hazard",
            toxicologyMechanism = "Hydrogenation of squalene eliminates double bonds, preventing comedogenic squalene peroxide formation under UV exposure.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_sq_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "Evaluated safe up to 100% concentration with non-irritating, non-sensitizing clinical profile.",
                    evidenceStrength = "Strong",
                    conditions = "Safe in all cosmetic categories up to 100%",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2021-01-19"
                )
            )
        ),
        CanonicalIngredient(
            id = "ing_panthenol",
            canonicalName = "Panthenol (Provitamin B5)",
            category = "Humectant / Soothing Active",
            aliases = listOf("panthenol", "d-panthenol", "dexpanthenol", "provitamin b5"),
            description = "Alcohol analog of pantothenic acid (Vitamin B5) that binds water and accelerates epidermal wound repair.",
            casNumber = "81-13-0 / 16485-10-2",
            chemicalClass = "Amide Alcohol / Provitamin B5",
            maxSafeConcentration = "Up to 5.0% in leave-on barrier creams",
            dermalBioavailability = "Readily penetrates epidermis and converts enzymatically to pantothenic acid (Coenzyme A)",
            toxicologyMechanism = "Stimulates fibroblast proliferation and accelerates re-epithelialization in compromised skin barriers.",
            evidenceRecords = listOf(
                EvidenceRecord(
                    id = "ev_pan_cir",
                    organization = "US CIR",
                    findingType = FindingType.BENIGN_EVIDENCE,
                    severity = Severity.INFO,
                    summary = "CIR Expert Panel confirmed panthenol and pantothenic acid are safe as used in cosmetic formulations.",
                    evidenceStrength = "Strong",
                    conditions = "Safe up to 5% in leave-on products",
                    jurisdiction = "GLOBAL",
                    sourceUrl = "https://www.cir-safety.org",
                    reviewDate = "2020-08-11"
                )
            )
        )
    )

    fun findByAlias(rawString: String): CanonicalIngredient? {
        val clean = normalizeString(rawString)
        if (clean.isBlank()) return null

        // 1. Direct match on canonical name or aliases
        val exact = allIngredients.firstOrNull { ing ->
            normalizeString(ing.canonicalName) == clean ||
            ing.aliases.any { normalizeString(it) == clean }
        }
        if (exact != null) return exact

        // 2. Substring or token overlap match (e.g. "contains retinol 0.2%" or "water/aqua")
        return allIngredients.firstOrNull { ing ->
            ing.aliases.any { alias ->
                val normAlias = normalizeString(alias)
                clean == normAlias ||
                (clean.length >= 4 && clean.contains(normAlias) && normAlias.length >= 4) ||
                (normAlias.length >= 4 && normAlias.contains(clean) && clean.length >= 4)
            }
        }
    }

    fun normalizeString(input: String): String {
        return input.lowercase()
            .replace(Regex("""[\(\)\[\]\{\}\*•·\.:;,\-%]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    data class AllergenMatchResult(
        val allergenLabel: String,
        val prefKey: String,
        val severity: Severity,
        val explanation: String
    )

    private val synonymGroups: Map<String, List<String>> = mapOf(
        "fragrance" to listOf("fragrance", "parfum", "perfume", "aroma", "linalool", "limonene", "citronellol", "geraniol", "eugenol", "coumarin", "citral", "benzyl benzoate", "hexyl cinnamal", "lilial", "butylphenyl methylpropional"),
        "sulfate" to listOf("sulfate", "sls", "sles", "sodium lauryl sulfate", "sodium laureth sulfate", "ammonium lauryl sulfate", "sodium coco sulfate", "sodium dodecyl sulfate"),
        "paraben" to listOf("paraben", "methylparaben", "propylparaben", "butylparaben", "ethylparaben", "isobutylparaben"),
        "silicone" to listOf("dimethicone", "silicone", "cyclopentasiloxane", "cyclohexasiloxane", "amodimethicone", "trimethicone", "siloxane"),
        "nut" to listOf("almond", "prunus amygdalus", "peanut", "arachis", "argan", "argania spinosa", "macadamia", "walnut", "juglans", "hazelnut", "cashew", "pistachio", "pecan", "brazil nut", "shea", "butyrospermum parkii"),
        "gluten" to listOf("wheat", "triticum", "gluten", "barley", "hordeum", "rye", "secale", "avena sativa", "oat", "oatmeal", "hydrolyzed wheat"),
        "soy" to listOf("soy", "soja", "glycine soja", "soybean", "lecithin", "tofu"),
        "essential oil" to listOf("essential oil", "lavandula", "lavender", "melaleuca", "tea tree", "eucalyptus", "rosemary", "rosmarinus", "peppermint", "mentha", "citrus", "bergamot", "ylang", "chamomile", "pelargonium"),
        "formaldehyde" to listOf("formaldehyde", "methylene glycol", "formalin", "dmdm hydantoin", "imidazolidinyl urea", "diazolidinyl urea", "quaternium 15", "bronopol", "sodium hydroxymethylglycinate"),
        "MIT" to listOf("methylisothiazolinone", "methylchloroisothiazolinone", "isothiazolinone", "kathon")
    )

    fun tokenMatchesAllergenQuery(rawToken: String, canonical: CanonicalIngredient?, allergenQuery: String): Boolean {
        val normToken = normalizeString(rawToken)
        val normQuery = normalizeString(allergenQuery)
        if (normToken.isBlank() || normQuery.isBlank()) return false

        val candidateStrings = buildList {
            add(normToken)
            if (canonical != null) {
                add(normalizeString(canonical.canonicalName))
                canonical.aliases.forEach { add(normalizeString(it)) }
            }
        }

        // Direct token or substring match
        if (candidateStrings.any { it == normQuery || it.contains(normQuery) || (normQuery.length >= 4 && normQuery.contains(it) && it.length >= 4) }) {
            return true
        }

        // Check synonym groups if the user's allergen query corresponds to a known group
        for ((groupKey, synonyms) in synonymGroups) {
            val queryInGroup = normQuery.contains(groupKey.lowercase()) || synonyms.any { normQuery.contains(it) }
            if (queryInGroup) {
                if (candidateStrings.any { cand -> synonyms.any { syn -> cand.contains(syn) } }) {
                    return true
                }
            }
        }

        return false
    }

    fun matchUserAllergens(
        rawToken: String,
        canonical: CanonicalIngredient?,
        activePreferences: List<com.example.data.local.PreferenceSettingEntity>,
        customAllergens: List<com.example.data.local.CustomAllergenEntity>
    ): List<AllergenMatchResult> {
        val matches = mutableListOf<AllergenMatchResult>()
        val normToken = normalizeString(rawToken)
        val enabledPrefs = activePreferences.filter { it.isEnabled }.associateBy { it.key }

        // 1. Evaluate active custom allergens first (supports HIGH_RISK or WARNING severity)
        for (custom in customAllergens.filter { it.isEnabled }) {
            if (tokenMatchesAllergenQuery(rawToken, canonical, custom.name)) {
                val sev = if (custom.severityLevel.equals("HIGH_RISK", ignoreCase = true)) {
                    Severity.CRITICAL
                } else {
                    Severity.CAUTION
                }
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Personal Allergen: ${custom.name}",
                        prefKey = "custom_${custom.id}",
                        severity = sev,
                        explanation = "Flagged by your personal Allergy Filter for \"${custom.name}\" (${if (sev == Severity.CRITICAL) "High Risk escalation" else "Caution alert"})."
                    )
                )
            }
        }

        // 2. Evaluate standard allergen & dietary preferences on both canonical and raw tokens
        if ("pref_fragrance_free" in enabledPrefs) {
            val isFrag = canonical?.isFragrance == true || tokenMatchesAllergenQuery(rawToken, canonical, "fragrance")
            if (isFrag) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Fragrance / Parfum",
                        prefKey = "pref_fragrance_free",
                        severity = Severity.CAUTION,
                        explanation = "Matches your Fragrance-Free filter. Contains aromatic parfum or volatile fragrance sensitizer."
                    )
                )
            }
        }

        if ("pref_sulfate_free" in enabledPrefs) {
            val isSulf = canonical?.isSulfate == true || tokenMatchesAllergenQuery(rawToken, canonical, "sulfate")
            if (isSulf) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Sulfates (SLS / SLES)",
                        prefKey = "pref_sulfate_free",
                        severity = Severity.CAUTION,
                        explanation = "Matches your Sulfate-Free filter. Contains anionic sulfate surfactant."
                    )
                )
            }
        }

        if ("pref_paraben_free" in enabledPrefs) {
            val isParab = canonical?.isParaben == true || tokenMatchesAllergenQuery(rawToken, canonical, "paraben")
            if (isParab) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Parabens",
                        prefKey = "pref_paraben_free",
                        severity = Severity.CAUTION,
                        explanation = "Matches your Paraben-Free filter. Contains p-hydroxybenzoate ester preservative."
                    )
                )
            }
        }

        if ("pref_silicone_free" in enabledPrefs) {
            val isSil = canonical?.isSilicone == true || tokenMatchesAllergenQuery(rawToken, canonical, "silicone")
            if (isSil) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Silicones",
                        prefKey = "pref_silicone_free",
                        severity = Severity.CAUTION,
                        explanation = "Matches your Silicone-Free filter. Contains siloxane/dimethicone polymer."
                    )
                )
            }
        }

        if ("pref_pregnancy_caution" in enabledPrefs) {
            val isPreg = canonical?.pregnancyCaution == true ||
                normToken.contains("retinol") || normToken.contains("retinoid") ||
                normToken.contains("tretinoin") || normToken.contains("adapalene") ||
                normToken.contains("salicylic") || normToken.contains("hydroquinone") ||
                normToken.contains("oxybenzone") || normToken.contains("lilial")
            if (isPreg) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Pregnancy Caution",
                        prefKey = "pref_pregnancy_caution",
                        severity = Severity.CRITICAL,
                        explanation = "Matches your Pregnancy & Nursing Caution filter. Scientific bodies advise avoiding or limiting exposure."
                    )
                )
            }
        }

        if ("pref_vegan" in enabledPrefs) {
            val isAnimal = canonical?.isAnimalDerived == true ||
                normToken.contains("lanolin") || normToken.contains("carmine") ||
                normToken.contains("beeswax") || normToken.contains("cera alba") ||
                normToken.contains("collagen") || normToken.contains("keratin") ||
                normToken.contains("snail") || normToken.contains("honey")
            if (isAnimal) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Animal-Derived (Non-Vegan)",
                        prefKey = "pref_vegan",
                        severity = Severity.CAUTION,
                        explanation = "Matches your Vegan / Cruelty-Free filter. Derived from animal origin."
                    )
                )
            }
        }

        if ("pref_tree_nuts" in enabledPrefs) {
            val isNut = tokenMatchesAllergenQuery(rawToken, canonical, "nut")
            if (isNut) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Tree Nut / Peanut Allergen",
                        prefKey = "pref_tree_nuts",
                        severity = Severity.CRITICAL,
                        explanation = "Matches your Tree Nut & Peanut Allergen filter. Contains botanical nut/seed protein derivatives."
                    )
                )
            }
        }

        if ("pref_gluten" in enabledPrefs) {
            val isGluten = tokenMatchesAllergenQuery(rawToken, canonical, "gluten")
            if (isGluten) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Gluten / Wheat Derivative",
                        prefKey = "pref_gluten",
                        severity = Severity.CRITICAL,
                        explanation = "Matches your Gluten / Wheat filter. Contains grain protein or cross-contact derivative."
                    )
                )
            }
        }

        if ("pref_essential_oils" in enabledPrefs) {
            val isEssential = tokenMatchesAllergenQuery(rawToken, canonical, "essential oil")
            if (isEssential) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Essential Oil / Terpene",
                        prefKey = "pref_essential_oils",
                        severity = Severity.CAUTION,
                        explanation = "Matches your Essential Oils & Terpenes filter. Contains volatile botanical oil."
                    )
                )
            }
        }

        if ("pref_soy" in enabledPrefs) {
            val isSoy = tokenMatchesAllergenQuery(rawToken, canonical, "soy")
            if (isSoy) {
                matches.add(
                    AllergenMatchResult(
                        allergenLabel = "Soy / Legume Derivative",
                        prefKey = "pref_soy",
                        severity = Severity.CAUTION,
                        explanation = "Matches your Soy & Legume filter. Contains glycine soja or soy derivative."
                    )
                )
            }
        }

        return matches.distinctBy { it.allergenLabel }
    }
}
