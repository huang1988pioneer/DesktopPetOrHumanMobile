package com.pioneer.desktoppetorhumanmobile;

import java.util.Locale;

final class AppTalk {
    private AppTalk() {
    }

    static String forOpen(String key, String app, String kind) {
        switch (kind) {
            case "browser":
                return "打開「" + app + "」了，一起找點新資料？";
            case "code":
                return "開發時間到，慢慢來，我陪你一起整理。";
            case "office":
                return "「" + app + "」準備好了，記得適時休息。";
            case "chat":
                return "訊息來了，處理完再回來陪我吧。";
            case "music":
                return "播放「" + app + "」，讓心情放鬆一下。";
            case "video":
                return "「" + app + "」開好了，記得休息眼睛喔。";
            case "game":
                return "遊戲時間！玩得開心，也別太晚睡。";
            case "note":
                return "把想法記下來，之後就不怕忘記。";
            default:
                return "你打開了「" + app + "」，我在旁邊陪你。";
        }
    }

    static String forClose(String key, String app, String kind) {
        switch (kind) {
            case "browser":
                return "先把「" + app + "」收起來，眼睛休息一下。";
            case "code":
                return "「" + app + "」先告一段落，記得存檔。";
            case "office":
                return "「" + app + "」先休息，辛苦了。";
            case "chat":
                return "訊息處理好了嗎？喝口水再繼續。";
            case "music":
                return "音樂停了，換個節奏休息一下吧。";
            case "video":
                return "「" + app + "」看完了嗎？站起來動一動吧。";
            case "game":
                return "「" + app + "」結束了，休息一下再玩。";
            case "note":
                return "想法記好了，做得好。";
            default:
                return "你關掉了「" + app + "」，辛苦了。";
        }
    }

    static String kindOf(String packageName) {
        String p = packageName.toLowerCase(Locale.ROOT);
        if (equalsAny(p,
                "com.android.chrome", "com.chrome.beta", "com.sec.android.app.sbrowser",
                "com.duckduckgo.mobile.android", "org.mozilla.firefox", "com.brave.browser",
                "com.microsoft.emmx", "com.opera.browser")
                || has(p, "chrome", "firefox", "sbrowser", "duckduckgo", "brave", "opera")) {
            return "browser";
        }
        if (has(p, "youtube.music")) {
            return "music";
        }
        if (equalsAny(p,
                "com.google.android.youtube", "tv.danmaku.bili", "com.bilibili.app.in",
                "com.ss.android.ugc.aweme", "com.zhiliaoapp.musically", "com.netflix.mediaclient",
                "org.videolan.vlc", "com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.pro",
                "com.google.android.apps.youtube.kids")
                || has(p, "youtube", "netflix", "bilibili", "bili", "mxtech", "vlc", "twitch",
                "iqiyi", "tiktok", "aweme", "douyin")) {
            return "video";
        }
        if (equalsAny(p,
                "com.spotify.music", "com.google.android.apps.youtube.music",
                "com.skysoft.kkbox.android", "com.tencent.qqmusic")
                || has(p, "youtube.music", "spotify", "soundcloud", "kkbox", "joox", "qqmusic")) {
            return "music";
        }
        if (equalsAny(p,
                "jp.naver.line.android", "com.whatsapp", "org.telegram.messenger",
                "com.facebook.orca", "com.discord", "com.tencent.mm", "com.instagram.android",
                "com.twitter.android", "com.facebook.katana")
                || has(p, "line", "whatsapp", "telegram", "messenger", "discord", "wechat",
                "tencent.mm", "kakaotalk", "instagram", "twitter", "facebook")) {
            return "chat";
        }
        if (equalsAny(p, "com.github.android", "com.gitlab.android", "com.termux")
                || has(p, "github", "gitlab", "termux", "aide")) {
            return "code";
        }
        if (equalsAny(p,
                "com.microsoft.office.word", "com.microsoft.office.excel",
                "com.microsoft.office.powerpoint", "cn.wps.moffice_eng", "com.google.android.gm",
                "com.google.android.apps.docs")
                || has(p, "docs", "office", "word", "excel", "powerpoint", "wps", "gmail")) {
            return "office";
        }
        if (equalsAny(p,
                "com.google.android.keep", "com.evernote", "notion.id", "md.obsidian")
                || has(p, "keep", "note", "evernote", "notion", "obsidian")) {
            return "note";
        }
        if (has(p, "game", "roblox", "minecraft", "steam", "genshin", "mlbb", "pubg", "unity")) {
            return "game";
        }
        return "generic";
    }

    private static boolean equalsAny(String packageName, String... exact) {
        for (String candidate : exact) {
            if (packageName.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static boolean has(String packageName, String... needles) {
        for (String needle : needles) {
            if (packageName.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
