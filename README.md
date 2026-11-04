# پروژه اختصاصی اندروید آریانا وی‌پی‌ان (ARIANA VPN)
**طراح و توسعه‌دهنده:** فاطمه زهرا ساعی (Fatemeh Zahra Saei)  
**مجوز:** ۱۰۰٪ رایگان و عام‌المنفعه (Open Access - Zero Fees)

---

## مشخصات فنی پروژه اندروید:
- **معماری هسته:** استفاده مستقیم از `android.net.VpnService` لینوکس جهت ایجاد رابط شبکه مجازی TUN (`/dev/tun`)
- **پروتکل‌های تحت پوشش:** VLESS Reality (با فلو XTLS Vision و استتار TLS 1.3) و WireGuard
- **پشتیبانی سیستم‌عامل:** اندروید ۷.۰ (API 24) تا اندروید ۱۵ (API 35)
- **ابزار بیلد:** Gradle 8.2 + Kotlin 1.9.22 + Java JDK 17

---

## ۳ روش برای ساخت فایل واقعی `app-release.apk`:

### روش ۱: ساخت مستقیم با Android Studio (پیشنهادی)
۱. نرم‌افزار **Android Studio** را باز کنید.
۲. گزینه **Open** را انتخاب کرده و همین پوشه (`android`) را برگزینید.
۳. صبر کنید تا Gradle Sync تکمیل شود.
۴. از منوی بالا گزینه **Build > Build Bundle(s) / APK(s) > Build APK(s)** را بزنید.
۵. فایل `app-release.apk` (یا `app-debug.apk`) در مسیر زیر آماده نصب خواهد بود:
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

---

### روش ۲: ساخت از طریق خط فرمان ترمینال (Terminal / CMD)
پیش‌نیاز: نصب بودن Java JDK 17
```bash
cd android
chmod +x gradlew
./gradlew assembleRelease
```
فایل APK خروجی بلافاصله تولید می‌شود. برای نصب مستقیم روی تلفن همراه با کابل USB:
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

---

### روش ۳: ساخت کاملاً ابری و رایگان با GitHub Actions (بدون نیاز به سیستم قوی)
یک فایل از پیش‌پیکربندی‌شده در مسیر `.github/workflows/build-apk.yml` قرار داده شده است:
۱. این پروژه را در یک مخزن رایگان گیت‌هاب (GitHub Repository) قرار دهید (Push کنید).
۲. تب **Actions** گیت‌هاب به صورت خودکار فعال شده و پروژه را با اوبونتو و Android SDK بیلد می‌کند.
۳. پس از حدود ۹۰ ثانیه، فایل نهایی `app-release.apk` به صورت مستقیم از بخش Artifacts قابل دانلود خواهد بود.
