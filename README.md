# 🁣 تطبيق دومينو - Domino App

تطبيق لعبة دومينو لنظام Android مبني بـ Kotlin + Jetpack Compose يتبع Clean Architecture.

## البنية المعمارية

```
Presentation Layer  →  ViewModels + Screens + Navigation
Domain Layer        →  Models + UseCases + Repository Interfaces  
Data Layer          →  Repository Impls + Engines (Game + AI)
DI Layer            →  Hilt Modules
```

## المميزات
- 🎮 لعب ضد AI بثلاثة مستويات (سهل / متوسط / صعب)
- 🌐 لعب شبكي عبر WiFi Direct
- 📊 إحصائيات وإنجازات
- ⚙️ إعدادات قابلة للتخصيص
- 🔗 Deep Links لجميع الشاشات

## متطلبات التشغيل
- Android Studio Hedgehog أو أحدث
- Android SDK 26+
- Kotlin 2.0+

## تشغيل المشروع
```bash
# استنساخ المشروع
git clone https://github.com/Almnjshy/domino.git

# فتح في Android Studio وتشغيل المزامنة Gradle
# ثم Run ← app
```

## تشغيل الاختبارات
```bash
./gradlew test                  # Unit tests
./gradlew connectedAndroidTest  # UI tests
```

## البنية التفصيلية للملفات
انظر `PROJECT_SUMMARY.md`
