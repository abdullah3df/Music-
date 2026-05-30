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
    val background: Color = Color(0xFF131215),
    val surface: Color = Color(0xFF1E1C21),
    val surfaceVariant: Color = Color(0xFF28252C)
) {
    TEAL(
        id = "teal",
        displayNameAr = "النيلي البارد",
        displayNameEn = "Teal Breeze",
        primary = Color(0xFF00ADB5),
        primaryContainer = Color(0xFF0F3438)
    ),
    PURPLE(
        id = "purple",
        displayNameAr = "البنفسجي الإمبراطوري",
        displayNameEn = "Cosmic Purple",
        primary = Color(0xFFBB86FC),
        primaryContainer = Color(0xFF321A4B)
    ),
    GOLD(
        id = "gold",
        displayNameAr = "الذهبي الدافئ",
        displayNameEn = "Sunset Gold",
        primary = Color(0xFFFFF176),
        primaryContainer = Color(0xFF423B17)
    ),
    GREEN(
        id = "green",
        displayNameAr = "الأخضر الزمردي",
        displayNameEn = "Emerald Forest",
        primary = Color(0xFF4CAF50),
        primaryContainer = Color(0xFF163E18)
    ),
    RED(
        id = "red",
        displayNameAr = "الأحمر الياقوتي",
        displayNameEn = "Rose Crimson",
        primary = Color(0xFFE91E63),
        primaryContainer = Color(0xFF491124)
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
            "ar" to "هالة الموسيقى",
            "en" to "AURA MUSIC",
            "fr" to "AURA MUSIQUE",
            "es" to "AURA MÚSICA",
            "de" to "AURA MUSIK"
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
            "ar" to "الدعم والمساعدة",
            "en" to "Support & Contact",
            "fr" to "Support & Aide",
            "es" to "Soporte y Ayuda",
            "de" to "Support & Hilfe"
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
            "ar" to "يسعدنا دائماً سماع رأيك أو تقديم المساعدة في حال واجهت أي مشكلة أثناء استخدام Aura Music! تفضل بالتواصل معنا عبر قنواتنا المخصصة.",
            "en" to "We love to hear from you or help if you face any issues while using Aura Music! Contact us through our direct support channels.",
            "fr" to "Nous serions ravis de vous aider en cas de problème! Contactez notre support direct.",
            "es" to "¡Nos encanta escucharte y ayudarte si tienes problemas! Contáctanos directamente.",
            "de" to "Wir helfen Ihnen gerne bei Problemen! Kontaktieren Sie uns direkt."
        ),
        "support_email" to mapOf(
            "ar" to "البريد الإلكتروني للدعم:",
            "en" to "Support Email:",
            "fr" to "Courriel de support:",
            "es" to "Email de soporte:",
            "de" to "Support-E-Mail:"
        ),
        "support_hours" to mapOf(
            "ar" to "ساعات العمل:",
            "en" to "Working Hours:",
            "fr" to "Heures de travail:",
            "es" to "Horas de atención:",
            "de" to "Arbeitszeit:"
        ),
        "support_hours_val" to mapOf(
            "ar" to "24 ساعة طوال أيام الأسبوع",
            "en" to "24/7 Support Active",
            "fr" to "Soutien actif 24h/24",
            "es" to "Soporte activo 24/7",
            "de" to "24/7 Support aktiv"
        ),
        "support_web" to mapOf(
            "ar" to "الموقع الإلكتروني والتوثيق:",
            "en" to "Website & Documentation:",
            "fr" to "Site web & Documents:",
            "es" to "Sitio web y Documentos:",
            "de" to "Website & Dokumente:"
        ),
        "submit_feedback" to mapOf(
            "ar" to "إرسال رسالة أو ملاحظة",
            "en" to "Send a message / feedback",
            "fr" to "Envoyer un message",
            "es" to "Enviar comentarios / opinión",
            "de" to "Nachricht / Feedback senden"
        ),
        "feedback_placeholder" to mapOf(
            "ar" to "اكتب رسالتك هنا بالتفصيل مضافةً إليها معلومات التواصل الخاصة بك...",
            "en" to "Write your message here in detail with your preferred contact info...",
            "fr" to "Écrivez votre message ici avec vos coordonnées...",
            "es" to "Escriba su mensaje aquí en detalle con su información de contacto...",
            "de" to "Schreiben Sie Ihre Nachricht hier mit Ihren Kontaktdaten..."
        ),
        "feedback_success" to mapOf(
            "ar" to "تم إرسال رسالتك لفريق الدعم بنجاح! سنقوم بالرد عليك قريباً. 💬",
            "en" to "Feedback submitted successfully! We will get back to you shortly. 💬",
            "fr" to "Commentaire envoyé avec succès! Merci. 💬",
            "es" to "¡Comentario enviado correctamente! Gracias. 💬",
            "de" to "Feedback erfolgreich gesendet! Vielen Dank. 💬"
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
        )
    )

    fun get(key: String, lang: String): String {
        val entry = translations[key] ?: return key
        return entry[lang] ?: entry["en"] ?: key
    }
}
