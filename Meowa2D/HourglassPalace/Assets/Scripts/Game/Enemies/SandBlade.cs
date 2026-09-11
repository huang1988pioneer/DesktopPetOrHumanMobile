using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// The djinn's readable projectile: slow enough to duck, hang under or cut
    /// out of the air with the scimitar.
    /// </summary>
    public sealed class SandBlade : MonoBehaviour
    {
        public float speed = 6.5f;
        public float life = 4f;
        public LayerMask solidMask;
        public Vector2 hitSize = new Vector2(0.7f, 0.7f);

        int _dir = 1;
        float _clock;

        public void Launch(int direction)
        {
            _dir = direction >= 0 ? 1 : -1;
            var sr = GetComponent<SpriteRenderer>();
            if (sr != null) sr.flipX = _dir < 0;
        }

        void Update()
        {
            if (GameDirector.Paused) return;
            float dt = Time.deltaTime;
            _clock += dt;
            transform.position += new Vector3(_dir * speed * dt, 0f, 0f);
            transform.Rotate(0f, 0f, -540f * dt * _dir);

            if (_clock > life || Physics2D.OverlapBox(transform.position, hitSize * 0.8f, 0f, solidMask))
            {
                Destroy(gameObject);
                return;
            }

            var player = GameDirector.Instance != null ? GameDirector.Instance.player : null;
            if (player == null || player.Dead) return;
            var d = player.Center - (Vector2)transform.position;
            if (Mathf.Abs(d.x) > hitSize.x || Mathf.Abs(d.y) > hitSize.y + 0.5f) return;

            if (player.BladeActive)
            {
                // Cut out of the air - the cat's answer to the first phase.
                HitStop.Punch(0.05f);
                Destroy(gameObject);
                return;
            }
            player.Hurt(1, false, _dir);
            Destroy(gameObject);
        }
    }
}
