China Midnight Floating — ছোট floating clock Android project

ফিচার:
- অন্য অ্যাপের ওপর ছোট floating pill/card দেখা যায়।
- বাংলাদেশ ও চীনের সময়, মিলিসেকেন্ডসহ কাউন্টডাউন।
- স্ক্রিনের যেকোনো জায়গায় drag করা যায়; × চাপলে বন্ধ।
- “START TINY FLOATING CLOCK” চালু করলে floating clock শুরু হয় এবং একই সঙ্গে চীনের স্থানীয় রাত ১২:০০-তে vibration alarm সেট করার চেষ্টা করে।
- চাইলে আলাদা “SET CHINA MIDNIGHT VIBRATION” বোতাম দিয়েও alarm সেট করা যায়।

APK বানাতে:
1. ZIP extract করুন।
2. Android Studio-তে folder খুলুন; Gradle sync শেষ হতে দিন।
3. Build > Build APK(s) চাপুন।
4. APK: app/build/outputs/apk/debug/app-debug.apk

প্রথমবার:
- START TINY FLOATING CLOCK চাপুন।
- Android Settings-এ “Display over other apps” / “Appear on top” permission Allow করুন।
- অ্যাপে ফিরে আবার START চাপুন।
- প্রথমবার floating চালু করার সময় exact alarm permission চাইতে পারে; Settings-এ Alarms & reminders Allow করে অ্যাপে ফিরে আবার Start চাপুন।
- দরকার হলে “SET CHINA MIDNIGHT VIBRATION” বোতাম দিয়েও alarm সেট করুন।
- Android 13+ হলে notification permission Allow করুন।

নোট:
- এটি source project; APK এখানে compile করা হয়নি।
- ফোন বন্ধ, battery dead, DND/vibration off বা OEM background restriction থাকলে alert প্রভাবিত হতে পারে।
- floating clock-এর milliseconds UI refresh 50ms; এটি 1ms precision display নয়। Alarm Android exact alarm API ব্যবহার করে।

GitHub দিয়ে APK build:
1. এই ZIP extract করে সব ফাইল GitHub repository-তে upload করুন (ZIP-কে শুধু একটি ফাইল হিসেবে upload করলে হবে না; আগে extract করতে হবে)।
2. GitHub repository-এর Actions tab খুলুন।
3. “Build Android APK” workflow বেছে Run workflow চাপুন। main/master-এ push করলেও workflow চলবে।
4. Build সবুজ হলে run-এর পেজের নিচে Artifacts থেকে China-Midnight-Floating-APK ZIP download করুন।
5. Artifact ZIP extract করলে app-debug.apk পাবেন।
