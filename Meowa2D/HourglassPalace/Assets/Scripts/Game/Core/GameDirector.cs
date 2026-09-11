using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.InputSystem;
using UnityEngine.SceneManagement;

namespace HourglassPalace
{
    /// <summary>
    /// Owns the global run state: the royal hourglass, the current lantern and
    /// its snapshot, the moon key, and the two ways the slice can end - the
    /// palace sealing itself, or the moon gate opening.
    /// </summary>
    public sealed class GameDirector : MonoBehaviour
    {
        public static GameDirector Instance { get; private set; }

        /// <summary>True while a cutscene-ish state owns the screen; actors idle.</summary>
        public static bool Paused { get; private set; }

        [Header("Hourglass")]
        [Tooltip("Baseline for the slice. Design target: 90-180s left on a first run, 315-450s on a practised route.")]
        public float hourglassSeconds = 900f;
        [Tooltip("Sand granted when the player retries after the palace seals itself.")]
        public float sealRetrySeconds = 300f;
        [Range(0f, 1f)] public float lowSandFraction = 0.2f;

        [Header("Scene")]
        public PlayerController player;
        public LanternCheckpoint startLantern;

        readonly Dictionary<string, object> _snapshot = new Dictionary<string, object>();
        readonly List<ISnapshotable> _tracked = new List<ISnapshotable>();

        float _remaining;
        bool _sealed;
        bool _won;
        bool _hasMoonKey;
        bool _respawning;
        int _snapHealth;
        bool _snapMoonKey;
        LanternCheckpoint _lantern;

        public float Remaining => Mathf.Max(0f, _remaining);
        public float RemainingFraction => hourglassSeconds <= 0f ? 0f : Mathf.Clamp01(_remaining / hourglassSeconds);
        public bool LowSand => !_sealed && !_won && RemainingFraction <= lowSandFraction;
        public bool HasMoonKey => _hasMoonKey;
        public bool PalaceSealed => _sealed;
        public bool Won => _won;
        public LanternCheckpoint CurrentLantern => _lantern;

        void Awake()
        {
            Instance = this;
            Paused = false;
            _remaining = hourglassSeconds;
            Time.fixedDeltaTime = 1f / 60f;
            Time.timeScale = 1f;
            QualitySettings.antiAliasing = 0;
            QualitySettings.anisotropicFiltering = AnisotropicFiltering.Disable;
            QualitySettings.vSyncCount = 1;
        }

        void Start()
        {
            _tracked.Clear();
            foreach (var mb in FindObjectsByType<MonoBehaviour>(FindObjectsInactive.Include, FindObjectsSortMode.None))
                if (mb is ISnapshotable s) _tracked.Add(s);

            if (startLantern != null) RegisterLantern(startLantern);
            else CaptureSnapshot();
        }

        void OnDestroy()
        {
            if (Instance == this) Instance = null;
            Paused = false;
        }

        void Update()
        {
            var kb = Keyboard.current;
            if (kb != null && kb.escapeKey.wasPressedThisFrame)
            {
                Paused = false;
                Time.timeScale = 1f;
                SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex);
                return;
            }

            if (_won) return;

            if (!_sealed && player != null)
            {
                _remaining -= Time.deltaTime;
                if (_remaining <= 0f) SealPalace();
            }

            // The seal is a state to read, not a fail sound: the player chooses
            // when to step back in from the last lantern.
            if (_sealed && kb != null && (kb.spaceKey.wasPressedThisFrame || kb.eKey.wasPressedThisFrame))
            {
                _sealed = false;
                Paused = false;
                _remaining = sealRetrySeconds;
                RestoreFromLantern();
            }
        }

        void SealPalace()
        {
            _remaining = 0f;
            _sealed = true;
            Paused = true;
            if (player != null) player.Freeze();
            GameEvents.RaiseHourglassEmptied();
        }

        // ---- checkpoints -------------------------------------------------

        public void RegisterLantern(LanternCheckpoint lantern)
        {
            if (lantern == null || _lantern == lantern) return;
            _lantern = lantern;
            CaptureSnapshot();
        }

        void CaptureSnapshot()
        {
            _snapshot.Clear();
            for (int i = 0; i < _tracked.Count; i++)
            {
                var s = _tracked[i];
                if (s == null) continue;
                _snapshot[s.SnapshotId] = s.Capture();
            }
            _snapHealth = player != null ? player.Health : 3;
            _snapMoonKey = _hasMoonKey;
        }

        public void OnPlayerDied(PlayerController who)
        {
            if (_respawning || _won) return;
            _respawning = true;
            GameEvents.RaisePlayerDied();
            StartCoroutine(RespawnAfter(0.9f));
        }

        IEnumerator RespawnAfter(float delay)
        {
            yield return new WaitForSeconds(delay);
            RestoreFromLantern();
        }

        void RestoreFromLantern()
        {
            _respawning = false;
            for (int i = 0; i < _tracked.Count; i++)
            {
                var s = _tracked[i];
                if (s == null) continue;
                if (_snapshot.TryGetValue(s.SnapshotId, out var state)) s.Restore(state);
            }
            _hasMoonKey = _snapMoonKey;

            if (player != null)
            {
                var spawn = _lantern != null ? _lantern.SpawnPoint : player.transform.position;
                // A lantern is a checkpoint, not a free heal: health comes back
                // exactly as it was when the flame was lit.
                player.Respawn(spawn, _snapHealth);
            }
            Paused = false;
            GameEvents.RaiseCheckpointRestored(_lantern != null ? _lantern.index : 0);
        }

        // ---- run objectives ----------------------------------------------

        public void GiveMoonKey() => _hasMoonKey = true;

        public bool TryOpenMoonGate()
        {
            if (!_hasMoonKey || _won) return false;
            _won = true;
            Paused = true;
            if (player != null) player.Freeze();
            GameEvents.RaiseMoonGateOpened();
            return true;
        }
    }
}
