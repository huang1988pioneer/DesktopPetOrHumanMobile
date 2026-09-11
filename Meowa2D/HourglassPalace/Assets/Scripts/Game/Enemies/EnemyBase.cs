using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Shared body for every palace defender: a small pool of effective hits,
    /// a clear stagger on each one, blade-clash support, and a lantern snapshot
    /// so a guard felled before the flame stays down and one felled after it
    /// comes back.
    /// </summary>
    public abstract class EnemyBase : MonoBehaviour, IDamageable, ISnapshotable
    {
        [Header("Body")]
        public int hitsToDown = 3;
        public float staggerTime = 0.45f;
        public float knockback = 0.6f;
        [Tooltip("Generated enemy art faces left; flip handling keys off this.")]
        public bool spriteFacesLeft = true;

        [Header("Engagement")]
        public float engageRange = 6.5f;
        public float floorTolerance = 1.6f;

        protected SpriteRenderer Sr;
        protected Vector3 Home;
        protected int Facing = -1;
        protected float Stagger;

        int _hits;
        bool _down;
        float _flash;
        Color _baseColor = Color.white;
        string _snapshotId;

        public bool Downed => _down;
        public bool BladeActive { get; protected set; }
        public string SnapshotId => _snapshotId ??= HierarchyId.Of(transform);

        protected PlayerController Player
        {
            get
            {
                var game = GameDirector.Instance;
                return game != null ? game.player : null;
            }
        }

        protected bool Idle => GameDirector.Paused || _down;

        protected virtual void Awake()
        {
            Sr = GetComponentInChildren<SpriteRenderer>();
            if (Sr != null) _baseColor = Sr.color;
            Home = transform.position;
            _snapshotId = HierarchyId.Of(transform);
        }

        protected virtual void Update()
        {
            if (_flash > 0f)
            {
                _flash -= Time.deltaTime;
                if (Sr != null)
                    Sr.color = _flash > 0f ? Color.Lerp(_baseColor, new Color(1f, 0.5f, 0.45f), 0.8f) : _baseColor;
            }
            if (Idle) return;
            if (Stagger > 0f)
            {
                Stagger -= Time.deltaTime;
                BladeActive = false;
                return;
            }
            Think(Time.deltaTime);
            ApplyFacing();
        }

        protected abstract void Think(float dt);

        protected void ApplyFacing()
        {
            if (Sr == null) return;
            Sr.flipX = spriteFacesLeft ? Facing > 0 : Facing < 0;
        }

        /// <summary>True when the ground continues far enough ahead to step onto.</summary>
        protected bool GroundAhead(float distance, LayerMask solid)
        {
            var origin = (Vector2)transform.position + new Vector2(Facing * distance, 0.35f);
            return Physics2D.Raycast(origin, Vector2.down, 1.3f, solid);
        }

        /// <summary>
        /// Enemy attack resolution.  If the cat's blade is live in the same
        /// space the strikes cancel into a clash instead of a hit.
        /// </summary>
        protected void StrikePlayer(Vector2 center, Vector2 size, int damage, bool lethal)
        {
            var player = Player;
            if (player == null || player.Dead) return;
            var half = size * 0.5f;
            var d = player.Center - center;
            if (Mathf.Abs(d.x) > half.x || Mathf.Abs(d.y) > half.y) return;

            if (player.BladeActive)
            {
                player.Clash(transform.position.x > player.transform.position.x ? 1 : -1);
                Clash();
                return;
            }
            player.Hurt(damage, lethal, transform.position.x > player.transform.position.x ? -1 : 1);
        }

        public virtual void Clash()
        {
            BladeActive = false;
            Stagger = Mathf.Max(Stagger, 0.3f);
            var player = Player;
            int away = player != null && player.transform.position.x > transform.position.x ? -1 : 1;
            transform.position += new Vector3(away * knockback * 0.8f, 0f, 0f);
        }

        public virtual void TakeHit(int amount, int fromFacing, bool lethal)
        {
            if (_down) return;
            _hits += Mathf.Max(1, amount);
            _flash = 0.14f;
            HitStop.Punch(0.05f);
            // Every hit must shove the guard clear, so the cat cannot stand in
            // its face and saw through the fight.
            transform.position += new Vector3(fromFacing * knockback, 0f, 0f);
            Stagger = staggerTime;
            BladeActive = false;
            if (lethal || _hits >= hitsToDown) GoDown();
        }

        protected virtual void GoDown()
        {
            _down = true;
            BladeActive = false;
            OnDowned();
            gameObject.SetActive(false);
        }

        protected virtual void OnDowned() { }

        protected virtual void OnRestored() { }

        sealed class Snap
        {
            public int Hits;
            public bool Down;
            public Vector3 Position;
            public int Facing;
        }

        public virtual object Capture() => new Snap
        {
            Hits = _hits,
            Down = _down,
            Position = transform.position,
            Facing = Facing
        };

        public virtual void Restore(object state)
        {
            if (state is not Snap snap) return;
            _hits = snap.Hits;
            _down = snap.Down;
            transform.position = snap.Position;
            Facing = snap.Facing == 0 ? -1 : snap.Facing;
            Stagger = 0f;
            BladeActive = false;
            _flash = 0f;
            if (Sr != null) Sr.color = _baseColor;
            gameObject.SetActive(!_down);
            ApplyFacing();
            OnRestored();
        }
    }
}
