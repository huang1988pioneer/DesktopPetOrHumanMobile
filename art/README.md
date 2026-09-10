# 人物 SVG

`characters/` 是 12 位角色的人形向量原稿，每位有 `idle`（待機）、
`happy`（開心）、`sleep`（睡眠）三個版本，共 36 份。沿用原有角色的
髮型、服裝、配色與配件，以透明背景、柔和色塊、圓角線條重繪。
寵物型態仍使用既有圖像。

- `character-sheet-idle.svg`：完整角色總覽。
- `character-sheet-happy.svg`：開心表情與揮手動作。
- `character-sheet-sleep.svg`：閉眼、休息手勢與睡眠符號。

## 編輯

直接編輯 `characters/*_human_*.svg`，接著在專案根目錄執行：

```shell
node scripts/sync-character-art.mjs
node scripts/sync-character-art.mjs --check
```

同步工具不需要第三方套件，會生成：

- Android：`app/src/main/res/drawable/vector_*_human_*.xml`，由原生
  `VectorDrawable` 按目前大小繪製。
- iOS：`iOS/DesktopPetOrHumanMobile/CharacterArt.xcassets`，由 Xcode
  編譯 SVG，保留向量資料，並以原始彩色圖像載入。

向量使用相同的 400 × 400 viewBox；原稿僅使用展開後的 `path` 與
直接填色／描邊，不依賴字型、點陣圖、濾鏡或外部資源。繪圖軟體匯出時
請將群組變形、形狀與筆刷展開為路徑。同步器遇到不支援的元素會報錯，
避免兩個平台悄悄呈現不同的圖像。

`scripts/draw-human-characters.mjs` 保存本次手繪路徑的初始構圖與共用部件。
只有要重新生成全部原稿時才執行；它會覆寫 SVG 原稿的後續手動修改。
產物已納入專案，正常 Gradle／Xcode 建置不需要 Node.js。

平台格式參考：[Android VectorDrawable](https://developer.android.com/develop/ui/views/graphics/vector-drawable-resources)、
[Apple SVG image assets](https://developer.apple.com/documentation/xcode-release-notes/xcode-12-release-notes)。
