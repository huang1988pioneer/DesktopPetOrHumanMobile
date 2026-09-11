using UnityEngine;

namespace HourglassPalace
{
    /// <summary>A standing danger: an open pit floor, a bed of fixed spikes.</summary>
    public sealed class LethalZone : MonoBehaviour
    {
        public bool instantKill = true;
        public int damage = 1;

        void OnTriggerEnter2D(Collider2D other) => Touch(other);
        void OnTriggerStay2D(Collider2D other) => Touch(other);

        void Touch(Collider2D other)
        {
            if (GameDirector.Paused) return;
            var player = other.GetComponentInParent<PlayerController>();
            if (player != null) player.Hurt(damage, instantKill);
        }
    }
}
