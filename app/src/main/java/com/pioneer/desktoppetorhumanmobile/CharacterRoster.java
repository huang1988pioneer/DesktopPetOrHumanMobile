package com.pioneer.desktoppetorhumanmobile;

/** The shared ordering for the 10 companion assets, labels, and dialogue. */
final class CharacterRoster {
    static final String[] IDS = {
            "gpt_daughter", "claude_daughter", "whale_daughter", "gugugaga",
            "fengxiong", "tuge", "miaobubu", "miaobaibai", "yamei", "yumei"
    };

    static final String[] NAMES = {
            "GPT 娘", "Claude 娘", "鯨魚娘", "咕咕嘎嘎",
            "鋒兄", "塗哥", "喵布布", "喵白白", "牙妹", "魚妹"
    };

    private CharacterRoster() {
    }

    static int clampIndex(int index) {
        return Math.max(0, Math.min(IDS.length - 1, index));
    }
}
