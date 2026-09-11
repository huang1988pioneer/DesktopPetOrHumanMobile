using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Collapsing sandstone.  Three beats of feedback - crack, drop, dust - and
    /// the choice it exists to pose: keep running, or wait for solid footing.
    /// It rebuilds on a lantern reset, so the question can be asked again.
    /// </summary>
    [RequireComponent(typeof(Rigidbody2D))]
    public sealed class CrumblingBrick : MonoBehaviour, ISnapshotable
    {
        public float crackDelay = 0.35f;
        public float dropDelay = 0.8f;
        public float rebuildDelay = 3.2f;
        public float shakeAmplitude = 0.045f;

        Rigidbody2D _rb;
        Collider2D _col;
        SpriteRenderer _sr;
        Vector3 _home;
        Color _base;
        float _clock = -1f;
        bool _falling;
        string _snapshotId;

        public string SnapshotId => _snapshotId;

        void Awake()
        {
            _rb = GetComponent<Rigidbody2D>();
            _col = GetComponent<Collider2D>();
            _sr = GetComponentInChildren<SpriteRenderer>();
            _home = transform.position;
            _base = _sr != null ? _sr.color : Color.white;
            _rb.bodyType = RigidbodyType2D.Kinematic;
            _rb.constraints = RigidbodyConstraints2D.FreezeRotation;
            _snapshotId = HierarchyId.Of(transform);
        }

        void OnCollisionEnter2D(Collision2D collision)
        {
            if (_clock >= 0f) return;
            if (collision.collider.GetComponentInParent<PlayerController>() == null) return;
            _clock = 0f;
        }

        void Update()
        {
            if (_clock < 0f || GameDirector.Paused) return;
            _clock += Time.deltaTime;

            if (!_falling)
            {
                if (_clock >= crackDelay)
                {
                    // cracks show, then the brick trembles on the edge of letting go
                    if (_sr != null) _sr.color = Color.Lerp(_base, new Color(0.62f, 0.46f, 0.36f), 0.85f);
                    float shake = shakeAmplitude * Mathf.Sin(_clock * 58f);
                    transform.position = _home + new Vector3(shake, 0f, 0f);
                }
                if (_clock >= dropDelay)
                {
                    _falling = true;
                    _rb.bodyType = RigidbodyType2D.Dynamic;
                    _rb.gravityScale = 3.4f;
                }
                return;
            }

            if (_clock >= dropDelay + rebuildDelay) Rebuild();
        }

        void Rebuild()
        {
            _clock = -1f;
            _falling = false;
            _rb.bodyType = RigidbodyType2D.Kinematic;
            _rb.linearVelocity = Vector2.zero;
            transform.position = _home;
            transform.rotation = Quaternion.identity;
            if (_col != null) _col.enabled = true;
            if (_sr != null) _sr.color = _base;
        }

        public object Capture() => null;

        public void Restore(object state) => Rebuild();
    }
}
