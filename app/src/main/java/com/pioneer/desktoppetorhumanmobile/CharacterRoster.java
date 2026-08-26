package com.pioneer.desktoppetorhumanmobile;

/** The shared ordering for the 12 character assets, labels, and dialogue. */
final class CharacterRoster {
    static final String[] IDS = {
            "fengge", "fengxiong", "gugugaga", "laowangmao",
            "miaobaibai", "miaobubu", "miaoniang", "tuge",
            "xiaotu", "xiaoying", "yamei", "yumei"
    };

    static final String[] NAMES = {
            "鋒哥", "鋒兄", "咕咕嘎嘎", "老王貓",
            "喵白白", "喵布布", "喵娘", "兔哥",
            "小兔", "小影", "雅美", "玉美"
    };

    static final boolean[] PRIMARY_IS_HUMAN = {
            true, true, false, false,
            false, false, true, true,
            true, true, true, true
    };

    static final String[] PET_MODE_NAMES = {
            "黑貓模式", "柴犬模式", "企鵝模式", "招財貓模式",
            "白貓模式", "三花模式", "貓咪模式", "兔子模式",
            "兔子模式", "黑貓模式", "紅貓模式", "海豚模式"
    };

    private CharacterRoster() {
    }

    static int clampIndex(int index) {
        return Math.max(0, Math.min(IDS.length - 1, index));
    }
}
