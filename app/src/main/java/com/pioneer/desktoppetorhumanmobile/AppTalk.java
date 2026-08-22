package com.pioneer.desktoppetorhumanmobile;

final class AppTalk {
    private AppTalk() {
    }

    static String forOpen(String key, String app, String kind) {
        switch (kind) {
            case "browser":
                return openBrowser(key, app);
            case "code":
                return openCode(key, app);
            case "office":
                return openOffice(key, app);
            case "chat":
                return openChat(key, app);
            case "music":
                return openMusic(key, app);
            case "video":
                return openVideo(key, app);
            case "game":
                return openGame(key, app);
            case "note":
                return openNote(key, app);
            default:
                return openGeneric(key, app);
        }
    }

    static String forClose(String key, String app, String kind) {
        switch (kind) {
            case "browser":
                return closeBrowser(key, app);
            case "code":
                return closeCode(key, app);
            case "office":
                return closeOffice(key, app);
            case "chat":
                return closeChat(key, app);
            case "music":
                return closeMusic(key, app);
            case "video":
                return closeVideo(key, app);
            case "game":
                return closeGame(key, app);
            case "note":
                return closeNote(key, app);
            default:
                return closeGeneric(key, app);
        }
    }

    static String kindOf(String packageName) {
        String p = packageName.toLowerCase();
        if (equalsAny(p,
                "com.android.chrome", "com.chrome.beta", "com.sec.android.app.sbrowser",
                "com.duckduckgo.mobile.android", "org.mozilla.firefox", "com.brave.browser",
                "com.microsoft.emmx", "com.opera.browser")) {
            return "browser";
        }
        if (equalsAny(p,
                "com.google.android.youtube", "tv.danmaku.bili", "com.bilibili.app.in",
                "com.ss.android.ugc.aweme", "com.zhiliaoapp.musically", "com.netflix.mediaclient",
                "org.videolan.vlc", "com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.pro",
                "com.google.android.apps.youtube.kids")) {
            return "video";
        }
        if (equalsAny(p,
                "com.spotify.music", "com.google.android.apps.youtube.music",
                "com.skysoft.kkbox.android", "com.tencent.qqmusic")) {
            return "music";
        }
        if (equalsAny(p,
                "jp.naver.line.android", "com.whatsapp", "org.telegram.messenger",
                "com.facebook.orca", "com.discord", "com.tencent.mm", "com.instagram.android",
                "com.twitter.android", "com.facebook.katana")) {
            return "chat";
        }
        if (equalsAny(p, "com.github.android", "com.gitlab.android", "com.termux")) {
            return "code";
        }
        if (equalsAny(p,
                "com.microsoft.office.word", "com.microsoft.office.excel",
                "com.microsoft.office.powerpoint", "cn.wps.moffice_eng", "com.google.android.gm",
                "com.google.android.apps.docs")) {
            return "office";
        }
        if (equalsAny(p,
                "com.google.android.keep", "com.evernote", "notion.id", "md.obsidian")) {
            return "note";
        }
        if (has(p, "chrome", "firefox", "sbrowser", "duckduckgo", "brave", "opera")) {
            return "browser";
        }
        if (has(p, "youtube.music")) {
            return "music";
        }
        if (has(p, "youtube", "netflix", "bilibili", "bili", "mxtech", "vlc", "twitch", "iqiyi",
                "tiktok", "aweme", "douyin")) {
            return "video";
        }
        if (has(p, "spotify", "soundcloud", "kkbox", "joox", "qqmusic")) {
            return "music";
        }
        if (has(p, "line", "whatsapp", "telegram", "messenger", "discord", "wechat", "tencent.mm",
                "kakaotalk", "instagram", "twitter", "facebook")) {
            return "chat";
        }
        if (has(p, "github", "gitlab", "termux", "aide")) {
            return "code";
        }
        if (has(p, "docs", "office", "word", "excel", "powerpoint", "wps", "gmail")) {
            return "office";
        }
        if (has(p, "keep", "note", "evernote", "notion", "obsidian")) {
            return "note";
        }
        if (has(p, "game", "roblox", "minecraft", "steam", "genshin", "mlbb", "pubg", "unity")) {
            return "game";
        }
        return "generic";
    }

