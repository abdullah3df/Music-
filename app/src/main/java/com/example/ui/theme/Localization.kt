package com.example.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    AR("ar", "العربية", "العربية"),
    EN("en", "English", "English"),
    FR("fr", "French", "Français"),
    ES("es", "Spanish", "Español"),
    DE("de", "German", "Deutsch")
}

enum class AppThemeColor(
    val id: String,
    val displayNameAr: String,
    val displayNameEn: String,
    val primary: Color,
    val primaryContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color
) {
    TEAL(
        id = "teal",
        displayNameAr = "الفيروزي الفاخر",
        displayNameEn = "Luxury Turquoise",
        primary = Color(0xFF00E5FF),
        primaryContainer = Color(0xFF00363D),
        background = Color(0xFF070F12),
        surface = Color(0xFF0F1B20),
        surfaceVariant = Color(0xFF182930)
    ),
    PURPLE(
        id = "purple",
        displayNameAr = "البنفسجي الإمبراطوري",
        displayNameEn = "Imperial Amethyst",
        primary = Color(0xFFD6A2E8),
        primaryContainer = Color(0xFF3B1E4E),
        background = Color(0xFF09070F),
        surface = Color(0xFF110E1A),
        surfaceVariant = Color(0xFF191526)
    ),
    GOLD(
        id = "gold",
        displayNameAr = "الذهبي الشمباني",
        displayNameEn = "Satin Champagne",
        primary = Color(0xFFE5B869),
        primaryContainer = Color(0xFF453018),
        background = Color(0xFF0D0B07),
        surface = Color(0xFF17130D),
        surfaceVariant = Color(0xFF221D14)
    ),
    GREEN(
        id = "green",
        displayNameAr = "اليشم الإمبراطوري",
        displayNameEn = "Imperial Jade",
        primary = Color(0xFF5ED3A2),
        primaryContainer = Color(0xFF113825),
        background = Color(0xFF070B09),
        surface = Color(0xFF0F1713),
        surfaceVariant = Color(0xFF16231C)
    ),
    RED(
        id = "red",
        displayNameAr = "الوردي المخملي الفاخر",
        displayNameEn = "Velvet Rose",
        primary = Color(0xFFFA4B6E),
        primaryContainer = Color(0xFF4D101C),
        background = Color(0xFF0B0506),
        surface = Color(0xFF170D0E),
        surfaceVariant = Color(0xFF231416)
    );

    companion object {
        fun fromId(id: String): AppThemeColor {
            return values().firstOrNull { it.id == id } ?: PURPLE
        }
    }
}

