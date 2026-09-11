using System;

namespace HourglassPalace
{
    /// <summary>Icon shown next to something the cat can interact with.</summary>
    public enum PromptIcon { None, Paw, Hand, Blade, Moon }

    /// <summary>
    /// The five rule-layer events the slice is built around: damage, death,
    /// checkpoint restore, hourglass empty and moon-gate victory.  UI, animation
    /// and audio only subscribe here - they never own a rule.
    /// </summary>
    public static class GameEvents
    {
        public static event Action<int, bool> PlayerDamaged;   // health left, was it lethal
        public static event Action PlayerDied;
        public static event Action<int> CheckpointRestored;    // lantern index
        public static event Action HourglassEmptied;
        public static event Action MoonGateOpened;

        public static void RaisePlayerDamaged(int healthLeft, bool lethal) => PlayerDamaged?.Invoke(healthLeft, lethal);
        public static void RaisePlayerDied() => PlayerDied?.Invoke();
        public static void RaiseCheckpointRestored(int lanternIndex) => CheckpointRestored?.Invoke(lanternIndex);
        public static void RaiseHourglassEmptied() => HourglassEmptied?.Invoke();
        public static void RaiseMoonGateOpened() => MoonGateOpened?.Invoke();

        /// <summary>Static events survive play-mode reloads, so clear them on load.</summary>
        [UnityEngine.RuntimeInitializeOnLoadMethod(UnityEngine.RuntimeInitializeLoadType.SubsystemRegistration)]
        public static void ResetAll()
        {
            PlayerDamaged = null;
            PlayerDied = null;
            CheckpointRestored = null;
            HourglassEmptied = null;
            MoonGateOpened = null;
        }
    }
}
