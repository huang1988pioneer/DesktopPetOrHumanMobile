using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// A marked column of falling sand.  The mark comes first and stays long
    /// enough to walk out of; the fall itself costs one paw, never the run.
    /// </summary>
    public sealed class SandColumn : MonoBehaviour
    {
        public float warnTime = 0.8f;
        public float fallTime = 0.7f;
        public float fadeTime = 0.35f;
        public float width = 1.2f;
        public float height = 7f;

        SpriteRenderer _sr;
        float _clock;
        bool _hitLanded;

        public void Arm()
        {
            _clock = 0f;
            _hitLanded = false;
        }

        void Awake()
        {
            _sr = GetComponentInChildren<SpriteRenderer>();
        }

        void Update()
        {
            if (GameDirector.Paused) return;
            _clock += Time.deltaTime;

            if (_clock < warnTime)
            {
                // A thin bright mark on the floor: the warning, not the damage.
                Shape(width * 0.9f, 0.18f, 0f);
                if (_sr != null)
                    _sr.color = new Color(1f, 0.85f, 0.45f, Mathf.PingPong(_clock * 6f, 0.7f) + 0.3f);
                return;
            }

            if (_clock < warnTime + fallTime)
            {
                Shape(width, height, height * 0.5f);
                if (_sr != null) _sr.color = new Color(0.95f, 0.82f, 0.55f, 0.92f);
                TryHit();
                return;
            }

            float t = (_clock - warnTime - fallTime) / Mathf.Max(0.01f, fadeTime);
            if (_sr != null) _sr.color = new Color(0.95f, 0.82f, 0.55f, Mathf.Lerp(0.92f, 0f, t));
            if (t >= 1f) Destroy(gameObject);
        }

        void Shape(float w, float h, float yOffset)
        {
            if (_sr == null) return;
            _sr.drawMode = SpriteDrawMode.Sliced;
            _sr.size = new Vector2(w, h);
            _sr.transform.localPosition = new Vector3(0f, yOffset, 0f);
        }

        void TryHit()
        {
            if (_hitLanded) return;
            var player = GameDirector.Instance != null ? GameDirector.Instance.player : null;
            if (player == null || player.Dead) return;
            var d = player.Center - (Vector2)transform.position;
            if (Mathf.Abs(d.x) > width * 0.5f || d.y < -0.5f || d.y > height) return;
            _hitLanded = true;
            player.Hurt(1, false, player.Facing * -1);
        }
    }
}
