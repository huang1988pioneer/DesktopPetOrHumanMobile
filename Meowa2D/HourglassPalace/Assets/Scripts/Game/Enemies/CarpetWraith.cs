using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Carpet Wraith.  Owns one strip of air and nothing else: it drifts, tenses
    /// (cloth pulled taut, blue rim), swoops once through the cat's position and
    /// returns to its lane.  Two blade hits disperse it, and a linked lever can
    /// send it away entirely.
    /// </summary>
    public sealed class CarpetWraith : EnemyBase, IMechanismTarget
    {
        [Header("Lane")]
        public float laneHalfWidth = 5f;
        public float driftSpeed = 1.9f;
        public float bobAmplitude = 0.45f;
        public float bobRate = 1.3f;

        [Header("Swoop")]
        public float tellTime = 0.45f;
        public float swoopSpeed = 9f;
        public float returnSpeed = 4.5f;
        public float cooldown = 2.2f;
        public Vector2 contactSize = new Vector2(1.9f, 1.4f);

        enum Phase { Drift, Tell, Swoop, Return }
        Phase _phase = Phase.Drift;
        float _timer;
        float _cooldown;
        float _bobClock;
        Vector2 _swoopTarget;
        int _driftDir = 1;
        bool _retired;

        protected override void Awake()
        {
            base.Awake();
            hitsToDown = Mathf.Min(hitsToDown, 2);
        }

        protected override void Think(float dt)
        {
            if (_retired) return;
            var player = Player;
            _bobClock += dt;
            if (_cooldown > 0f) _cooldown -= dt;

            switch (_phase)
            {
                case Phase.Drift:
                    Drift(dt);
                    if (player != null && !player.Dead && _cooldown <= 0f
                        && Vector2.Distance(player.Center, transform.position) < engageRange)
                    {
                        _phase = Phase.Tell;
                        _timer = tellTime;
                        _swoopTarget = player.Center;
                    }
                    break;

                case Phase.Tell:
                    _timer -= dt;
                    // cloth snaps taut and the ghost-blue rim flickers
                    if (Sr != null)
                        Sr.color = Color.Lerp(Color.white, new Color(0.62f, 0.78f, 1f),
                            Mathf.PingPong(_timer * 8f, 1f));
                    if (_timer <= 0f)
                    {
                        if (Sr != null) Sr.color = Color.white;
                        _phase = Phase.Swoop;
                        _timer = 1.3f;
                    }
                    break;

                case Phase.Swoop:
                    _timer -= dt;
                    transform.position = Vector2.MoveTowards(transform.position, _swoopTarget, swoopSpeed * dt);
                    StrikePlayer(transform.position, contactSize, 1, false);
                    if (_timer <= 0f || Vector2.Distance(transform.position, _swoopTarget) < 0.15f)
                        _phase = Phase.Return;
                    break;

                case Phase.Return:
                    var lane = new Vector2(transform.position.x, Home.y);
                    transform.position = Vector2.MoveTowards(transform.position, lane, returnSpeed * dt);
                    if (Mathf.Abs(transform.position.y - Home.y) < 0.08f)
                    {
                        _phase = Phase.Drift;
                        _cooldown = cooldown;
                    }
                    break;
            }

            var seen = Player;
            Facing = seen != null && !seen.Dead
                ? (seen.transform.position.x > transform.position.x ? 1 : -1)
                : _driftDir;
        }

        void Drift(float dt)
        {
            float x = transform.position.x + _driftDir * driftSpeed * dt;
            if (x < Home.x - laneHalfWidth) { x = Home.x - laneHalfWidth; _driftDir = 1; }
            else if (x > Home.x + laneHalfWidth) { x = Home.x + laneHalfWidth; _driftDir = -1; }
            transform.position = new Vector3(x, Home.y + Mathf.Sin(_bobClock * bobRate) * bobAmplitude, transform.position.z);
        }

        /// <summary>A thrown lever can clear the airspace for a while.</summary>
        public void SetPowered(bool powered)
        {
            _retired = powered;
            if (Sr != null) Sr.enabled = !powered;
            var col = GetComponent<Collider2D>();
            if (col != null) col.enabled = !powered;
            if (powered) _phase = Phase.Drift;
        }

        protected override void OnRestored()
        {
            _phase = Phase.Drift;
            _timer = 0f;
            _cooldown = 0f;
            _retired = false;
            if (Sr != null) { Sr.color = Color.white; Sr.enabled = true; }
            var col = GetComponent<Collider2D>();
            if (col != null) col.enabled = true;
        }
    }
}
