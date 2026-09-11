using UnityEngine;

namespace HourglassPalace
{
    /// <summary>Background depth. Pure decoration - it owns no rule.</summary>
    public sealed class ParallaxLayer : MonoBehaviour
    {
        public Transform cam;
        public Vector2 factor = new Vector2(0.2f, 0.08f);

        Vector3 _start;
        Vector3 _camStart;

        void Start()
        {
            _start = transform.position;
            if (cam == null && Camera.main != null) cam = Camera.main.transform;
            _camStart = cam != null ? cam.position : Vector3.zero;
        }

        void LateUpdate()
        {
            if (cam == null) return;
            var delta = cam.position - _camStart;
            transform.position = new Vector3(
                _start.x + delta.x * factor.x,
                _start.y + delta.y * factor.y,
                _start.z);
        }
    }
}
