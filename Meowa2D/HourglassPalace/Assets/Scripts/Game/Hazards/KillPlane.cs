using UnityEngine;

namespace HourglassPalace
{
    /// <summary>Backstop under the palace: nothing falls out of the level.</summary>
    public sealed class KillPlane : MonoBehaviour
    {
        public float y = -40f;

        void Update()
        {
            var player = GameDirector.Instance != null ? GameDirector.Instance.player : null;
            if (player != null && !player.Dead && player.transform.position.y < y)
                player.Hurt(1, true);
        }
    }
}
