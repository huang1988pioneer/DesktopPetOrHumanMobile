# 角色設定圖

`turnarounds/` 保存 10 位新夥伴的正面、側面、背面設定圖：

| App 名稱 | 設定圖 |
| --- | --- |
| GPT 娘 | `gpt_daughter.png` |
| Claude 娘 | `claude_daughter.png` |
| 鯨魚娘 | `whale_daughter.png` |
| 咕咕嘎嘎 | `gugugaga.png` |
| 鋒兄 | `fengxiong.png` |
| 塗哥 | `tuge.png` |
| 喵布布 | `miaobubu.png` |
| 喵白白 | `miaobaibai.png` |
| 牙妹 | `yamei.png` |
| 魚妹 | `yumei.png` |

`../scripts/extract-turnaround-fronts.py` 會從每張設定圖擷取正面，移除
背景並輸出 App 使用的透明方形 PNG。重新產生需要 Python 3 與 FFmpeg。
