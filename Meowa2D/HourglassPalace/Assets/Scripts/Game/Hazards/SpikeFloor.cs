using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Cycling spike floor.  The cycle is fixed and the rise is announced by a
    /// crack and a short flash, so the window is learnable rather than lucky.
    /// A pressure plate can be wired to stall it in the down state.
    /// </summary>
    public sealed class SpikeFloor : MonoBehaviour, IMechanismTarget
    {
        [Header("Cycle")]
        public float period = 2.4f;
        public float upTime = 0.9f;
        public float warnTime = 0.35f;
        public float phaseOffset;
        public float rise = 0.55f;
        public float travelTime = 0.08f;

        [Header("Parts (the trigger must sit on this same object so it moves with the blades)")]
        public Transform blades;
        public SpriteRenderer bladeSprite;
        public Collider2D hurtBox;

        Vector3 _downPos;
        Vector3 _upPos;
        bool _stalled;

        void Awake()
        {
            if (blades == null) blades = transform;
            if (hurtBox == null) hurtBox = GetComponent<Collider2D>();
            if (bladeSprite == null) bladeSprite = GetComponentInChildren<SpriteRenderer>();
            _upPos = blades.position;
            _downPos = _upPos + Vector3.down * rise;
            blades.position = _downPos;
            if (hurtBox != null) hurtBox.enabled = false;
        }

        void Update()
        {
            if (GameDirector.Paused) return;
            if (_stalled)
            {
                Settle(false, 0f);
                return;
            }

            float t = Mathf.Repeat(Time.time + phaseOffset, period);
            bool up = t < upTime;
            bool warning = !up && t > period - warnTime;
            Settle(up, warning ? Mathf.PingPong((period - t) * 14f, 1f) : 0f);
        }

        void Settle(bool up, float warnPulse)
        {
            var target = up ? _upPos : _downPos;
            blades.position = Vector3.MoveTowards(blades.position, target,
                (rise / Mathf.Max(0.01f, travelTime)) * Time.deltaTime);
            if (hurtBox != null) hurtBox.enabled = up && blades.position.y > _downPos.y + rise * 0.35f;
            if (bladeSprite != null)
                bladeSprite.color = warnPulse > 0f
                    ? Color.Lerp(Color.white, new Color(1f, 0.75f, 0.6f), warnPulse)
                    : Color.white;
        }

        /// <summary>Wired to a plate or lever: powered means the bed stays down.</summary>
        public void SetPowered(bool powered) => _stalled = powered;

        void OnTriggerEnter2D(Collider2D other) => Touch(other);
        void OnTriggerStay2D(Collider2D other) => Touch(other);

        void Touch(Collider2D other)
        {
            if (GameDirector.Paused) return;
            if (hurtBox != null && !hurtBox.enabled) return;
            var player = other.GetComponentInParent<PlayerController>();
            if (player != null) player.Hurt(1, true);
        }
    }
}
