package com.pioneer.desktoppetorhumanmobile;

import java.util.Random;

final class CharacterTalk {
    private static final Random RANDOM = new Random();

    private static final String[] GREETINGS = {
            "GPT 娘上線了，今天也一起加油",
            "Claude 娘來陪你整理思緒",
            "鯨魚娘游過來囉～",
            "咕咕嘎嘎報到！",
            "鋒兄來了，今天一起慢慢來",
            "塗哥登場！準備開幹",
            "布布衝出來囉！",
            "喵～白白來陪你了",
            "牙妹來陪你了",
            "魚妹來了，今天也要順順利利"
    };

    private static final String[] CHEERS = {
            "靈感來了！",
            "思路清晰，繼續前進",
            "咕嚕～一起乘著浪花跳一下",
            "咕咕嘎嘎，超開心！",
            "哈哈，被你逗樂了",
            "耶嘿！超有精神",
            "喵嗚！好開心",
            "白白最喜歡你了",
            "呵呵，好開心",
            "一起游向好心情！"
    };

    private static final String[] SLEEPS = {
            "先休息一下，等會兒再想新點子",
            "Claude 娘要充會兒電了…",
            "鯨魚娘浮上水面打個盹…Zzz",
            "企鵝要站著睡了…Zzz",
            "先瞇一下，別太晚睡",
            "塗哥先躺一下，等等再衝",
            "布布先趴一下…",
            "白白想睡了…",
            "蓋好被子，晚安",
            "魚妹游回夢裡休息囉"
    };

    private static final String[] FAREWELLS = {
            "GPT 娘先下線，等等見！",
            "Claude 娘先去休息了",
            "鯨魚娘游回海裡囉，掰掰～",
            "咕咕嘎嘎先去睡了，晚安",
            "鋒兄先走了，別太晚睡",
            "塗哥先撤！明天再衝",
            "布布明天再來玩",
            "白白先去睡覺了喵",
            "牙妹先回家了，晚安",
            "魚妹游回海裡囉"
    };

    private static final String[][] IDLES = {
            {"需要靈感時，敲敲我", "今天想先做哪件事？", "我會陪你把事情理清楚"},
            {"慢慢來，思考也需要空間", "要不要先整理一下待辦？", "我在這裡陪你"},
            {"海浪的節奏讓人很放鬆", "今天也要記得喝水喔", "一起游一小圈嗎？"},
            {"咕咕嘎嘎！", "今天也要開開心心", "長按可以換夥伴"},
            {"記得喝水", "工作別太拼", "我在旁邊看著"},
            {"塗哥陪你一起加油", "事情一件一件來", "長按可以換夥伴"},
            {"布布最可愛", "有零食嗎？", "陪我玩嘛"},
            {"白白在這裡喔", "今天也要乖乖的", "長按可以換夥伴"},
            {"記得照顧自己", "要休息一下嗎？", "我會安靜陪著你"},
            {"別忘了補充水分", "一起游向好心情", "今天也要順順利利"}
    };

    private static final String[] FEEDS = {
            "靈感補給完成！",
            "謝謝，現在充滿靈感了",
            "一口小魚，幸福滿滿",
            "魚魚到位！咕咕嘎嘎",
            "謝啦，繼續加油",
            "補給完成！等等再衝",
            "呼嚕～開飯啦",
            "謝謝，我開動了喵",
            "點心時間，好開心",
            "補充能量，繼續游吧"
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

    static String feed(int index) {
        return FEEDS[clamp(index)];
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
