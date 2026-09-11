using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// A saw on a fixed track.  It never chases and never changes speed - the
    /// whole danger is whether you read its pass before you commit to the gap.
    /// </summary>
    public sealed class SawBlade : MonoBehaviour
    {
        public Vector2 travel = new Vector2(6f, 0f);
        public float speed = 3.2f;
        public float spin = 420f;
        public float pauseAtEnds = 0.25f;
        public float hitRadius = 0.75f;

        Vector3 _a;
        Vector3 _b;
        float _t;
        int _dir = 1;
        float _pause;

        void Awake()
        {
            _a = transform.position;
            _b = _a + (Vector3)travel;
        }

        void Update()
        {
            if (GameDirector.Paused) return;
            float dt = Time.deltaTime;
            transform.Rotate(0f, 0f, -spin * dt);

            float span = Mathf.Max(0.01f, Vector3.Distance(_a, _b));
            if (_pause > 0f) _pause -= dt;
            else
            {
                _t += _dir * (speed / span) * dt;
                if (_t >= 1f) { _t = 1f; _dir = -1; _pause = pauseAtEnds; }
                else if (_t <= 0f) { _t = 0f; _dir = 1; _pause = pauseAtEnds; }
            }
            transform.position = Vector3.Lerp(_a, _b, _t);

            var player = GameDirector.Instance != null ? GameDirector.Instance.player : null;
            if (player == null || player.Dead) return;
            if (Vector2.Distance(player.Center, transform.position) < hitRadius + 0.35f)
                player.Hurt(1, true);
        }

        void OnDrawGizmosSelected()
        {
            Gizmos.color = new Color(1f, 0.4f, 0.4f, 0.8f);
            Gizmos.DrawLine(transform.position, transform.position + (Vector3)travel);
            Gizmos.DrawWireSphere(transform.position, hitRadius);
        }
    }
}
