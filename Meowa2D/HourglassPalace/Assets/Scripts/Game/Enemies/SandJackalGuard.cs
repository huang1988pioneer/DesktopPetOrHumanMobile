using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Sand Jackal Guard.  Patrols a short beat, then holds a duelling gap:
    /// it advances when the cat backs off but only by a step, so neither
    /// fighter can kite the other forever.  Edges, spikes and crumbling brick
    /// stop its pursuit.
    /// </summary>
    public sealed class SandJackalGuard : EnemyBase
    {
        [Header("Patrol")]
        public Transform leftBound;
        public Transform rightBound;
        public float patrolSpeed = 1.6f;
        public LayerMask solidMask;

        [Header("Duel")]
        public float holdMin = 1.8f;
        public float holdMax = 2.7f;
        public float advanceSpeed = 2.6f;
        public float retreatSpeed = 2.0f;
        public float windup = 0.35f;
        public float active = 0.12f;
        public float recover = 0.3f;
        public float betweenSwings = 0.55f;
        public float reach = 2.2f;

        enum Phase { Patrol, Face, Windup, Slash, Recover }
        Phase _phase = Phase.Patrol;
        float _timer;
        float _cooldown;
        float _retreatBudget;

        protected override void Think(float dt)
        {
            var player = Player;
            if (_cooldown > 0f) _cooldown -= dt;

            if (player == null || player.Dead)
            {
                Patrol(dt);
                return;
            }

            float dx = player.transform.position.x - transform.position.x;
            float dist = Mathf.Abs(dx);
            bool sameFloor = Mathf.Abs(player.transform.position.y - transform.position.y) < floorTolerance;

            if (_phase == Phase.Patrol)
            {
                if (!sameFloor || dist > engageRange) { Patrol(dt); return; }
                _phase = Phase.Face;
            }

            if (_phase == Phase.Face)
            {
                if (!sameFloor || dist > engageRange * 1.25f) { _phase = Phase.Patrol; return; }
                Facing = dx > 0f ? 1 : -1;

                if (dist > holdMax)
                {
                    // Close the gap, but never walk off a ledge to do it.
                    if (GroundAhead(0.8f, solidMask)) Step(advanceSpeed * dt);
                    _retreatBudget = 0.45f;
                }
                else if (dist < holdMin)
                {
                    // One step of ground given up, then it stands its ground.
                    if (_retreatBudget > 0f)
                    {
                        _retreatBudget -= dt;
                        Facing = -Facing;
                        if (GroundAhead(0.8f, solidMask)) Step(retreatSpeed * dt);
                        Facing = -Facing;
                    }
                }
                else if (_cooldown <= 0f)
                {
                    _phase = Phase.Windup;
                    _timer = windup;
                }
                return;
            }

            _timer -= dt;
            switch (_phase)
            {
                case Phase.Windup:
                    // shoulder rises: the tell the player reads
                    if (Sr != null) Sr.color = Color.Lerp(Color.white, new Color(1f, 0.86f, 0.6f), 0.6f);
                    if (_timer <= 0f)
                    {
                        if (Sr != null) Sr.color = Color.white;
                        _phase = Phase.Slash;
                        _timer = active;
                        BladeActive = true;
                    }
                    break;
                case Phase.Slash:
                    StrikePlayer(
                        (Vector2)transform.position + new Vector2(Facing * reach * 0.5f, 1.0f),
                        new Vector2(reach, 1.9f), 1, false);
                    if (_timer <= 0f)
                    {
                        BladeActive = false;
                        _phase = Phase.Recover;
                        _timer = recover;
                    }
                    break;
                case Phase.Recover:
                    if (_timer <= 0f)
                    {
                        _phase = Phase.Face;
                        _cooldown = betweenSwings;
                    }
                    break;
            }
        }

        void Patrol(float dt)
        {
            float minX = leftBound != null ? leftBound.position.x : Home.x - 3f;
            float maxX = rightBound != null ? rightBound.position.x : Home.x + 3f;
            Step(patrolSpeed * dt);
            if (transform.position.x <= minX) Facing = 1;
            else if (transform.position.x >= maxX) Facing = -1;
            else if (!GroundAhead(0.8f, solidMask)) Facing = -Facing;
        }

        void Step(float amount)
        {
            transform.position += new Vector3(Facing * amount, 0f, 0f);
        }

        protected override void OnRestored()
        {
            _phase = Phase.Patrol;
            _timer = 0f;
            _cooldown = 0f;
            if (Sr != null) Sr.color = Color.white;
        }
    }
}