object Localization {
    private val translations = mapOf(
        "app_title" to mapOf(
            "ar" to "AURA MUSIC",
            "en" to "AURA MUSIC",
            "fr" to "AURA MUSIC",
            "es" to "AURA MUSIC",
            "de" to "AURA MUSIC"
        ),
        "tab_sounds" to mapOf(
            "ar" to "الأصوات",
            "en" to "All Sounds",
            "fr" to "Sons",
            "es" to "Sonidos",
            "de" to "Töne"
        ),
        "tab_radio" to mapOf(
            "ar" to "الراديو",
            "en" to "Radio",
            "fr" to "Radio",
            "es" to "Radio",
            "de" to "Radio"
        ),
        "tab_playlists" to mapOf(
            "ar" to "القوائم",
            "en" to "Playlists",
            "fr" to "Listes",
            "es" to "Listas",
            "de" to "Playlists"
        ),
        "tab_favorites" to mapOf(
            "ar" to "المفضلة",
            "en" to "Favorites",
            "fr" to "Favoris",
            "es" to "Favoritos",
            "de" to "Favoriten"
        ),
        "settings" to mapOf(
            "ar" to "الضبط والتخصيص",
            "en" to "Settings & Options",
            "fr" to "Paramètres & Options",
            "es" to "Configuración",
            "de" to "Optionen"
        ),
        "support" to mapOf(
            "ar" to "إرسال الاقتراحات",
            "en" to "Send Suggestions",
            "fr" to "Send Suggestions",
            "es" to "Send Suggestions",
            "de" to "Send Suggestions"
        ),
        "app_language" to mapOf(
            "ar" to "لغة التطبيق",
            "en" to "App Language",
            "fr" to "Langue de l'application",
            "es" to "Idioma de la aplicación",
            "de" to "App-Sprache"
        ),
        "app_theme" to mapOf(
            "ar" to "مظهر ولون التطبيق",
            "en" to "App Visual Theme",
            "fr" to "Thème visuel",
            "es" to "Tema de la aplicación",
            "de" to "Visuelles App-Design"
        ),
        "support_desc" to mapOf(
            "ar" to "يسعدنا دائماً تلقي اقتراحاتكم وملاحظاتكم القيمة لتطوير وإثراء تجربة Aura Music! تفضلوا بمراسلتنا مباشرة عبر البريد الإلكتروني.",
            "en" to "We love hearing your valuable suggestions and feedback to enhance your Aura Music experience! Reach out to us via email.",
            "fr" to "We love hearing your valuable suggestions and feedback to enhance your Aura Music experience! Reach out to us via email.",
            "es" to "We love hearing your valuable suggestions and feedback to enhance your Aura Music experience! Reach out to us via email.",
            "de" to "We love hearing your valuable suggestions and feedback to enhance your Aura Music experience! Reach out to us via email."
        ),
        "support_email" to mapOf(
            "ar" to "بريد الاقتراحات:",
            "en" to "Suggestions Email:",
            "fr" to "Suggestions Email:",
            "es" to "Suggestions Email:",
            "de" to "Suggestions Email:"
        ),

        "toast_scanned" to mapOf(
            "ar" to "تم العثور على %s من الملفات الصوتية الجديدة بالجهاز ومزامنتها بنجاح!",
            "en" to "Scanned & successfully synchronized %s new audio files from your device!",
            "fr" to "%s nouveaux fichiers audio trouvés et synchronisés!",
            "es" to "¡Se encontraron y sincronizaron %s nuevos archivos de audio!",
            "de" to "%s neue Audiodateien gefunden und erfolgreich synchronisiert!"
        ),
        "toast_up_to_date" to mapOf(
            "ar" to "تم فحص الملفات! المكتبة مطابقة لملفات جهازك بالفعل.",
            "en" to "Scan complete! Music library is already up to date.",
            "fr" to "Numérisation terminée! La bibliothèque est déjà à jour.",
            "es" to "¡Escaneo completado! La biblioteca ya está actualizada.",
            "de" to "Musikbibliothek ist bereits auf dem neuesten Stand."
        ),
        "toast_error_scan" to mapOf(
            "ar" to "حدث خطأ أثناء فحص ملفات جهازك الصوتية",
            "en" to "An error occurred while scanning your device audio files",
            "fr" to "Une erreur est survenue lors de la numérisation",
            "es" to "Ocurrió un error al escanear los archivos de audio",
            "de" to "Fehler beim Scannen der Audiodateien Ihres Geräts"
        ),
        "no_sounds_title" to mapOf(
            "ar" to "لا توجد أصوات مضافة",
            "en" to "No audio tracks added",
            "fr" to "Aucun son ajouté",
            "es" to "No hay pistas agregadas",
            "de" to "Keine Audiodateien"
        ),
        "no_sounds_desc" to mapOf(
            "ar" to "لم يتم العثور على أي ملفات صوتية بعد. اضغط على الزر أدناه لمسح ومزامنة جهازك بالكامل تلقائياً.",
            "en" to "No audio files were found. Tap below to search for audio files across folders and downloads automatically.",
            "fr" to "Aucun fichier trouvé. Appuyez ci-dessous pour lancer une analyse.",
            "es" to "No se encontraron archivos. Toque abajo para buscar archivos automáticamente.",
            "de" to "Keine Musikdateien gefunden. Tippen Sie unten, um das Gerät automatisch zu scannen."
        ),
        "btn_grant" to mapOf(
            "ar" to "السماح بالوصول والفحص التلقائي",
            "en" to "Grant Permission & Auto Scan",
            "fr" to "Autoriser l'accès & Scanner",
            "es" to "Permitir Acceso y Escanear",
            "de" to "Zugriff zulassen & Scannen"
        ),
        "btn_auto_scan" to mapOf(
            "ar" to "البدء بالفحص والمزامنة التلقائية",
            "en" to "Start Auto Scanning & Sync",
            "fr" to "Démarrer l'analyse automatique",
            "es" to "Iniciar Escaneo Automático",
            "de" to "Automatischen Scan starten"
        ),
        "radio_no_stations" to mapOf(
            "ar" to "لا توجد محطات راديو مضافة",
            "en" to "No radio stations added",
            "fr" to "Aucune station de radio ajoutée",
            "es" to "No hay estaciones de radio",
            "de" to "Keine Radiosender"
        ),
        "radio_desc_empty" to mapOf(
            "ar" to "اضغط على زر الإضافة لإنشاء محطة راديو مخصصة خاصة بك.",
            "en" to "Tap the add button to create your own custom streaming radio station.",
            "fr" to "Appuyez sur le bouton pour ajouter votre station de radio personnalisée.",
            "es" to "Toque el botón para crear su propia estación de radio personalizada.",
            "de" to "Tippen Sie auf die Schaltfläche, um einen Radiosender hinzuzufügen."
        ),
        "playlist_no_playlists" to mapOf(
            "ar" to "لا توجد قوائم تشغيل مضافة",
            "en" to "No playlists created",
            "fr" to "Aucune playlist créée",
            "es" to "No hay listas creadas",
            "de" to "Keine Playlists erstellt"
        ),
        "playlist_desc_empty" to mapOf(
            "ar" to "اضغط على زر الإضافة لإنشاء قائمة تشغيل لترتيب مذكراتك وأصواتك.",
            "en" to "Tap the add button to form a playlist and organize your recordings or audios.",
            "fr" to "Appuyez sur le bouton '+' pour organiser vos fichiers.",
            "es" to "Toque el botón para organizar sus grabaciones.",
            "de" to "Tippen Sie auf Plus, um Playlists zu erstellen."
        ),
        "playlist_create" to mapOf(
            "ar" to "إنشاء قائمة تشغيل جديدة",
            "en" to "Create New Playlist",
            "fr" to "Créer une Playlist",
            "es" to "Crear Nueva Lista",
            "de" to "Neue Playlist"
        ),
        "add_radio" to mapOf(
            "ar" to "أضف راديو جديد",
            "en" to "Add New Radio",
            "fr" to "Ajouter une Radio",
            "es" to "Agregar Radio",
            "de" to "Radio hinzufügen"
        ),
        "favorites_empty" to mapOf(
            "ar" to "المفضلة فارغة",
            "en" to "Favorites is empty",
            "fr" to "Favoris vide",
            "es" to "Favoritos vacío",
            "de" to "Favoriten leer"
        ),
        "favorites_desc" to mapOf(
            "ar" to "اضغط على أيقونة القلب في أي ملف صوتي لإضافته هنا للوصول السريع.",
            "en" to "Tap the heart icon on any audio file to add it here for quick access.",
            "fr" to "Appuyez sur l'icône de cœur pour ajouter ici.",
            "es" to "Toque el corazón para agregarlo aquí.",
            "de" to "Tippen Sie auf das Herz, um Titel hier hinzuzufügen."
        ),
        "scan_card_title_scanning" to mapOf(
            "ar" to "جاري البحث والمزامنة الآلية...",
            "en" to "Auto-scanning and syncing...",
            "fr" to "Numérisation et synchronisation...",
            "es" to "Escaneando y sincronizando...",
            "de" to "Musik wird gesucht und synchronisiert..."
        ),
        "scan_card_title_idle" to mapOf(
            "ar" to "مسح ومزامنة جهازك تلقائياً",
            "en" to "Scan & Sync device automatically",
            "fr" to "Scanner & synchroniser",
            "es" to "Escanear y sincronizar",
            "de" to "Gerät scannen & synchronisieren"
        ),
        "scan_card_desc_scanning" to mapOf(
            "ar" to "فضلاً انتظر لعدة ثوانٍ حتى ننتهي من فحص وتحديث مكتبتك الصوتية من الذاكرة...",
            "en" to "Please wait a few seconds while we scan and update your audio library from memory...",
            "fr" to "Veuillez patienter quelques secondes pendant la mise à jour...",
            "es" to "Espere unos segundos mientras finaliza la sincronización...",
            "de" to "Bitte warten Sie einige Sekunden..."
        ),
        "scan_card_desc_idle" to mapOf(
            "ar" to "اضغط هنا للبحث الآلي في التحميلات والمجلدات وتحديث كافة ملفاتك الصوتية.",
            "en" to "Tap to automatically scan downloads and folders to refresh your entire audio library.",
            "fr" to "Appuyez pour analyser automatiquement vos dossiers et rafraîchir la bibliothèque.",
            "es" to "Presione para escanear descargas y carpetas para actualizar su biblioteca.",
            "de" to "Tippen Sie hier, um Downloads und Ordner nach Audiodateien zu scannen."
        ),
        "support_form_title" to mapOf(
            "ar" to "أرسل رسالة للدعم الفني مباشرة",
            "en" to "Send a Direct Support Ticket",
            "fr" to "Envoyer un Ticket de Support",
            "es" to "Enviar Mensaje de Soporte",
            "de" to "Support-Nachricht senden"
        ),
        "autoplay_title" to mapOf(
            "ar" to "التشغيل التلقائي للتالي",
            "en" to "Autoplay Next Track",
            "fr" to "Lecture automatique",
            "es" to "Reproducción automática",
            "de" to "Autoplay"
        ),
        "autoplay_desc" to mapOf(
            "ar" to "تشغيل المقطع التالي تلقائياً فور انتهاء الملف الحالي بدون توقف.",
            "en" to "Plays the next audio track automatically when current finishes.",
            "fr" to "Lit la piste suivante automatiquement.",
            "es" to "Reproduce la siguiente pista automáticamente.",
            "de" to "Spielt den nächsten Titel automatisch ab."
        ),
        "equalizer_title" to mapOf(
            "ar" to "محسن الصوت الرقمي Aura DSP",
            "en" to "Aura DSP Audio Engager",
            "fr" to "Amélioration audio DSP",
            "es" to "Mejora de audio DSP",
            "de" to "DSP Audio-Verbesserung"
        ),
        "equalizer_desc" to mapOf(
            "ar" to "تنشيط فلاتر تنقية صوتية ذكية لزيادة الوضوح والبيس.",
            "en" to "Enable advanced digital filters for clarity and rich bass.",
            "fr" to "Active les filtres DSP pour la clarté et les basses.",
            "es" to "Activa filtros de audio para mayor claridad.",
            "de" to "Aktiviert Audiofilter für mehr Klarheit."
        ),
        "haptic_title" to mapOf(
            "ar" to "الاستجابة اللمسية الفاخرة",
            "en" to "Premium Haptic Feedback",
            "fr" to "Retour haptique luxueux",
            "es" to "Respuesta háptica premium",
            "de" to "Premium haptisches Feedback"
        ),
        "haptic_desc" to mapOf(
            "ar" to "اهتزازات لمسية خفيفة تمنح شعوراً رائعاً واحترافياً عند الضغط.",
            "en" to "Subtle haptic vibrations on control buttons for polished feel.",
            "fr" to "Vibrations haptiques subtiles lors de l'appui.",
            "es" to "Sutiles vibraciones táctiles al presionar.",
            "de" to "Subtile haptische Rückmeldung beim Drücken."
        )
    )

    fun get(key: String, lang: String): String {
        val entry = translations[key] ?: return key
        return entry[lang] ?: entry["en"] ?: key
    }
}
