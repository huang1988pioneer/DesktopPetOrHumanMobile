package com.pioneer.desktoppetorhumanmobile;

import java.util.Random;

final class CharacterTalk {
    private static final Random RANDOM = new Random();

    private static final String[] GREETINGS = {
            "鋒哥上線，開始幹活",
            "鋒兄來了，今天一起慢慢來",
            "ググ！ガガ！企鵝報到",
            "招財進寶！老王貓報到",
            "喵～白白來陪你了",
            "布布衝出來囉！",
            "喵娘來啦～一起玩嘛",
            "塗哥登場！準備開幹",
            "嘿，小塗報到！",
            "小英打卡了",
            "牙妹來陪你了",
            "撲通～魚妹游過來了"
    };

    private static final String[] CHEERS = {
            "做得好！",
            "哈哈，被你逗樂了",
            "ガガガ！好開心",
            "金元寶來了！",
            "喵嗚！好開心",
            "嘿嘿，再摸一下！",
            "喵喵！最喜歡你了",
            "耶嘿！超有精神",
            "耶！今天也超讚",
            "有被鼓勵到…",
            "呵呵，好開心",
            "尾巴都在搖了！"
    };

    private static final String[] SLEEPS = {
            "午休十分鐘，別吵",
            "先瞇一下，別吵",
            "企鵝要站著睡了…Zzz",
            "賺飽了，先睡一波",
            "白白想睡了…",
            "布布先趴一下…",
            "喵娘要捲成一團了",
            "才沒有累…只是躺一下",
            "先喝口茶再睡…",
            "先閉一下眼…",
            "蓋好被子，晚安",
            "縮回貝殼裡睡一下"
    };

    private static final String[] FAREWELLS = {
            "鋒哥下班了",
            "鋒兄先走了，別太晚睡",
            "ググ…ペンギン寝る",
            "財神先收工，明天見",
            "白白先去睡覺了喵",
            "布布明天再來玩",
            "喵娘去睡美容覺囉",
            "塗哥先撤！明天再衝",
            "小塗先閃了，掰掰",
            "小英終於可以打卡下班…",
            "牙妹先回家了，晚安",
            "魚妹游回海里囉"
    };

    private static final String[][] IDLES = {
            {"進度如何？", "會議先放著", "需要幫忙再叫我"},
            {"記得喝水", "工作別太拼", "我在旁邊看著"},
            {"ググガガ", "今天也要滑行", "肚子想吃魚"},
            {"財源滾滾來", "摸摸我招好運", "今天也要發大財"},
            {"白白在這裡喔", "可以摸摸我的帽子", "今天也要乖乖的"},
            {"有零食嗎", "布布最可愛", "陪我玩嘛"},
            {"摸摸頭可以嗎", "今天也要被疼愛", "長按可以換朋友"},
            {"今天也要第一", "別發呆啦", "跟塗哥一起加油"},
            {"作業寫完了嗎", "長按可以換朋友", "我會待在桌角"},
            {"郵件回完了嗎", "再撐一下就下班", "咖啡續一下"},
            {"功課寫了嗎", "要記得休息", "我會安靜陪著你"},
            {"這裡也有海的味道", "想聽海浪嗎", "別把我弄乾了"}
    };

    private CharacterTalk() {
    }

    static String greeting(int index) {
        return GREETINGS[clamp(index)];
    }

    static String cheer(int index) {
        return CHEERS[clamp(index)];
    }

    static String sleep(int index) {
        return SLEEPS[clamp(index)];
    }

    static String farewell(int index) {
        return FAREWELLS[clamp(index)];
    }

    static String idle(int index) {
        String[] lines = IDLES[clamp(index)];
        return lines[RANDOM.nextInt(lines.length)];
    }

    private static int clamp(int index) {
        if (index < 0) {
            return 0;
        }
        if (index >= GREETINGS.length) {
            return GREETINGS.length - 1;
        }
        return index;
    }
}
