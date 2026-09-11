using UnityEngine;

namespace HourglassPalace
{
    /// <summary>
    /// A hanging chain.  It is grabbed only when the player actually asks for it
    /// (up or down held), so a chain across a run does not swallow the run.  The
    /// sway is decoration - the climb itself is a straight, readable line.
    /// </summary>
    [RequireComponent(typeof(BoxCollider2D))]
    public sealed class ClimbChain : MonoBehaviour
    {
        public float swayDegrees = 2.5f;
        public float swayRate = 0.9f;
        public Transform visual;

        BoxCollider2D _volume;
        float _phase;

        void Awake()
        {
            _volume = GetComponent<BoxCollider2D>();
            _volume.isTrigger = true;
            _phase = Random.value * 10f;
        }

        void Update()
        {
            if (visual == null) return;
            float sway = Mathf.Sin((Time.time + _phase) * swayRate) * swayDegrees;
            visual.localRotation = Quaternion.Euler(0f, 0f, sway);
        }

        public bool Contains(Vector2 worldPoint) => _volume.OverlapPoint(worldPoint);

        void OnTriggerStay2D(Collider2D other)
        {
            if (GameDirector.Paused) return;
            var player = other.GetComponentInParent<PlayerController>();
            if (player == null || player.Dead) return;
            if (player.State == PlayerController.MoveState.Chain) return;
            var kb = UnityEngine.InputSystem.Keyboard.current;
            if (kb == null) return;
            bool reaching = kb.wKey.isPressed || kb.upArrowKey.isPressed
                            || kb.sKey.isPressed || kb.downArrowKey.isPressed;
            if (reaching) player.EnterChain(this);
        }

        void OnTriggerExit2D(Collider2D other)
        {
            var player = other.GetComponentInParent<PlayerController>();
            if (player != null) player.ExitChain(this);
        }
    }
}
