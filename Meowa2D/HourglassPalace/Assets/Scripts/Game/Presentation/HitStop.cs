using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// Two or three frames of frozen time on a connecting hit.  Purely felt,
    /// never a rule: nothing reads it back.
    /// </summary>
    public sealed class HitStop : MonoBehaviour
    {
        static HitStop _instance;
        float _remaining;

        public static void Punch(float seconds)
        {
            if (seconds <= 0f) return;
            if (_instance == null)
            {
                var go = new GameObject("~HitStop") { hideFlags = HideFlags.HideAndDontSave };
                _instance = go.AddComponent<HitStop>();
            }
            _instance._remaining = Mathf.Max(_instance._remaining, seconds);
            Time.timeScale = 0.02f;
        }

        void Update()
        {
            if (_remaining <= 0f) return;
            _remaining -= Time.unscaledDeltaTime;
            if (_remaining <= 0f)
            {
                _remaining = 0f;
                Time.timeScale = 1f;
            }
        }

        void OnDestroy()
        {
            if (_instance == this)
            {
                _instance = null;
                Time.timeScale = 1f;
            }
        }
    }
}
