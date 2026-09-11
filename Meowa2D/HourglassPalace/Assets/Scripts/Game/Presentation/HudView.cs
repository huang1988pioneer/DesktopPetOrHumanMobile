using UnityEngine;
using UnityEngine.UI;

namespace HourglassPalace
{
    /// <summary>
    /// Reads state, owns none of it.  Three paws top-left, the royal hourglass
    /// top-right, a single prompt glyph beside whatever is in reach, and one
    /// banner line reserved for the two endings.
    /// </summary>
    public sealed class HudView : MonoBehaviour
    {
        [Header("Paws")]
        public Image[] paws;
        public Sprite pawFull;
        public Sprite pawHurt;
        public Sprite pawEmpty;

        [Header("Hourglass")]
        public Image sandFill;
        public Text timeText;
        public Image lowSandFrame;

        [Header("Prompt")]
        public GameObject promptRoot;
        public Image promptIcon;
        public Text promptLabel;
        public Sprite iconPaw;
        public Sprite iconHand;
        public Sprite iconBlade;
        public Sprite iconMoon;

        [Header("Banners")]
        public Text bannerText;
        public Text screenText;
        public Text hintText;

        int _lastHealth = -1;
        float _damageFlash;
        float _screenFade;
        int _lastRoom = -1;
        CameraDirector _camera;

        void Awake()
        {
            _camera = FindFirstObjectByType<CameraDirector>();
            if (promptRoot != null) promptRoot.SetActive(false);
            if (lowSandFrame != null) lowSandFrame.color = new Color(1f, 0.55f, 0.3f, 0f);
        }

        void OnEnable()
        {
            GameEvents.PlayerDamaged += OnDamaged;
        }

        void OnDisable()
        {
            GameEvents.PlayerDamaged -= OnDamaged;
        }

        void OnDamaged(int health, bool lethal) => _damageFlash = 0.35f;

        void Update()
        {
            var game = GameDirector.Instance;
            if (game == null) return;
            var player = game.player;

            DrawPaws(player);
            DrawHourglass(game);
            DrawPrompt(player, game);
            DrawBanner(game);
            DrawScreenName();
            if (_damageFlash > 0f) _damageFlash -= Time.unscaledDeltaTime;
        }

        void DrawPaws(PlayerController player)
        {
            if (paws == null || player == null) return;
            if (_lastHealth != player.Health) _lastHealth = player.Health;
            for (int i = 0; i < paws.Length; i++)
            {
                if (paws[i] == null) continue;
                // full, damaged, spent - three readable states, never a number
                paws[i].sprite = i < player.Health ? pawFull : (i == player.Health ? pawHurt : pawEmpty);
                paws[i].color = i < player.Health
                    ? Color.white
                    : new Color(1f, 1f, 1f, _damageFlash > 0f && i == player.Health ? 0.35f + Mathf.PingPong(Time.unscaledTime * 12f, 0.5f) : 0.55f);
            }
        }

        void DrawHourglass(GameDirector game)
        {
            if (sandFill != null) sandFill.fillAmount = game.RemainingFraction;
            if (timeText != null)
            {
                int sec = Mathf.CeilToInt(game.Remaining);
                timeText.text = $"{sec / 60:00}:{sec % 60:00}";
                timeText.color = game.LowSand ? new Color(1f, 0.62f, 0.38f) : new Color(0.94f, 0.92f, 0.86f);
            }
            if (lowSandFrame != null)
            {
                // low sand pulses the frame; it never screams
                float a = game.LowSand && !game.PalaceSealed
                    ? 0.12f + 0.16f * Mathf.Abs(Mathf.Sin(Time.unscaledTime * 2.1f))
                    : 0f;
                lowSandFrame.color = new Color(1f, 0.5f, 0.28f, a);
            }
        }

        void DrawPrompt(PlayerController player, GameDirector game)
        {
            if (promptRoot == null) return;
            var prompt = player != null && !player.Dead && !game.PalaceSealed && !game.Won ? player.Prompt : null;
            promptRoot.SetActive(prompt != null);
            if (prompt == null) return;
            if (promptLabel != null) promptLabel.text = "E  " + prompt.Label;
            if (promptIcon != null)
            {
                promptIcon.sprite = prompt.Icon switch
                {
                    PromptIcon.Hand => iconHand,
                    PromptIcon.Blade => iconBlade,
                    PromptIcon.Moon => iconMoon,
                    _ => iconPaw
                };
                promptIcon.enabled = promptIcon.sprite != null;
            }
        }

        void DrawBanner(GameDirector game)
        {
            if (bannerText == null) return;
            if (game.Won)
                bannerText.text = "月門開啟 · 落沙大廳封鎖解除";
            else if (game.PalaceSealed)
                bannerText.text = "宮門封閉 · 空白鍵自最近的燈籠重入";
            else
                bannerText.text = "";

            if (hintText == null) return;
            hintText.text = game.Won || game.PalaceSealed
                ? ""
                : (game.HasMoonKey ? "月鑰在手 · 前往月門" : "A D 走 · Shift 跑 · Space 跳 · W 攀 · J 揮刀 · E 互動");
        }

        void DrawScreenName()
        {
            if (screenText == null || _camera == null) return;
            var room = _camera.CurrentRoom;
            if (room == null) return;
            if (room.index != _lastRoom)
            {
                _lastRoom = room.index;
                _screenFade = 2.4f;
                screenText.text = $"{room.index:00}  {room.screenName}";
            }
            if (_screenFade > 0f) _screenFade -= Time.unscaledDeltaTime;
            var c = screenText.color;
            c.a = Mathf.Clamp01(_screenFade > 1.6f ? (2.4f - _screenFade) / 0.8f : _screenFade / 1.6f);
            screenText.color = c;
        }
    }
}
