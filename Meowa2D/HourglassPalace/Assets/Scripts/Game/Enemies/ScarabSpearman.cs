using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Scarab Spearman.  Holds a narrow platform and never jumps: the answer is
    /// footing, not speed.  The spear tip lights and holds before a long thrust,
    /// and if the cat leaps overhead the spear comes up as a guard, so vaulting
    /// it is not a free pass - the opening is its recovery, from behind.
    /// </summary>
    public sealed class ScarabSpearman : EnemyBase
    {
        [Header("Post")]
        public float advanceSpeed = 1.25f;
        public float postHalfWidth = 3.5f;
        public LayerMask solidMask;

        [Header("Thrust")]
        public float tipGlow = 0.4f;
        public float thrustActive = 0.18f;
        public float thrustRecover = 0.45f;
        public float thrustReach = 3.2f;
        public float betweenThrusts = 0.8f;
        public float triggerRange = 4.2f;

        [Header("Guard")]
        public float guardHeight = 1.4f;
        public float guardHoldTime = 0.45f;

        enum Phase { Hold, Glow, Thrust, Recover }
        Phase _phase = Phase.Hold;
        float _timer;
        float _cooldown;
        float _guard;

        /// <summary>While guarding, blows are turned aside instead of landing.</summary>
        public bool Guarding => _guard > 0f;

        protected override void Think(float dt)
        {
            var player = Player;
            if (_cooldown > 0f) _cooldown -= dt;
            if (_guard > 0f) _guard -= dt;

            if (player == null || player.Dead)
            {
                HoldPost(dt);
                return;
            }

            float dx = player.transform.position.x - transform.position.x;
            float dy = player.transform.position.y - transform.position.y;
            Facing = dx > 0f ? 1 : -1;

            // Overhead: raise the spear rather than be leapt past for free.
            if (Mathf.Abs(dx) < 2.2f && dy > guardHeight * 0.5f && dy < guardHeight * 2.4f)
            {
                _guard = guardHoldTime;
                BladeActive = true;
                return;
            }
            BladeActive = _phase == Phase.Thrust;

            switch (_phase)
            {
                case Phase.Hold:
                    if (Mathf.Abs(dy) < floorTolerance && Mathf.Abs(dx) < triggerRange && _cooldown <= 0f)
                    {
                        _phase = Phase.Glow;
                        _timer = tipGlow;
                    }
                    else HoldPost(dt);
                    break;

                case Phase.Glow:
                    _timer -= dt;
                    if (Sr != null)
                        Sr.color = Color.Lerp(Color.white, new Color(1f, 0.92f, 0.55f), 0.75f);
                    if (_timer <= 0f)
                    {
                        if (Sr != null) Sr.color = Color.white;
                        _phase = Phase.Thrust;
                        _timer = thrustActive;
                    }
                    break;

                case Phase.Thrust:
                    _timer -= dt;
                    BladeActive = true;
                    StrikePlayer(
                        (Vector2)transform.position + new Vector2(Facing * thrustReach * 0.5f, 1.0f),
                        new Vector2(thrustReach, 1.1f), 1, false);
                    if (_timer <= 0f)
                    {
                        BladeActive = false;
                        _phase = Phase.Recover;
                        _timer = thrustRecover;
                    }
                    break;

                case Phase.Recover:
                    _timer -= dt;
                    if (_timer <= 0f)
                    {
                        _phase = Phase.Hold;
                        _cooldown = betweenThrusts;
                    }
                    break;
            }
        }

        void HoldPost(float dt)
        {
            var player = Player;
            if (player == null || player.Dead) return;
            float dx = player.transform.position.x - transform.position.x;
            if (Mathf.Abs(dx) < 2.4f) return;
            if (Mathf.Abs(transform.position.x + Facing * 0.4f - Home.x) > postHalfWidth) return;
            if (!GroundAhead(0.7f, solidMask)) return;
            transform.position += new Vector3(Facing * advanceSpeed * dt, 0f, 0f);
        }

        public override void TakeHit(int amount, int fromFacing, bool lethal)
        {
            if (Guarding)
            {
                // Turned aside on the shaft: both fighters give ground.
                Clash();
                var player = Player;
                if (player != null)
                    player.Clash(transform.position.x > player.transform.position.x ? 1 : -1);
                return;
            }
            base.TakeHit(amount, fromFacing, lethal);
        }

        protected override void OnRestored()
        {
            _phase = Phase.Hold;
            _timer = 0f;
            _cooldown = 0f;
            _guard = 0f;
            if (Sr != null) Sr.color = Color.white;
        }
    }
}