    private static boolean equalsAny(String packageName, String... exact) {
        for (int i = 0; i < exact.length; i++) {
            if (packageName.equals(exact[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean has(String packageName, String... needles) {
        for (int i = 0; i < needles.length; i++) {
            if (packageName.contains(needles[i])) {
                return true;
            }
        }
        return false;
    }

    private static String openBrowser(String key, String app) {
        if ("miaobaibai".equals(key)) {
            return "喵～打開" + app + "了";
        }
        if ("miaobubu".equals(key)) {
            return "布布看到" + app + "！要看什麼";
        }
        if ("xiaotu".equals(key)) {
            return "喔！" + app + "開了，衝上網";
        }
        if ("laowangmao".equals(key)) {
            return "上網也要招財，" + app + "開好了";
        }
        if ("fengxiong".equals(key)) {
            return app + "開了，先把正事查完";
        }
        if ("fengge".equals(key)) {
            return app + "就緒。別只顧著滑";
        }
        if ("xiaoying".equals(key)) {
            return app + "…先回郵件再看吧";
        }
        if ("miaoniang".equals(key)) {
            return "喵娘陪你逛" + app + "～";
        }
        if ("tuge".equals(key)) {
            return app + "開了！今天查什麼";
        }
        if ("yamei".equals(key)) {
            return "你開了" + app + "，慢慢看喔";
        }
        if ("yumei".equals(key)) {
            return "游進" + app + "看看～";
        }
        if ("gugugaga".equals(key)) {
            return "ググ！" + app + "打開了";
        }
        return "你打開了" + app;
    }

    private static String openCode(String key, String app) {
        if ("fengge".equals(key)) {
            return app + "就緒，開始幹活";
        }
        if ("fengxiong".equals(key)) {
            return app + "開了，慢慢寫別爆肝";
        }
        if ("gugugaga".equals(key)) {
            return "ガガ，開始寫 code 了";
        }
        if ("laowangmao".equals(key)) {
            return "寫出會賺錢的程式！";
        }
        if ("miaobaibai".equals(key)) {
            return "白白陪你寫程式喔";
        }
        if ("miaobubu".equals(key)) {
            return "布布看你敲鍵盤！";
        }
        if ("miaoniang".equals(key)) {
            return "喵娘當你的程式吉祥物";
        }
        if ("tuge".equals(key)) {
            return app + "！今天也要一次過";
        }
        if ("xiaotu".equals(key)) {
            return app + "登場，來寫扣！";
        }
        if ("xiaoying".equals(key)) {
            return app + "又開了…今天也加班嗎";
        }
        if ("yamei".equals(key)) {
            return "寫程式記得休息喔";
        }
        if ("yumei".equals(key)) {
            return "程式像海浪，一波一波寫";
        }
        return "你打開了" + app;
    }

    private static String openOffice(String key, String app) {
        if ("fengge".equals(key)) {
            return app + "就緒。把報告收尾";
        }
        if ("fengxiong".equals(key)) {
            return app + "開了，一份一份來";
        }
        if ("gugugaga".equals(key)) {
            return "ググ，" + app + "辦公時間";
        }
        if ("laowangmao".equals(key)) {
            return app + "開了，業績會來的";
        }
        if ("miaobaibai".equals(key)) {
            return "要辦公了嗎？白白陪著";
        }
        if ("miaobubu".equals(key)) {
            return app + "！布布當你秘書";
        }
        if ("miaoniang".equals(key)) {
            return "喵娘幫你盯著" + app;
        }
        if ("tuge".equals(key)) {
            return app + "開了！今天也超有效率";
        }
        if ("xiaotu".equals(key)) {
            return "拿出" + app + "，認真模式";
        }
        if ("xiaoying".equals(key)) {
            return app + "…這份做完就能下班嗎";
        }
        if ("yamei".equals(key)) {
            return "慢慢做" + app + "，別急";
        }
        if ("yumei".equals(key)) {
            return "文件像潮水，一頁一頁來";
        }
        return "你打開了" + app;
    }

    private static String openChat(String key, String app) {
        if ("fengge".equals(key)) {
            return app + "。先處理重要的";
        }
        if ("fengxiong".equals(key)) {
            return app + "開了，回完再忙";
        }
        if ("gugugaga".equals(key)) {
            return "ガガ，" + app + "有人說話";
        }
        if ("laowangmao".equals(key)) {
            return "貴人來電？" + app + "開了";
        }
        if ("miaobaibai".equals(key)) {
            return "有人找你了嗎？";
        }
        if ("miaobubu".equals(key)) {
            return app + "亮了！誰找布布的主人";
        }
        if ("miaoniang".equals(key)) {
            return "喵～誰在" + app + "找你";
        }
        if ("tuge".equals(key)) {
            return app + "！我先不當電燈泡";
        }
        if ("xiaotu".equals(key)) {
            return app + "來訊息了？快看";
        }
        if ("xiaoying".equals(key)) {
            return "又是" + app + "…會議是不是又來了";
        }
        if ("yamei".equals(key)) {
            return "有訊息的話，溫柔回喔";
        }
        if ("yumei".equals(key)) {
            return "海的另一邊有人叫你";
        }
        return "你打開了" + app;
    }

    private static String openMusic(String key, String app) {
        if ("fengge".equals(key)) {
            return "放歌可以，音量別吵到開會";
        }
        if ("fengxiong".equals(key)) {
            return "有音樂比較能專心";
        }
        if ("gugugaga".equals(key)) {
            return "ググガガ～放歌了";
        }
        if ("laowangmao".equals(key)) {
            return "放財神歌也行，讚讚";
        }
        if ("miaobaibai".equals(key)) {
            return "放歌了喵～白白搖尾巴";
        }
        if ("miaobubu".equals(key)) {
            return "音樂！布布要跟著跳";
        }
        if ("miaoniang".equals(key)) {
            return "喵娘想聽情歌～";
        }
        if ("tuge".equals(key)) {
            return "BGM 開了！狀態拉滿";
        }
        if ("xiaotu".equals(key)) {
            return app + "開了，今日 BGM 是？";
        }
        if ("xiaoying".equals(key)) {
            return "有歌聽，加班比較撐得住";
        }
        if ("yamei".equals(key)) {
            return "輕輕放音樂，心情會好";
        }
        if ("yumei".equals(key)) {
            return "像海浪的節奏～";
        }
        return "你打開了" + app;
    }

    private static String openVideo(String key, String app) {
        if ("fengge".equals(key)) {
            return app + "開了。看完回來工作";
        }
        if ("fengxiong".equals(key)) {
            return "看一下就好，記得休息眼睛";
        }
        if ("gugugaga".equals(key)) {
            return "ガガ，要看片了";
        }
        if ("laowangmao".equals(key)) {
            return "看片可以，別忘了正事發財";
        }
        if ("miaobaibai".equals(key)) {
            return "要看影片嗎？白白也可以看";
        }
        if ("miaobubu".equals(key)) {
            return "影片時間！布布坐好了";
        }
        if ("miaoniang".equals(key)) {
            return "陪你追劇喵～";
        }
        if ("tuge".equals(key)) {
            return app + "啟動！不要一直看廣告";
        }
        if ("xiaotu".equals(key)) {
            return app + "！今天看什麼";
        }
        if ("xiaoying".equals(key)) {
            return "用" + app + "偷閒一下…可以嗎";
        }
        if ("yamei".equals(key)) {
            return "看影片也要眨眼喔";
        }
        if ("yumei".equals(key)) {
            return "螢幕裡也有一片海";
        }
        return "你打開了" + app;
    }

    private static String openGame(String key, String app) {
        if ("fengge".equals(key)) {
            return app + "。玩完把正事收尾";
        }
        if ("fengxiong".equals(key)) {
            return "玩一下可以，別太晚";
        }
        if ("gugugaga".equals(key)) {
            return "ググ！要去冒險了";
        }
        if ("laowangmao".equals(key)) {
            return "遊戲也求一個大爆金";
        }
        if ("miaobaibai".equals(key)) {
            return "要玩" + app + "了？記得休息喵";
        }
        if ("miaobubu".equals(key)) {
            return "遊戲！布布幫你加油";
        }
        if ("miaoniang".equals(key)) {
            return "喵娘當你的幸運符～";
        }
        if ("tuge".equals(key)) {
            return app + "！上了就別坑隊友";
        }
        if ("xiaotu".equals(key)) {
            return app + "開了！今天要贏";
        }
        if ("xiaoying".equals(key)) {
            return "下班了才准開" + app + "…對吧";
        }
        if ("yamei".equals(key)) {
            return "玩歸玩，別熬夜喔";
        }
        if ("yumei".equals(key)) {
            return "去冒險吧，我會在岸邊等";
        }
        return "你打開了" + app;
    }

    private static String openNote(String key, String app) {
        if ("fengge".equals(key)) {
            return "把待辦記清楚";
        }
        if ("fengxiong".equals(key)) {
            return app + "開了，想到就寫";
        }
        if ("gugugaga".equals(key)) {
            return "ググ，要寫字了";
        }
        if ("laowangmao".equals(key)) {
            return "好點子先記下來，以後會值錢";
        }
        if ("miaobaibai".equals(key)) {
            return "要記什麼嗎？白白看著";
        }
        if ("miaobubu".equals(key)) {
            return app + "！布布想貼小貼紙";
        }
        if ("miaoniang".equals(key)) {
            return "喵娘幫你記住～";
        }
        if ("tuge".equals(key)) {
            return app + "！重點畫起來";
        }
        if ("xiaotu".equals(key)) {
            return "拿出" + app + "，寫重點";
        }
        if ("xiaoying".equals(key)) {
            return "又要記一堆…先寫最急的";
        }
        if ("yamei".equals(key)) {
            return "慢慢寫，字漂亮一點";
        }
        if ("yumei".equals(key)) {
            return "把心事寫在貝殼上";
        }
        return "你打開了" + app;
    }

    private static String openGeneric(String key, String app) {
        if ("fengge".equals(key)) {
            return app + "已開啟";
        }
        if ("fengxiong".equals(key)) {
            return "看到你打開" + app + "了";
        }
        if ("gugugaga".equals(key)) {
            return "ガガ，" + app + "打開了";
        }
        if ("laowangmao".equals(key)) {
            return app + "開了，萬事順心";
        }
        if ("miaobaibai".equals(key)) {
            return "你打開了" + app + "喵";
        }
        if ("miaobubu".equals(key)) {
            return "新程式！" + app;
        }
        if ("miaoniang".equals(key)) {
            return "喵？這是" + app + "耶";
        }
        if ("tuge".equals(key)) {
            return app + "登場！";
        }
        if ("xiaotu".equals(key)) {
            return "喔喔，" + app + "開了";
        }
        if ("xiaoying".equals(key)) {
            return "又開一個…" + app;
        }
        if ("yamei".equals(key)) {
            return "你開了" + app + "呢";
        }
        if ("yumei".equals(key)) {
            return "撲通，" + app + "游過來了";
        }
        return "你打開了" + app;
    }

    private static String closeBrowser(String key, String app) {
        return byKey(key,
                app + "已關閉。回來工作",
                app + "關了，別滑太晚",
                "ググ，" + app + "關掉了",
                app + "關掉，該賺的都看到了",
                app + "關掉了喵",
                "不看" + app + "了？布布還想逛",
                "不逛" + app + "了？喵娘還在",
                app + "關掉！下一件",
                app + "收起來，眼睛休息一下",
                app + "關了…可以專心一下了",
                app + "關掉了，眼睛會舒服一點",
                "游出" + app + "了～");
    }

    private static String closeCode(String key, String app) {
        return byKey(key,
                app + "已結束。進度如何",
                app + "關了，記得有存檔",
                "ガガ，code 收起來了",
                "程式存好了，財運也存好",
                "寫完了嗎？白白看你關了",
                "不敲鍵盤了？布布鼓掌",
                "寫完要摸摸頭～",
                app + "收工！今天也有產出",
                app + "收工！去喝水",
                "終於關掉" + app + "…能下班了嗎",
                "不寫了就休息喔",
                "這一波程式寫完了");
    }

    private static String closeOffice(String key, String app) {
        return byKey(key,
                app + "已關閉。報告交了嗎",
                app + "關了，今天也夠了",
                "ググ，" + app + "下班了",
                app + "關了，業績入袋",
                app + "收起來了，辛苦了",
                "秘書布布宣布：" + app + "下班",
                app + "收好了，來休息嘛",
                app + "收工！效率讚",
                app + "收工！文件存了嗎",
                app + "關掉…這份總算結束了吧",
                "辦公告一段落了喔",
                "文件退潮了，喘口氣");
    }

    private static String closeChat(String key, String app) {
        return byKey(key,
                app + "已關閉。專注眼前",
                app + "關了，少被訊息拉走",
                "ガガ，" + app + "不說話了",
                "貴人聊完了，繼續發財",
                app + "關掉了，等等再回也行",
                "不聊天了？布布陪你",
                "不回訊息了？那陪喵娘",
                app + "關掉，別再已讀不回了",
                app + "靜音收工",
                app + "關了，會議總該結束了吧",
                "聊天先停，好好休息",
                "對岸暫時沒人叫你了");
    }

    private static String closeMusic(String key, String app) {
        return byKey(key,
                app + "已停止。該開會了嗎",
                "音樂關了，世界突然很靜",
                "ググ…歌沒了",
                "歌單收了，耳朵也招財完畢",
                "歌停了…有點安靜喵",
                "音樂沒了，布布還想跳",
                "不唱歌了？喵娘有點寂寞",
                "BGM 關掉，下一場！",
                "BGM 停了，有點空",
                "歌停了，加班比較沒力…",
                "音樂停了，靜靜也好",
                "海浪聲也跟著停了");
    }

    private static String closeVideo(String key, String app) {
        return byKey(key,
                app + "已關閉。休息夠了就開工",
                app + "關了，記得眨眨眼",
                "ガガ，片片結束了",
                "片看完，該回來發財了",
                "影片看完了嗎？",
                "不看了？布布還沒看夠",
                "劇看完了？還想陪你",
                app + "關掉！別被片尾拖住",
                app + "關掉，眼睛歇一下",
                "偷閒結束…" + app + "關了",
                "不看了就讓眼睛休息",
                "螢幕裡的海退潮了");
    }

    private static String closeGame(String key, String app) {
        return byKey(key,
                app + "已結束。正事接回來",
                app + "關了，去睡覺比較實在",
                "ググ，冒險結束了",
                "不玩了，爆金就算數",
                "不玩" + app + "了？下次再加油",
                "遊戲關了，布布幫你慶功",
                "冒險結束，回喵娘這裡",
                app + "下了！下把再衝",
                app + "下線！今天有贏嗎",
                app + "關了…明天上班還是會累",
                "不玩了就早點休息喔",
                "冒險的人回到岸上了");
    }

    private static String closeNote(String key, String app) {
        return byKey(key,
                "待辦記下了就關",
                app + "關了，寫過就好",
                "ググ，字寫完了",
                "點子存起來，以後會值錢",
                "筆記本收好了喵",
                app + "闔上，貼紙也收好",
                "喵娘幫你記住了",
                app + "收！重點帶走",
                "重點寫完就關" + app,
                app + "關了，清單還是一樣長…",
                "寫完就闔上囉",
                "貝殼蓋起來了");
    }

    private static String closeGeneric(String key, String app) {
        return byKey(key,
                app + "已關閉",
                "看到你關掉" + app + "了",
                "ガガ，" + app + "關掉了",
                app + "關了，萬事順心",
                "你關掉了" + app + "喵",
                app + "掰掰！",
                app + "走了？喵娘還在",
                app + "下場！",
                app + "收起來了",
                "少一個視窗了…" + app,
                "你關掉" + app + "了呢",
                app + "游走了");
    }

    private static String byKey(String key, String fengge, String fengxiong, String gugugaga,
            String laowangmao, String miaobaibai, String miaobubu, String miaoniang,
            String tuge, String xiaotu, String xiaoying, String yamei, String yumei) {
        if ("fengge".equals(key)) {
            return fengge;
        }
        if ("fengxiong".equals(key)) {
            return fengxiong;
        }
        if ("gugugaga".equals(key)) {
            return gugugaga;
        }
        if ("laowangmao".equals(key)) {
            return laowangmao;
        }
        if ("miaobaibai".equals(key)) {
            return miaobaibai;
        }
        if ("miaobubu".equals(key)) {
            return miaobubu;
        }
        if ("miaoniang".equals(key)) {
            return miaoniang;
        }
        if ("tuge".equals(key)) {
            return tuge;
        }
        if ("xiaotu".equals(key)) {
            return xiaotu;
        }
        if ("xiaoying".equals(key)) {
            return xiaoying;
        }
        if ("yamei".equals(key)) {
            return yamei;
        }
        if ("yumei".equals(key)) {
            return yumei;
        }
        return "你關掉了應用程式";
    }
}
