package com.example.ui.util

/**
 * 8-language support for Purescan App UI and Gemini AI ingredient analysis reports.
 */
enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val badge: String,
    val regionDefault: String
) {
    ENGLISH("en", "English", "English", "EN", "US"),
    SPANISH("es", "Español", "Spanish", "ES", "EU"),
    FRENCH("fr", "Français", "French", "FR", "EU"),
    GERMAN("de", "Deutsch", "German", "DE", "EU"),
    HINDI("hi", "हिन्दी", "Hindi", "HI", "GLOBAL"),
    ARABIC("ar", "العربية", "Arabic", "AR", "GLOBAL"),
    JAPANESE("ja", "日本語", "Japanese", "JA", "JP"),
    PORTUGUESE("pt", "Português", "Portuguese", "PT", "EU");

    val flagEmoji: String
        get() = badge

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

/**
 * Three-tier clinical classification for personal allergen and ingredient rules (P0 UX Readiness).
 * Separates medically diagnosed allergies from personal sensitivities and lifestyle avoidances.
 */
enum class AllergenRuleClassification(
    val id: String,
    val badgeLabel: String,
    val shortLabel: String,
    val description: String,
    val defaultSeverity: String
) {
    MEDICAL_ALLERGY(
        id = "MEDICAL_ALLERGY",
        badgeLabel = "Medical Allergy • Confirmed Diagnosis",
        shortLabel = "Medical Allergy",
        description = "Clinically diagnosed immune/anaphylactic or severe contact allergy (escalates to High Hazard)",
        defaultSeverity = "HIGH_RISK"
    ),
    PERSONAL_SENSITIVITY(
        id = "PERSONAL_SENSITIVITY",
        badgeLabel = "Personal Sensitivity • Skin / Digestive",
        shortLabel = "Sensitivity",
        description = "Causes irritation, breakouts, flushing, or discomfort (flags as Warning)",
        defaultSeverity = "WARNING"
    ),
    PREFERENCE_AVOIDANCE(
        id = "PREFERENCE_AVOIDANCE",
        badgeLabel = "Personal Avoidance • Lifestyle / Clean Rule",
        shortLabel = "Avoidance",
        description = "Ethical, dietary, or formulation preference without acute medical risk",
        defaultSeverity = "WARNING"
    );

    companion object {
        fun fromId(raw: String?): AllergenRuleClassification {
            return entries.firstOrNull { it.id.equals(raw, ignoreCase = true) } ?: PERSONAL_SENSITIVITY
        }
    }
}

/**
 * Explicit ingredient match certainty (P0 UX Readiness).
 * Distinguishes confirmed personal rule matches, possible derivatives, safe detections, and insufficient evidence.
 */
enum class MatchCertainty(
    val label: String,
    val explanation: String
) {
    CONFIRMED_RULE_MATCH(
        label = "Confirmed Match to Your Selected Rule",
        explanation = "Directly matches an active rule in your personal Allergy & Sensitivity Profile."
    ),
    POSSIBLE_DERIVATIVE_MATCH(
        label = "Possible Match / Botanical Derivative",
        explanation = "Related chemical synonym or botanical derivative that may cross-react with your selected rule."
    ),
    DETECTED_NO_RULE_MATCH(
        label = "Ingredient Detected • No Personal Rule Triggered",
        explanation = "Identified in toxicology catalog with no match against your selected personal rules."
    ),
    EVIDENCE_INSUFFICIENT(
        label = "Evidence Insufficient • Unverified Token",
        explanation = "Not matched in the canonical catalog; absence of a match does not prove universal safety."
    )
}

data class LocalizedUiStrings(
    val scanProductTitle: String,
    val scanProductSubtitle: String,
    val howItWorksLink: String,
    val navHome: String,
    val navHistory: String,
    val navScan: String,
    val navSaved: String,
    val navProfile: String,
    val languageLabel: String,
    val allergyFilterTitle: String,
    val trustSummaryHeader: String,
    val nonUniversalSafetyNotice: String
)

object AppLocalization {
    fun stringsFor(language: AppLanguage): LocalizedUiStrings = when (language) {
        AppLanguage.ENGLISH -> LocalizedUiStrings(
            scanProductTitle = "Scan a Product",
            scanProductSubtitle = "Photograph the ingredient list or scan a barcode",
            howItWorksLink = "How Purescan works & what it cannot verify",
            navHome = "Home",
            navHistory = "History",
            navScan = "Scan",
            navSaved = "Saved",
            navProfile = "Profile",
            languageLabel = "App & AI Report Language",
            allergyFilterTitle = "Personal Allergy & Sensitivity Rules",
            trustSummaryHeader = "Clinical Evidence & Personal Rule Check",
            nonUniversalSafetyNotice = "Checked against your selected rules and regulatory records. Absence of a rule match does not guarantee universal safety for every person."
        )
        AppLanguage.SPANISH -> LocalizedUiStrings(
            scanProductTitle = "Escanear Producto",
            scanProductSubtitle = "Fotografía la lista de ingredientes o escanea un código",
            howItWorksLink = "Cómo funciona Purescan y sus límites de verificación",
            navHome = "Inicio",
            navHistory = "Historial",
            navScan = "Escanear",
            navSaved = "Guardados",
            navProfile = "Perfil",
            languageLabel = "Idioma de App e Informes IA",
            allergyFilterTitle = "Reglas de Alergias y Sensibilidad",
            trustSummaryHeader = "Evidencia Clínica y Reglas Personales",
            nonUniversalSafetyNotice = "Verificado según tus reglas seleccionadas. La ausencia de alertas no garantiza seguridad universal."
        )
        AppLanguage.FRENCH -> LocalizedUiStrings(
            scanProductTitle = "Scanner un Produit",
            scanProductSubtitle = "Photographiez la liste d'ingrédients ou un code-barres",
            howItWorksLink = "Comment fonctionne Purescan et ses limites",
            navHome = "Accueil",
            navHistory = "Historique",
            navScan = "Scanner",
            navSaved = "Favoris",
            navProfile = "Profil",
            languageLabel = "Langue de l'App et des Rapports IA",
            allergyFilterTitle = "Règles d'Allergies et Sensibilités",
            trustSummaryHeader = "Preuves Cliniques et Règles Personnelles",
            nonUniversalSafetyNotice = "Vérifié selon vos règles personnelles. L'absence d'alerte ne garantit pas une innocuité universelle."
        )
        AppLanguage.GERMAN -> LocalizedUiStrings(
            scanProductTitle = "Produkt Scannen",
            scanProductSubtitle = "Zutatenliste fotografieren oder Barcode scannen",
            howItWorksLink = "Wie Purescan funktioniert & Prüfgrenzen",
            navHome = "Start",
            navHistory = "Verlauf",
            navScan = "Scannen",
            navSaved = "Merkliste",
            navProfile = "Profil",
            languageLabel = "Sprache für App & KI-Berichte",
            allergyFilterTitle = "Allergie- & Empfindlichkeitsregeln",
            trustSummaryHeader = "Evidenzprüfung & Persönliche Regeln",
            nonUniversalSafetyNotice = "Geprüft anhand Ihrer gewählten Regeln. Kein Treffer bedeutet keine universelle Sicherheitsgarantie."
        )
        AppLanguage.HINDI -> LocalizedUiStrings(
            scanProductTitle = "उत्पाद स्कैन करें",
            scanProductSubtitle = "सामग्री सूची की फोटो लें या बारकोड स्कैन करें",
            howItWorksLink = "Purescan कैसे काम करता है और इसकी सीमाएँ",
            navHome = "होम",
            navHistory = "इतिहास",
            navScan = "स्कैन",
            navSaved = "सेव किए गए",
            navProfile = "प्रोफ़ाइल",
            languageLabel = "ऐप और AI रिपोर्ट भाषा",
            allergyFilterTitle = "व्यक्तिगत एलर्जी और संवेदनशीलता नियम",
            trustSummaryHeader = "वैज्ञानिक प्रमाण और व्यक्तिगत नियम जाँच",
            nonUniversalSafetyNotice = "आपके चयनित नियमों के विरुद्ध जाँचा गया। किसी नियम का न मिलना सार्वभौमिक सुरक्षा की गारंटी नहीं है।"
        )
        AppLanguage.ARABIC -> LocalizedUiStrings(
            scanProductTitle = "مسح منتج",
            scanProductSubtitle = "صوّر قائمة المكونات أو امسح الرمز الشريطي",
            howItWorksLink = "كيف يعمل Purescan وما لا يمكنه التحقق منه",
            navHome = "الرئيسية",
            navHistory = "السجل",
            navScan = "مسح",
            navSaved = "المحفوظات",
            navProfile = "الملف",
            languageLabel = "لغة التطبيق وتقارير الذكاء الاصطناعي",
            allergyFilterTitle = "قواعد الحساسية الشخصية والمكونات",
            trustSummaryHeader = "الأدلة العلمية وفحص القواعد الشخصية",
            nonUniversalSafetyNotice = "تم الفحص وفق قواعدك المحددة. عدم وجود تطابق لا يعني ضمان الأمان المطلق لكل شخص."
        )
        AppLanguage.JAPANESE -> LocalizedUiStrings(
            scanProductTitle = "製品をスキャン",
            scanProductSubtitle = "成分表を撮影するかバーコードをスキャン",
            howItWorksLink = "Purescanの仕組みと検証の限界について",
            navHome = "ホーム",
            navHistory = "履歴",
            navScan = "スキャン",
            navSaved = "保存済み",
            navProfile = "プロフィール",
            languageLabel = "アプリとAIレポートの言語",
            allergyFilterTitle = "個人のアレルギー・敏感肌ルール",
            trustSummaryHeader = "科学的根拠と個人ルールの照合",
            nonUniversalSafetyNotice = "選択されたルールに基づき照合しました。該当なしでもすべての人への絶対的な安全を保証するものではありません。"
        )
        AppLanguage.PORTUGUESE -> LocalizedUiStrings(
            scanProductTitle = "Escanear Produto",
            scanProductSubtitle = "Fotografe a lista de ingredientes ou código de barras",
            howItWorksLink = "Como o Purescan funciona e limites de verificação",
            navHome = "Início",
            navHistory = "Histórico",
            navScan = "Escanear",
            navSaved = "Salvos",
            navProfile = "Perfil",
            languageLabel = "Idioma do App e Relatórios IA",
            allergyFilterTitle = "Regras de Alergia e Sensibilidade",
            trustSummaryHeader = "Evidência Clínica e Regras Pessoais",
            nonUniversalSafetyNotice = "Verificado com suas regras seleccionadas. A ausência de alertas não garante segurança universal."
        )
    }

    fun scanProductTitle(language: AppLanguage): String = stringsFor(language).scanProductTitle
    fun scanProductSubtitle(language: AppLanguage): String = stringsFor(language).scanProductSubtitle
    fun howItWorksLink(language: AppLanguage): String = stringsFor(language).howItWorksLink
    fun historyTab(language: AppLanguage): String = stringsFor(language).navHistory
    fun savedTab(language: AppLanguage): String = stringsFor(language).navSaved
    fun profileTab(language: AppLanguage): String = stringsFor(language).navProfile
    fun languageSelectorTitle(language: AppLanguage): String = stringsFor(language).languageLabel

    fun searchPlaceholder(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Search ingredients, allergens, or products..."
        AppLanguage.SPANISH -> "Buscar ingredientes, alérgenos o productos..."
        AppLanguage.FRENCH -> "Rechercher des ingrédients, allergènes ou produits..."
        AppLanguage.GERMAN -> "Inhaltsstoffe, Allergene oder Produkte suchen..."
        AppLanguage.HINDI -> "सामग्री, एलर्जेन या उत्पाद खोजें..."
        AppLanguage.ARABIC -> "ابحث عن المكونات أو مسببات الحساسية أو المنتجات..."
        AppLanguage.JAPANESE -> "成分、アレルゲン、製品を検索..."
        AppLanguage.PORTUGUESE -> "Pesquisar ingredientes, alérgenos ou produtos..."
    }

    fun safetyTipsLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Safety Tips"
        AppLanguage.SPANISH -> "Consejos"
        AppLanguage.FRENCH -> "Conseils"
        AppLanguage.GERMAN -> "Sicherheit"
        AppLanguage.HINDI -> "सुरक्षा टिप्स"
        AppLanguage.ARABIC -> "نصائح الأمان"
        AppLanguage.JAPANESE -> "安全ヒント"
        AppLanguage.PORTUGUESE -> "Dicas"
    }

    fun settingsLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Allergen Rules"
        AppLanguage.SPANISH -> "Reglas Alergia"
        AppLanguage.FRENCH -> "Règles Allergie"
        AppLanguage.GERMAN -> "Allergieregeln"
        AppLanguage.HINDI -> "एलर्जी नियम"
        AppLanguage.ARABIC -> "قواعد الحساسية"
        AppLanguage.JAPANESE -> "アレルゲン設定"
        AppLanguage.PORTUGUESE -> "Regras Alergia"
    }

    fun authValueProposition(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Evidence-backed ingredient & allergen verification tailored to your personal rules."
        AppLanguage.SPANISH -> "Verificación de ingredientes y alérgenos basada en evidencia según tus reglas personales."
        AppLanguage.FRENCH -> "Vérification des ingrédients et allergènes fondée sur des preuves selon vos règles."
        AppLanguage.GERMAN -> "Evidenzbasierte Inhaltsstoff- und Allergenprüfung nach Ihren persönlichen Regeln."
        AppLanguage.HINDI -> "आपके व्यक्तिगत नियमों के अनुसार प्रमाण-आधारित सामग्री और एलर्जी की जाँच।"
        AppLanguage.ARABIC -> "تحقق مدعوم بالأدلة من المكونات ومسببات الحساسية وفق قواعدك الشخصية."
        AppLanguage.JAPANESE -> "個人のルールに基づいたエビデンス重視の成分・アレルゲン検証。"
        AppLanguage.PORTUGUESE -> "Verificação de ingredientes e alérgenos baseada em evidências para suas regras."
    }

    fun signInLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Sign In"
        AppLanguage.SPANISH -> "Iniciar Sesión"
        AppLanguage.FRENCH -> "Connexion"
        AppLanguage.GERMAN -> "Anmelden"
        AppLanguage.HINDI -> "साइन इन"
        AppLanguage.ARABIC -> "تسجيل الدخول"
        AppLanguage.JAPANESE -> "サインイン"
        AppLanguage.PORTUGUESE -> "Entrar"
    }

    fun createAccountLabel(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Create Account"
        AppLanguage.SPANISH -> "Crear Cuenta"
        AppLanguage.FRENCH -> "Créer un Compte"
        AppLanguage.GERMAN -> "Konto Erstellen"
        AppLanguage.HINDI -> "खाता बनाएँ"
        AppLanguage.ARABIC -> "إنشاء حساب"
        AppLanguage.JAPANESE -> "アカウント作成"
        AppLanguage.PORTUGUESE -> "Criar Conta"
    }

    fun oneTapProfileDescription(language: AppLanguage): String = when (language) {
        AppLanguage.ENGLISH -> "Restores your local Room-saved allergy rules & scan history on this device without sharing data externally."
        AppLanguage.SPANISH -> "Restaura tus reglas de alergia e historial guardados localmente en este dispositivo."
        AppLanguage.FRENCH -> "Restaure vos règles d'allergie et votre historique enregistrés localement sur cet appareil."
        AppLanguage.GERMAN -> "Stellt Ihre lokal gespeicherten Allergieregeln und den Scanverlauf auf diesem Gerät wieder her."
        AppLanguage.HINDI -> "इस डिवाइस पर स्थानीय रूप से सहेजे गए एलर्जी नियम और स्कैन इतिहास को पुनर्स्थापित करता है।"
        AppLanguage.ARABIC -> "يستعيد قواعد الحساسية وسجل المسح المحفوظة محليًا على هذا الجهاز."
        AppLanguage.JAPANESE -> "この端末にローカル保存されたアレルゲンルールとスキャン履歴を復元します。"
        AppLanguage.PORTUGUESE -> "Restaura suas regras de alergia e histórico salvos localmente neste dispositivo."
    }
}
