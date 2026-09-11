using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// The moon gate slot.  Victory is three conditions and no shortcut: the key
    /// in hand, the cat at the slot, and E pressed.  Felling the djinn does not
    /// end the slice by itself.
    /// </summary>
    public sealed class MoonGate : WorldInteractable
    {
        public Transform gateDoor;
        public Vector3 openOffset = new Vector3(0f, 4.6f, 0f);
        public float openSpeed = 2.2f;
        public SpriteRenderer keyhole;

        Vector3 _closed;
        bool _opened;

        public override int Priority => PriorityGate;
        public override PromptIcon Icon => PromptIcon.Moon;
        public override string Label =>
            GameDirector.Instance != null && GameDirector.Instance.HasMoonKey ? "插入月鑰" : "月門緊閉";

        void Awake()
        {
            if (gateDoor == null) gateDoor = transform;
            _closed = gateDoor.position;
        }

        void Update()
        {
            if (keyhole != null)
            {
                bool ready = GameDirector.Instance != null && GameDirector.Instance.HasMoonKey;
                keyhole.color = ready
                    ? new Color(0.78f, 0.86f, 1f, 0.7f + 0.3f * Mathf.Sin(Time.time * 4f))
                    : new Color(0.35f, 0.38f, 0.5f, 0.8f);
            }
            if (!_opened) return;
            gateDoor.position = Vector3.MoveTowards(gateDoor.position, _closed + openOffset,
                openSpeed * Time.unscaledDeltaTime);
        }

        public override void Activate(PlayerController player)
        {
            if (_opened) return;
            var game = GameDirector.Instance;
            if (game == null || !game.TryOpenMoonGate()) return;
            _opened = true;
        }
    }
}
