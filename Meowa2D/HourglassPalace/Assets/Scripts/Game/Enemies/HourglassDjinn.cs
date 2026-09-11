using System.Collections.Generic;
using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Hourglass Djinn.  The single boss of the slice, and a reading test rather
    /// than a damage race: each phase is one legible pattern, and each phase ends
    /// by sinking and baring its core for a short window worth exactly two hits.
    /// Nothing it does is an unwarned kill - the room's own traps stay lethal.
    /// </summary>
    public sealed class HourglassDjinn : EnemyBase
    {
        [Header("Arena")]
        public float hoverHeight = 2.6f;
        public float driftSpeed = 2.2f;
        public float keepAway = 4.5f;
        public float arenaHalfWidth = 7f;

        [Header("Pattern")]
        public SandBlade bladePrefab;
        public SandColumn columnPrefab;
        public float bladeInterval = 1.5f;
        public float columnInterval = 2.2f;
        public int attacksPerPhase = 4;

        [Header("Core")]
        public float coreWindow = 4f;
        public int hitsPerPhase = 2;

        [Header("Defeat")]
        [Tooltip("Revealed when the djinn falls - normally the moon key.")]
        public GameObject reward;
        [Tooltip("Machinery that shuts down with the djinn (sealed gates, snuffed platforms).")]
        public List<MonoBehaviour> shutdownTargets = new List<MonoBehaviour>();
        [Tooltip("Platforms this djinn can snuff out during its third phase.")]
        public List<MonoBehaviour> snuffTargets = new List<MonoBehaviour>();

        enum Phase { Pattern, Sinking, Core, Rising }
        Phase _phase = Phase.Pattern;
        int _stage = 1;
        int _attacksLeft;
        int _coreHits;
        float _timer;
        float _attackTimer;
        int _driftDir = 1;
        float _snuffTimer;
        bool _snuffed;

        public int Stage => _stage;
        public bool CoreExposed => _phase == Phase.Core;

        protected override void Awake()
        {
            base.Awake();
            hitsToDown = hitsPerPhase * 3;
            _attacksLeft = attacksPerPhase;
        }

        protected override void Think(float dt)
        {
            var player = Player;
            if (player == null) return;
            Facing = player.transform.position.x > transform.position.x ? 1 : -1;

            switch (_phase)
            {
                case Phase.Pattern: TickPattern(dt, player); break;
                case Phase.Sinking: TickSink(dt); break;
                case Phase.Core: TickCore(dt); break;
                case Phase.Rising: TickRise(dt); break;
            }
        }

        void TickPattern(float dt, PlayerController player)
        {
            Hover(dt, player);

            if (_stage >= 3)
            {
                // Third phase also snuffs footing in and out, forcing swaps
                // between the floor and the chains.
                _snuffTimer -= dt;
                if (_snuffTimer <= 0f)
                {
                    _snuffed = !_snuffed;
                    _snuffTimer = _snuffed ? 2.4f : 3.2f;
                    foreach (var t in snuffTargets)
                        if (t is IMechanismTarget m) m.SetPowered(_snuffed);
                }
            }

            _attackTimer -= dt;
            if (_attackTimer > 0f) return;

            if (_stage == 1) FireBlade(player);
            else if (_stage == 2) DropColumn(player);
            else if (Random.value < 0.5f) FireBlade(player); else DropColumn(player);

            _attackTimer = _stage == 1 ? bladeInterval : (_stage == 2 ? columnInterval : bladeInterval * 0.85f);
            if (--_attacksLeft <= 0)
            {
                _phase = Phase.Sinking;
                _timer = 0.55f;
            }
        }

        void Hover(float dt, PlayerController player)
        {
            float targetY = Home.y + hoverHeight;
            float x = transform.position.x;
            float dx = player.transform.position.x - x;
            if (Mathf.Abs(dx) < keepAway) _driftDir = dx > 0f ? -1 : 1;   // keeps its distance
            x += _driftDir * driftSpeed * dt;
            if (x < Home.x - arenaHalfWidth) { x = Home.x - arenaHalfWidth; _driftDir = 1; }
            if (x > Home.x + arenaHalfWidth) { x = Home.x + arenaHalfWidth; _driftDir = -1; }
            transform.position = new Vector3(x,
                Mathf.MoveTowards(transform.position.y, targetY, 3f * dt), transform.position.z);
        }

        void FireBlade(PlayerController player)
        {
            if (bladePrefab == null) return;
            var shot = Instantiate(bladePrefab, transform.position + new Vector3(Facing * 0.8f, 0.2f, 0f), Quaternion.identity);
            // The template lives in the scene switched off, so the copy has to be woken.
            shot.gameObject.SetActive(true);
            shot.Launch(Facing);
        }

        void DropColumn(PlayerController player)
        {
            if (columnPrefab == null) return;
            var at = new Vector3(player.transform.position.x, Home.y, 0f);
            var column = Instantiate(columnPrefab, at, Quaternion.identity);
            column.gameObject.SetActive(true);
            column.Arm();
        }

        void TickSink(float dt)
        {
            _timer -= dt;
            transform.position = new Vector3(transform.position.x,
                Mathf.MoveTowards(transform.position.y, Home.y + 0.2f, 5f * dt), transform.position.z);
            if (_timer <= 0f)
            {
                _phase = Phase.Core;
                _timer = coreWindow;
                _coreHits = 0;
            }
        }

        void TickCore(float dt)
        {
            _timer -= dt;
            // the core glows: this is the window, and it is visible
            if (Sr != null)
                Sr.color = Color.Lerp(Color.white, new Color(1f, 0.78f, 0.35f), 0.5f + 0.5f * Mathf.Sin(Time.time * 12f));
            if (_timer <= 0f) BackToPattern();
        }

        void TickRise(float dt)
        {
            _timer -= dt;
            transform.position = new Vector3(transform.position.x,
                Mathf.MoveTowards(transform.position.y, Home.y + hoverHeight, 4f * dt), transform.position.z);
            if (_timer <= 0f) _phase = Phase.Pattern;
        }

        void BackToPattern()
        {
            if (Sr != null) Sr.color = Color.white;
            _attacksLeft = attacksPerPhase;
            _attackTimer = 0.4f;
            _phase = Phase.Rising;
            _timer = 0.5f;
        }

        public override void TakeHit(int amount, int fromFacing, bool lethal)
        {
            if (!CoreExposed)
            {
                // Sand closes over the blade: no damage while it is airborne.
                Clash();
                var player = Player;
                if (player != null)
                    player.Clash(transform.position.x > player.transform.position.x ? 1 : -1);
                return;
            }

            _coreHits++;
            base.TakeHit(amount, fromFacing, false);
            if (Downed) return;
            if (_coreHits >= hitsPerPhase)
            {
                _stage = Mathf.Min(3, _stage + 1);
                ClearSnuff();
                BackToPattern();
            }
        }

        void ClearSnuff()
        {
            _snuffed = false;
            _snuffTimer = 0f;
            foreach (var t in snuffTargets)
                if (t is IMechanismTarget m) m.SetPowered(false);
        }

        protected override void OnDowned()
        {
            ClearSnuff();
            foreach (var t in shutdownTargets)
                if (t is IMechanismTarget m) m.SetPowered(true);
            if (reward != null) reward.SetActive(true);
        }

        protected override void OnRestored()
        {
            _phase = Phase.Pattern;
            _stage = 1;
            _attacksLeft = attacksPerPhase;
            _attackTimer = 1f;
            _coreHits = 0;
            ClearSnuff();
            if (Sr != null) Sr.color = Color.white;
            foreach (var shot in FindObjectsByType<SandBlade>(FindObjectsSortMode.None)) Destroy(shot.gameObject);
            foreach (var col in FindObjectsByType<SandColumn>(FindObjectsSortMode.None)) Destroy(col.gameObject);
        }
    }
}
